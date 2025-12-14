-- ================================================
--  Master Rollback Script for All Databases
--  WARNING: This will drop all objects and data!
-- ================================================

-- 6️⃣ CART DB
\c cart_db;
DROP INDEX IF EXISTS cart.ix_cart_items_user_id;
DROP TABLE IF EXISTS cart.cart_items CASCADE;
DROP SCHEMA IF EXISTS cart CASCADE;
-- DROP DATABASE IF EXISTS cart_db;

-- 5️⃣ PAYMENT DB
\c payment_db;
DROP INDEX IF EXISTS payment.ix_payments_order_id;
DROP TABLE IF EXISTS payment.payments CASCADE;
DROP TABLE IF EXISTS payment.payment_method_types CASCADE;
DROP SCHEMA IF EXISTS payment CASCADE;
-- DROP DATABASE IF EXISTS payment_db;

-- 4️⃣ REVIEW DB
\c review_db;
DROP INDEX IF EXISTS review.ix_reviews_product_id;
DROP TABLE IF EXISTS review.reviews CASCADE;
DROP SCHEMA IF EXISTS review CASCADE;
-- DROP DATABASE IF EXISTS review_db;

-- 3️⃣ ORDER DB
\c order_db;
DROP INDEX IF EXISTS "order".ix_order_items_order_id;
DROP INDEX IF EXISTS "order".ix_orders_order_date;
DROP INDEX IF EXISTS "order".ix_orders_user_id;
DROP TABLE IF EXISTS "order".order_items CASCADE;
DROP TABLE IF EXISTS "order".orders CASCADE;
DROP TABLE IF EXISTS "order".order_status CASCADE;
DROP SCHEMA IF EXISTS "order" CASCADE;
DROP EXTENSION IF EXISTS "uuid-ossp";
-- DROP DATABASE IF EXISTS order_db;

-- 2️⃣ PRODUCT DB
\c product_db;
DROP INDEX IF EXISTS product.ix_product_discounts_active;
DROP INDEX IF EXISTS product.ix_product_discounts_product_id;
DROP INDEX IF EXISTS product.ix_products_category_id;
DROP INDEX IF EXISTS product.ix_categories_name;
DROP TABLE IF EXISTS product.product_discounts CASCADE;
DROP TABLE IF EXISTS product.discount_methods CASCADE;
DROP TABLE IF EXISTS product.products CASCADE;
DROP TABLE IF EXISTS product.categories CASCADE;
DROP SCHEMA IF EXISTS product CASCADE;
DROP EXTENSION IF EXISTS "uuid-ossp";
-- DROP DATABASE IF EXISTS product_db;

-- 1️⃣ USER DB
\c user_db;
DROP INDEX IF EXISTS user.ix_users_email;
DROP TABLE IF EXISTS user.users CASCADE;
DROP SCHEMA IF EXISTS user CASCADE;
DROP EXTENSION IF EXISTS "uuid-ossp";
-- DROP DATABASE IF EXISTS user_db;
