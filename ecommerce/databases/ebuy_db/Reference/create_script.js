// Connect to the database
use ebuy_db;

// =============================
// USERS
// =============================
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "email", "password_hash"],
      properties: {
        user_id: { bsonType: "int" },
        name: { bsonType: "string", maxLength: 100 },
        email: { bsonType: "string", pattern: "^.+@.+\\..+$" },
        password_hash: { bsonType: "string" },
        address: { bsonType: ["string", "null"] },
        phone: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: "string" },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.users.createIndex({ email: 1 }, { unique: true });

// =============================
// CATEGORIES
// =============================
db.createCollection("categories", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name"],
      properties: {
        category_id: { bsonType: "int" },
        name: { bsonType: "string", maxLength: 100 },
        description: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.categories.createIndex({ name: 1 }, { unique: true });

// =============================
// PRODUCTS
// =============================
db.createCollection("products", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "price", "stock", "category_id"],
      properties: {
        product_id: { bsonType: "int" },
        name: { bsonType: "string", maxLength: 100 },
        description: { bsonType: ["string", "null"] },
        price: { bsonType: "decimal" },
        stock: { bsonType: "int" },
        category_id: { bsonType: "int" },
        image_url: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: ["string", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.products.createIndex({ category_id: 1 });

// =============================
// DISCOUNT METHODS
// =============================
db.createCollection("discount_methods", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        discount_method_id: { bsonType: "int" },
        method_name: { bsonType: "string", maxLength: 50 },
        description: { bsonType: ["string", "null"] },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.discount_methods.createIndex({ method_name: 1 }, { unique: true });

// =============================
// PRODUCT DISCOUNTS
// =============================
db.createCollection("product_discounts", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "discount_method_id", "discount_value", "start_date"],
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
  }
});
db.product_discounts.createIndex({ product_id: 1 });
db.product_discounts.createIndex({ active: 1 });

// =============================
// ORDER STATUS
// =============================
db.createCollection("order_status", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["status_name"],
      properties: {
        status_id: { bsonType: "int" },
        status_name: { bsonType: "string", maxLength: 50 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});

// =============================
// ORDERS
// =============================
db.createCollection("orders", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "status_id", "total_amount"],
      properties: {
        order_id: { bsonType: "int" },
        user_id: { bsonType: "int" },
        status_id: { bsonType: "int" },
        order_date: { bsonType: "date" },
        total_amount: { bsonType: "decimal" },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        correlation_id: { bsonType: ["string", "null"] },
        service_origin: { bsonType: ["string", "null"] },
        row_version: { bsonType: "long" }
      }
    }
  }
});
db.orders.createIndex({ user_id: 1 });
db.orders.createIndex({ order_date: 1 });

// =============================
// ORDER ITEMS
// =============================
db.createCollection("order_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "product_id", "quantity", "price"],
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
  }
});
db.order_items.createIndex({ order_id: 1 });

// =============================
// REVIEWS
// =============================
db.createCollection("reviews", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["user_id", "product_id", "rating"],
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
  }
});
db.reviews.createIndex({ product_id: 1 });

// =============================
// PAYMENT METHOD TYPES
// =============================
db.createCollection("payment_method_types", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["method_name"],
      properties: {
        payment_method_type_id: { bsonType: "int" },
        method_name: { bsonType: "string", maxLength: 50 },
        created_at: { bsonType: "date" },
        modified_at: { bsonType: "date" },
        row_version: { bsonType: "long" }
      }
    }
  }
});

// =============================
// PAYMENTS
// =============================
db.createCollection("payments", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["order_id", "payment_method_type_id", "amount", "payment_status"],
      properties: {
        payment_id: { bsonType: "int" },
        order_id: { bsonType: "int" },
        payment_method_type_id: { bsonType: "int" },
        payment_date: { bsonType: ["date", "null"] },
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

// =============================
// CART ITEMS
// =============================
db.createCollection("cart_items", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["product_id", "quantity"],
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
  }
});
db.cart_items.createIndex({ user_id: 1 });
