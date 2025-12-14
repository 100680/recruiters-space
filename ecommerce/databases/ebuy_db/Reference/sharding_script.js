// ================================================
// MongoDB Sharding Setup for ebuy_db
// ================================================

// 1) Enable sharding for the database
sh.enableSharding("ebuy_db");

// Switch to ebuy_db
use ebuy_db;

// ================================================
// USERS
// ================================================
db.users.createIndex({ user_id: 1 }, { name: "idx_user_id" });
sh.shardCollection("ebuy_db.users", { user_id: "hashed" });

// ================================================
// PRODUCTS
// ================================================
// If products collection is very large, shard it; otherwise skip
db.products.createIndex({ product_id: 1 }, { name: "idx_product_id" });
sh.shardCollection("ebuy_db.products", { product_id: "hashed" });

// ================================================
// CATEGORIES (small lookup table, no sharding)
// ================================================

// ================================================
// DISCOUNT METHODS (small lookup table, no sharding)
// ================================================

// ================================================
// PRODUCT_DISCOUNTS
// ================================================
db.product_discounts.createIndex({ product_id: 1 }, { name: "idx_product_id" });
sh.shardCollection("ebuy_db.product_discounts", { product_id: "hashed" });

// ================================================
// ORDERS
// ================================================
db.orders.createIndex({ user_id: "hashed", order_date: 1 }, { name: "idx_userid_hashed_orderdate" });
sh.shardCollection("ebuy_db.orders", { user_id: "hashed", order_date: 1 });

// ================================================
// ORDER_ITEMS (if separate; if embedded in orders, skip)
// ================================================
db.order_items.createIndex({ order_id: 1 }, { name: "idx_order_id" });
sh.shardCollection("ebuy_db.order_items", { order_id: "hashed" });

// ================================================
// ORDER_STATUS (small lookup table, no sharding)
// ================================================

// ================================================
// REVIEWS
// ================================================
db.reviews.createIndex({ product_id: 1 }, { name: "idx_product_id" });
sh.shardCollection("ebuy_db.reviews", { product_id: "hashed" });

// ================================================
// PAYMENTS
// ================================================
db.payments.createIndex({ order_id: "hashed", payment_date: 1 }, { name: "idx_orderid_hashed_paymentdate" });
sh.shardCollection("ebuy_db.payments", { order_id: "hashed", payment_date: 1 });

// ================================================
// PAYMENT_METHOD_TYPES (small lookup table, no sharding)
// ================================================

// ================================================
// CART_ITEMS
// ================================================
db.cart_items.createIndex({ user_id: 1 }, { name: "idx_user_id" });
sh.shardCollection("ebuy_db.cart_items", { user_id: "hashed" });

// ================================================
// Done
// ================================================
print("Sharding setup complete for ebuy_db");

// Optional: View status
sh.status();
