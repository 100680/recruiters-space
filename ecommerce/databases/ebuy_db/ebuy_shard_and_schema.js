// ebuy_shard_and_schema.js
// Single deploy script: validation, indexes, sharding, pre-split
// Run on a mongos. Test in staging first.

// Use the target DB
const DBNAME = "ebuy_db";
db = db.getSiblingDB(DBNAME);

// -----------------------------
// Utility helpers
// -----------------------------
function collectionExists(name) {
  return db.getCollectionNames().indexOf(name) !== -1;
}

// create or update collection validator (idempotent)
function ensureCollectionValidator(collName, validator) {
  if (collectionExists(collName)) {
    // update validator using collMod
    try {
      print("collMod: updating validator for " + collName);
      db.runCommand({
        collMod: collName,
        validator: validator,
        validationLevel: "moderate", // moderate to allow existing docs during rollout
        validationAction: "warn"
      });
    } catch (e) {
      print("Warning: collMod failed for " + collName + " - " + e);
    }
  } else {
    print("createCollection: " + collName);
    db.createCollection(collName, { validator: validator, validationLevel: "moderate", validationAction: "warn" });
  }
}

// ensure index exists (idempotent)
function ensureIndex(collName, indexSpec, options) {
  options = options || {};
  try {
    db.getCollection(collName).createIndex(indexSpec, options);
  } catch (e) {
    print("Index creation failed for " + collName + " with " + tojson(indexSpec) + " -> " + e);
  }
}

// Check if a collection is already sharded
function isSharded(ns) {
  var cfg = sh.status(); // side-effect but ok
  var s = sh.getShardDistribution(ns); // might throw if not sharded
  try {
    var res = sh.getBalancerState();
  } catch (e) { }
  // Simpler check: consult config.collections
  var collInfo = db.getSiblingDB("config").collections.findOne({ _id: ns });
  return collInfo && collInfo.dropped === false && collInfo.key;
}

// Pre-split helper for hashed collections
function preSplitHashedCollection(ns, field, splitCount) {
  // ns example: "ebuy_db.users"
  print("[preSplit] Preparing to pre-split " + ns + " on hashed field '" + field + "' into " + splitCount + " chunks (if not already sharded)");

  // create index for shard key (hashed)
  var [dbName, collName] = ns.split(".");
  var coll = db.getSiblingDB(dbName).getCollection(collName);
  var idx = {};
  idx[field] = "hashed";
  coll.createIndex(idx);

  // shard collection if not already sharded
  var already = db.getSiblingDB("config").collections.findOne({ _id: ns });
  if (!already) {
    print("Sharding collection " + ns + " by {" + field + ": 'hashed'}");
    sh.shardCollection(ns, idx);
  } else {
    print(ns + " appears already present in config.collections (may already be sharded)");
  }

  // Pre-splitting hashed key space:
  // NOTE: Hashed keys are a 64-bit space. We'll attempt to create even splits by numeric offsets.
  // This is a heuristic suitable for many cases — test in staging.
  try {
    var min = NumberLong("-9223372036854775808");
    var max = NumberLong("9223372036854775807");
    var range = (max.toNumber ? max.toNumber() : 0) - (min.toNumber ? min.toNumber() : 0);
  } catch (err) {
    // fallback if toNumber not available
    print("Pre-split: numeric range calc fallback.");
  }

  // We'll create split points by generating numeric values in the 64-bit range.
  // NOTE: For very large splitCount, this may be slow; choose sensible values (4,8,16).
  for (var i = 1; i < splitCount; i++) {
    // compute approximate long value
    // Use BigInt for safety (if mongosh supports it)
    try {
      let bigMin = BigInt("-9223372036854775808");
      let bigMax = BigInt("9223372036854775807");
      let bigRange = bigMax - bigMin;
      let step = bigRange / BigInt(splitCount);
      let val = bigMin + step * BigInt(i);
      // convert to NumberLong-like object via string:
      let splitPoint = {};
      splitPoint[field] = NumberLong(val.toString());
      try {
        sh.splitAt(ns, splitPoint);
        print("Created split at " + tojson(splitPoint));
      } catch (e) {
        // splitAt may fail for hashed key specifics; ignore but log
        print("splitAt failed for " + ns + " with point " + tojson(splitPoint) + " -> " + e);
      }
    } catch (ex) {
      print("Pre-split BigInt path failed: " + ex + " — skipping further pre-splits for " + ns);
      break;
    }
  }

  print("[preSplit] Done for " + ns);
}

