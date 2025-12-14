// ebuy_rollback.js
// Run in mongosh: load("ebuy_rollback.js");

use ebuy_db;

// -------------------------
// Drop collections in order
// -------------------------

// 1) CART
if (db.getCollectionNames().includes("cart_items")) {
  db.cart_items.drop();
}

// 2) PAYMENTS
if (db.getCollectionNames().includes("payments")) {
  db.payments.drop();
}
if (db.getCollectionNames().includes("payment_method_types")) {
  db.payment_method_types.drop();
}

// 3) REVIEWS
if (db.getCollectionNames().includes("reviews")) {
  db.reviews.drop();
}

// 4) ORDERS
if (db.getCollectionNames().includes("order_items")) {
  db.order_items.drop();
}
if (db.getCollectionNames().includes("orders")) {
  db.orders.drop();
}
if (db.getCollectionNames().includes("order_status")) {
  db.order_status.drop();
}

// 5) PRODUCTS
if (db.getCollectionNames().includes("product_discounts")) {
  db.product_discounts.drop();
}
if (db.getCollectionNames().includes("discount_methods")) {
  db.discount_methods.drop();
}
if (db.getCollectionNames().includes("products")) {
  db.products.drop();
}
if (db.getCollectionNames().includes("categories")) {
  db.categories.drop();
}

// 6) USERS
if (db.getCollectionNames().includes("users")) {
  db.users.drop();
}

print("All collections dropped successfully from ebuy_db");
