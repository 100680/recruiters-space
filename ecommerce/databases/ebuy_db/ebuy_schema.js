// =========================================================
// ebuy_schema.js
// Ready-to-run MongoDB schema for ebuy_db
// =========================================================

// Switch to or create the database
db = db.getSiblingDB("ebuy_db");

// -------------------------
// 1) USERS
// -------------------------
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "email", "password_hash"],
      properties: {
        user_id: { bsonType: "long" },
        name: { bsonType: "string" },
        email: { bsonType: "string" },
        password_hash: { bsonType: "string" },
        address: { bsonType: ["string", "null"] },
        phone: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: ["binData", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ is_deleted: 1 });

// -------------------------
// 2) PRODUCTS
// -------------------------

// Create categories collection with enhanced validation
db.createCollection("categories", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name"],
      properties: {
        category_id: { bsonType: "long" },
        name: { 
          bsonType: "string",
          minLength: 1,
          maxLength: 100
        },
        description: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        created_by: { bsonType: ["string", "null"] },
        modified_by: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

// Create discount_methods collection with enhanced fields
db.createCollection("discount_methods", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        discount_method_id: { bsonType: "long" },
        method_name: { 
          bsonType: "string",
          minLength: 1,
          maxLength: 50
        },
        description: { bsonType: ["string", "null"] },
        is_percentage: { bsonType: "bool" },
        max_discount_value: { bsonType: ["double", "int", "long", "decimal", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        created_by: { bsonType: ["string", "null"] },
        modified_by: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

// Create products collection with enhanced validation
db.createCollection("products", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "price", "stock", "category_id"],
      properties: {
        product_id: { bsonType: "long" },
        name: { 
          bsonType: "string",
          minLength: 1,
          maxLength: 100
        },
        description: { bsonType: ["string", "null"] },
        price: { 
          bsonType: ["double", "int", "long", "decimal"], 
          minimum: 0
        },
        stock: { bsonType: "int", minimum: 0 },
        reorder_level: { bsonType: "int", minimum: 0 },
        category_id: { bsonType: "long" },
        image_url: { bsonType: ["string", "null"] },
        sku: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: "binData" },
        created_by: { bsonType: ["string", "null"] },
        modified_by: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

// Create product_discounts collection
db.createCollection("product_discounts", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "discount_method_id", "discount_value", "start_date"],
      properties: {
        product_discount_id: { bsonType: "long" },
        product_id: { bsonType: "long" },
        discount_method_id: { bsonType: "long" },
        discount_value: { 
          bsonType: ["double", "int", "long", "decimal"], 
          minimum: 0 
        },
        start_date: { bsonType: "date" },
        end_date: { bsonType: ["date", "null"] },
        active: { bsonType: "bool" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        created_by: { bsonType: ["string", "null"] },
        modified_by: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

// Create product_price_history collection
db.createCollection("product_price_history", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "new_price", "changed_at"],
      properties: {
        price_history_id: { bsonType: "long" },
        product_id: { bsonType: "long" },
        old_price: { bsonType: ["double", "int", "long", "decimal", "null"] },
        new_price: { bsonType: ["double", "int", "long", "decimal"] },
        price_change_reason: { bsonType: ["string", "null"] },
        changed_at: { bsonType: "date" },
        changed_by: { bsonType: ["string", "null"] },
        correlation_id: { bsonType: "binData" }
      }
    }
  }
});

// Create indexes for all collections
// Categories indexes
db.categories.createIndex({ "name": 1 }, { unique: true, partialFilterExpression: { is_deleted: false } });
db.categories.createIndex({ "is_deleted": 1 });

// Discount methods indexes
db.discount_methods.createIndex({ "method_name": 1 }, { unique: true, partialFilterExpression: { is_deleted: false } });
db.discount_methods.createIndex({ "is_deleted": 1 });

// Products indexes
db.products.createIndex({ "category_id": 1, "is_deleted": 1 });
db.products.createIndex({ "is_deleted": 1 });
db.products.createIndex({ "name": "text", "description": "text" });
db.products.createIndex({ "price": 1 });
db.products.createIndex({ "stock": 1, "reorder_level": 1 });
db.products.createIndex({ "sku": 1 }, { unique: true, sparse: true });
db.products.createIndex({ "correlation_id": 1 });

// Product discounts indexes
db.product_discounts.createIndex({ "product_id": 1 });
db.product_discounts.createIndex({ "discount_method_id": 1 });
db.product_discounts.createIndex({ "active": 1, "is_deleted": 1 });
db.product_discounts.createIndex({ "start_date": 1, "end_date": 1 });

// Price history indexes
db.product_price_history.createIndex({ "product_id": 1, "changed_at": -1 });


// -------------------------
// 3) ORDER
// -------------------------
db.createCollection("order_status", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["status_name"],
      properties: {
        status_id: { bsonType: "long" },
        status_name: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.order_status.createIndex({ is_deleted: 1 });

db.createCollection("orders", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "status_id", "total_amount"],
      properties: {
        order_id: { bsonType: "long" },
        user_id: { bsonType: "long" },
        status_id: { bsonType: "long" },
        order_date: { bsonType: "date" },
        total_amount: { bsonType: "double", minimum: 0 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: ["binData", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.orders.createIndex({ user_id: 1 });
db.orders.createIndex({ status_id: 1 });
db.orders.createIndex({ order_date: 1 });
db.orders.createIndex({ is_deleted: 1 });

db.createCollection("order_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "product_id", "quantity", "price", "final_price"],
      properties: {
        order_item_id: { bsonType: "long" },
        order_id: { bsonType: "long" },
        product_id: { bsonType: "long" },
        quantity: { bsonType: "int", minimum: 1 },
        price: { bsonType: "double", minimum: 0 },
        discount_method_id: { bsonType: ["long", "null"] },
        discount_value: { bsonType: ["double", "null"], minimum: 0 },
        final_price: { bsonType: "double", minimum: 0 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.order_items.createIndex({ order_id: 1 });
db.order_items.createIndex({ discount_method_id: 1 });
db.order_items.createIndex({ is_deleted: 1 });

// -------------------------
// 4) REVIEWS
// -------------------------
db.createCollection("reviews", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "product_id", "rating"],
      properties: {
        review_id: { bsonType: "long" },
        user_id: { bsonType: "long" },
        product_id: { bsonType: "long" },
        rating: { bsonType: "int", minimum: 1, maximum: 5 },
        comment: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: ["binData", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.reviews.createIndex({ product_id: 1 });
db.reviews.createIndex({ is_deleted: 1 });
db.reviews.createIndex({ user_id: 1, product_id: 1 }, { unique: true });

// -------------------------
// 5) PAYMENTS
// -------------------------
db.createCollection("payment_method_types", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        payment_method_type_id: { bsonType: "long" },
        method_name: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.payment_method_types.createIndex({ method_name: 1 }, { unique: true });
db.payment_method_types.createIndex({ is_deleted: 1 });

db.createCollection("payments", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "payment_method_type_id", "amount", "payment_status"],
      properties: {
        payment_id: { bsonType: "long" },
        order_id: { bsonType: "long" },
        payment_method_type_id: { bsonType: "long" },
        payment_date: { bsonType: ["date", "null"] },
        amount: { bsonType: "double", minimum: 0 },
        payment_status: { 
          enum: ["pending","authorized","captured","failed","refunded","voided"] 
        },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: ["binData", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});
db.payments.createIndex({ order_id: 1 });
db.payments.createIndex({ payment_status: 1, created_at: 1 });
db.payments.createIndex({ is_deleted: 1 });

// -------------------------
// 6) CART
// -------------------------
db.createCollection("cart_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "quantity"],
      properties: {
        cart_item_id: { bsonType: "long" },
        user_id: { bsonType: ["long", "null"] },
        session_id: { bsonType: ["string", "null"] },
        product_id: { bsonType: "long" },
        quantity: { bsonType: "int", minimum: 1 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        deleted_at: { bsonType: ["date", "null"] },
        is_deleted: { bsonType: "bool" },
        correlation_id: { bsonType: ["binData", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long", minimum: 1 }
      }
    }
  }
});

// Unique constraints
db.cart_items.createIndex({ user_id: 1, product_id: 1 }, { unique: true, partialFilterExpression: { session_id: null } });
db.cart_items.createIndex({ session_id: 1, product_id: 1 }, { unique: true, partialFilterExpression: { user_id: null } });
db.cart_items.createIndex({ is_deleted: 1 });
db.cart_items.createIndex({ user_id: 1 });

print("MongoDB ebuy_db schema created successfully!");