// -----------------------------
// 1) Create/Update Collections + Validation + Indexes
// -----------------------------

print("== Step 1: Create collections and validators ==");

// USERS
ensureCollectionValidator("users", {
  $jsonSchema: {
    bsonType: "object",
    required: ["name", "email", "password_hash", "created_at", "modified_at"],
    properties: {
      user_id: { bsonType: "int" },
      name: { bsonType: "string", maxLength: 100 },
      email: { bsonType: "string", pattern: "^.+@.+\\..+$" },
      password_hash: { bsonType: "string" },
      address: { bsonType: ["string", "null"], maxLength: 255 },
      phone: { bsonType: ["string", "null"], maxLength: 20 },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      correlation_id: { bsonType: ["string", "null"] },
      service_origin: { bsonType: ["string", "null"] },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("users", { email: 1 }, { unique: true, name: "idx_users_email" });
ensureIndex("users", { user_id: 1 }, { name: "idx_users_user_id" });

// CATEGORIES
ensureCollectionValidator("categories", {
  $jsonSchema: {
    bsonType: "object",
    required: ["name", "created_at", "modified_at"],
    properties: {
      category_id: { bsonType: "int" },
      name: { bsonType: "string", maxLength: 100 },
      description: { bsonType: ["string", "null"] },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("categories", { name: 1 }, { unique: true, name: "idx_categories_name" });

// PRODUCTS
ensureCollectionValidator("products", {
  $jsonSchema: {
    bsonType: "object",
    required: ["name", "price", "stock", "category_id", "created_at", "modified_at"],
    properties: {
      product_id: { bsonType: "int" },
      name: { bsonType: "string", maxLength: 100 },
      description: { bsonType: ["string", "null"] },
      price: { bsonType: "decimal" },
      stock: { bsonType: "int", minimum: 0 },
      category_id: { bsonType: "int" },
      image_url: { bsonType: ["string", "null"] },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      correlation_id: { bsonType: ["string", "null"] },
      service_origin: { bsonType: ["string", "null"] },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("products", { product_id: 1 }, { name: "idx_products_product_id" });
ensureIndex("products", { category_id: 1 }, { name: "idx_products_category_id" });
ensureIndex("products", { name: "text" }, { name: "idx_products_text_name" });

// DISCOUNT METHODS
ensureCollectionValidator("discount_methods", {
  $jsonSchema: {
    bsonType: "object",
    required: ["method_name", "created_at", "modified_at"],
    properties: {
      discount_method_id: { bsonType: "int" },
      method_name: { bsonType: "string", maxLength: 50 },
      description: { bsonType: ["string", "null"] },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("discount_methods", { method_name: 1 }, { unique: true, name: "idx_discount_methods_name" });

// PRODUCT DISCOUNTS
ensureCollectionValidator("product_discounts", {
  $jsonSchema: {
    bsonType: "object",
    required: ["product_id", "discount_method_id", "discount_value", "start_date", "created_at", "modified_at"],
    properties: {
      product_discount_id: { bsonType: "int" },
      product_id: { bsonType: "int" },
      discount_method_id: { bsonType: "int" },
      discount_value: { bsonType: "decimal" },
      start_date: { bsonType: "date" },
      end_date: { bsonType: ["date", "null"] },
      active: { bsonType: "bool" },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("product_discounts", { product_id: 1 }, { name: "idx_pd_product_id" });
ensureIndex("product_discounts", { active: 1 }, { name: "idx_pd_active" });

// ORDER STATUS
ensureCollectionValidator("order_status", {
  $jsonSchema: {
    bsonType: "object",
    required: ["status_name", "created_at", "modified_at"],
    properties: {
      status_id: { bsonType: "int" },
      status_name: { bsonType: "string", maxLength: 50 },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});

// ORDERS
ensureCollectionValidator("orders", {
  $jsonSchema: {
    bsonType: "object",
    required: ["user_id", "status_id", "order_date", "total_amount", "created_at", "modified_at"],
    properties: {
      order_id: { bsonType: "int" },
      user_id: { bsonType: "int" },
      status_id: { bsonType: "int" },
      order_date: { bsonType: "date" },
      total_amount: { bsonType: "decimal" },
      order_items: {
        bsonType: "array",
        description: "Embedded order items (recommended)",
        items: {
          bsonType: "object",
          required: ["product_id", "quantity", "price"],
          properties: {
            product_id: { bsonType: "int" },
            quantity: { bsonType: "int", minimum: 1 },
            price: { bsonType: "decimal" }
          }
        }
      },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      correlation_id: { bsonType: ["string", "null"] },
      service_origin: { bsonType: ["string", "null"] },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("orders", { user_id: 1 }, { name: "idx_orders_user_id" });
ensureIndex("orders", { order_date: -1 }, { name: "idx_orders_order_date" });

// ORDER_ITEMS (if kept separate)
ensureCollectionValidator("order_items", {
  $jsonSchema: {
    bsonType: "object",
    required: ["order_id", "product_id", "quantity", "price", "created_at", "modified_at"],
    properties: {
      order_item_id: { bsonType: "int" },
      order_id: { bsonType: "int" },
      product_id: { bsonType: "int" },
      quantity: { bsonType: "int", minimum: 1 },
      price: { bsonType: "decimal" },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("order_items", { order_id: 1 }, { name: "idx_orderitems_order_id" });

// REVIEWS
ensureCollectionValidator("reviews", {
  $jsonSchema: {
    bsonType: "object",
    required: ["user_id", "product_id", "rating", "created_at", "modified_at"],
    properties: {
      review_id: { bsonType: "int" },
      user_id: { bsonType: "int" },
      product_id: { bsonType: "int" },
      rating: { bsonType: "int", minimum: 1, maximum: 5 },
      comment: { bsonType: ["string", "null"] },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("reviews", { product_id: 1 }, { name: "idx_reviews_product_id" });

// PAYMENT METHOD TYPES
ensureCollectionValidator("payment_method_types", {
  $jsonSchema: {
    bsonType: "object",
    required: ["method_name", "created_at", "modified_at"],
    properties: {
      payment_method_type_id: { bsonType: "int" },
      method_name: { bsonType: "string", maxLength: 50 },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("payment_method_types", { method_name: 1 }, { unique: true, name: "idx_payment_methods_name" });

// PAYMENTS
ensureCollectionValidator("payments", {
  $jsonSchema: {
    bsonType: "object",
    required: ["order_id", "payment_method_type_id", "amount", "payment_status", "created_at", "modified_at"],
    properties: {
      payment_id: { bsonType: "int" },
      order_id: { bsonType: "int" },
      payment_method_type_id: { bsonType: "int" },
      payment_date: { bsonType: ["date","null"] },
      amount: { bsonType: "decimal" },
      payment_status: { bsonType: "string", maxLength: 50 },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("payments", { order_id: 1 }, { name: "idx_payments_order_id" });

// CART ITEMS
ensureCollectionValidator("cart_items", {
  $jsonSchema: {
    bsonType: "object",
    required: ["product_id", "quantity", "created_at", "modified_at"],
    properties: {
      cart_item_id: { bsonType: "int" },
      user_id: { bsonType: ["int", "null"] },
      session_id: { bsonType: ["string", "null"] },
      product_id: { bsonType: "int" },
      quantity: { bsonType: "int", minimum: 1 },
      created_at: { bsonType: "date" },
      modified_at: { bsonType: "date" },
      row_version: { bsonType: "long" }
    }
  }
});
ensureIndex("cart_items", { user_id: 1 }, { name: "idx_cart_user_id" });
ensureIndex("cart_items", { session_id: 1 }, { name: "idx_cart_session_id" });

print("== Collections, validators, and indexes ensured ==");

// -----------------------------
// 2) Sharding configuration
// -----------------------------
print("== Step 2: Enable sharding and shard selected collections ==");

// Enable sharding on DB (idempotent)
try {
  sh.enableSharding(DBNAME);
  print("Sharding enabled for DB: " + DBNAME);
} catch (e) {
  print("enableSharding: " + e);
}

// Helper to shard a collection by a spec if not already sharded
function ensureSharding(ns, keySpec) {
  const cfgColl = db.getSiblingDB("config").collections.findOne({ _id: ns });
  if (cfgColl && cfgColl.key) {
    print(ns + " already sharded with key: " + tojson(cfgColl.key));
    return;
  }
  // ensure index exists that matches keySpec
  try {
    db.getCollection(ns.split(".")[1]).createIndex(keySpec);
  } catch (e) {
    print("ensure index for shard key failed: " + e);
  }
  try {
    sh.shardCollection(ns, keySpec);
    print("Sharded " + ns + " by " + tojson(keySpec));
  } catch (err) {
    print("shardCollection failed for " + ns + ": " + err);
  }
}

// Use preSplitHashedCollection for hashed pre-splits (if you plan big imports)
print("== Pre-splitting hashed collections (set split counts as desired) ==");

// Example pre-splits (tweak splitCounts based on expected size)
// For safety, you can comment out pre-split calls if you don't need them.
try {
  preSplitHashedCollection(DBNAME + ".users", "user_id", 8);
  preSplitHashedCollection(DBNAME + ".products", "product_id", 8);
  preSplitHashedCollection(DBNAME + ".product_discounts", "product_id", 4);
  // orders: compound key - shard without pre-split (hashed prefix + date)
  // payments: compound key - shard without pre-split
  preSplitHashedCollection(DBNAME + ".order_items", "order_id", 8);
  preSplitHashedCollection(DBNAME + ".reviews", "product_id", 4);
  preSplitHashedCollection(DBNAME + ".cart_items", "user_id", 4);
} catch (e) {
  print("Pre-split step caught error: " + e);
}

// Shard compound collections
// ORDERS: { user_id: "hashed", order_date: 1 }
try {
  ensureSharding(DBNAME + ".orders", { user_id: "hashed", order_date: 1 });
} catch (e) {
  print("orders shard error: " + e);
}

// PAYMENTS: { order_id: "hashed", payment_date: 1 }
try {
  ensureSharding(DBNAME + ".payments", { order_id: "hashed", payment_date: 1 });
} catch (e) {
  print("payments shard error: " + e);
}

// PRODUCTS (if you want them sharded — optional; already pre-split above)
try {
  ensureSharding(DBNAME + ".products", { product_id: "hashed" });
} catch (e) {
  print("products shard error: " + e);
}

// PRODUCT_DISCOUNTS (shard by product_id)
try {
  ensureSharding(DBNAME + ".product_discounts", { product_id: "hashed" });
} catch (e) {
  print("product_discounts shard error: " + e);
}

// ORDER_ITEMS (if separate)
try {
  ensureSharding(DBNAME + ".order_items", { order_id: "hashed" });
} catch (e) {
  print("order_items shard error: " + e);
}

// REVIEWS
try {
  ensureSharding(DBNAME + ".reviews", { product_id: "hashed" });
} catch (e) {
  print("reviews shard error: " + e);
}

// CART_ITEMS
try {
  ensureSharding(DBNAME + ".cart_items", { user_id: "hashed" });
} catch (e) {
  print("cart_items shard error: " + e);
}

print("== Sharding setup finished ==");
sh.status();
