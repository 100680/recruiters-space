// ebuy_schema.js
// Run in mongosh: load("ebuy_schema.js");

// Switch to (or create) ebuy_db
use ebuy_db;

// -------------------------
// USER COLLECTION
// -------------------------
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "email", "password_hash"],
      properties: {
        name: { bsonType: "string", maxLength: 100 },
        email: { bsonType: "string", pattern: "^.+@.+$" },
        password_hash: { bsonType: "string" },
        address: { bsonType: "string" },
        phone: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: "string" },
        service_origin: { bsonType: "string" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.users.createIndex({ email: 1 }, { unique: true });

// -------------------------
// CATEGORY COLLECTION
// -------------------------
db.createCollection("categories", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name"],
      properties: {
        name: { bsonType: "string", maxLength: 100 },
        description: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.categories.createIndex({ name: 1 }, { unique: true });

// -------------------------
// PRODUCTS COLLECTION
// -------------------------
db.createCollection("products", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "price", "stock", "category_id"],
      properties: {
        name: { bsonType: "string", maxLength: 100 },
        description: { bsonType: "string" },
        price: { bsonType: "decimal" },
        stock: { bsonType: "int" },
        category_id: { bsonType: "objectId" },
        image_url: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: "string" },
        service_origin: { bsonType: "string" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.products.createIndex({ category_id: 1 });

// -------------------------
// DISCOUNT METHODS
// -------------------------
db.createCollection("discount_methods", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        method_name: { bsonType: "string", maxLength: 50 },
        description: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.discount_methods.createIndex({ method_name: 1 }, { unique: true });

// -------------------------
// PRODUCT DISCOUNTS
// -------------------------
db.createCollection("product_discounts", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "discount_method_id", "discount_value", "start_date"],
      properties: {
        product_id: { bsonType: "objectId" },
        discount_method_id: { bsonType: "objectId" },
        discount_value: { bsonType: "decimal" },
        start_date: { bsonType: "date" },
        end_date: { bsonType: "date" },
        active: { bsonType: "bool" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.product_discounts.createIndex({ product_id: 1 });
db.product_discounts.createIndex({ active: 1 });

// -------------------------
// ORDER STATUS
// -------------------------
db.createCollection("order_status", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["status_name"],
      properties: {
        status_name: { bsonType: "string", maxLength: 50 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});

// -------------------------
// ORDERS
// -------------------------
db.createCollection("orders", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "status_id", "total_amount"],
      properties: {
        user_id: { bsonType: "objectId" },
        status_id: { bsonType: "objectId" },
        order_date: { bsonType: "date" },
        total_amount: { bsonType: "decimal" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: "string" },
        service_origin: { bsonType: "string" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.orders.createIndex({ user_id: 1 });
db.orders.createIndex({ order_date: 1 });

// -------------------------
// ORDER ITEMS
// -------------------------
db.createCollection("order_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "product_id", "quantity", "price"],
      properties: {
        order_id: { bsonType: "objectId" },
        product_id: { bsonType: "objectId" },
        quantity: { bsonType: "int", minimum: 1 },
        price: { bsonType: "decimal" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.order_items.createIndex({ order_id: 1 });

// -------------------------
// REVIEWS
// -------------------------
db.createCollection("reviews", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "product_id", "rating"],
      properties: {
        user_id: { bsonType: "objectId" },
        product_id: { bsonType: "objectId" },
        rating: { bsonType: "int", minimum: 1, maximum: 5 },
        comment: { bsonType: "string" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.reviews.createIndex({ product_id: 1 });

// -------------------------
// PAYMENT METHODS
// -------------------------
db.createCollection("payment_method_types", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        method_name: { bsonType: "string", maxLength: 50 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});

// -------------------------
// PAYMENTS
// -------------------------
db.createCollection("payments", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "payment_method_type_id", "amount", "payment_status"],
      properties: {
        order_id: { bsonType: "objectId" },
        payment_method_type_id: { bsonType: "objectId" },
        payment_date: { bsonType: "date" },
        amount: { bsonType: "decimal" },
        payment_status: { bsonType: "string", maxLength: 50 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.payments.createIndex({ order_id: 1 });

// -------------------------
// CART ITEMS
// -------------------------
db.createCollection("cart_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "quantity"],
      properties: {
        user_id: { bsonType: "objectId" },
        session_id: { bsonType: "string" },
        product_id: { bsonType: "objectId" },
        quantity: { bsonType: "int", minimum: 1 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.cart_items.createIndex({ user_id: 1 });
