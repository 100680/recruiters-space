-- V1__create_schema_down.sql
-- Rollback for product_db

DROP INDEX IF EXISTS product.ix_products_discount_method_id;
DROP INDEX IF EXISTS product.ix_products_category_id;

DROP TABLE IF EXISTS product.products CASCADE;
DROP TABLE IF EXISTS product.discount_methods CASCADE;
DROP TABLE IF EXISTS product.categories CASCADE;

DROP SCHEMA IF EXISTS product CASCADE;

DROP EXTENSION IF EXISTS "uuid-ossp";
