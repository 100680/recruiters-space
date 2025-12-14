-- V1__create_schema_down.sql
-- Rollback for cart_db

DROP INDEX IF EXISTS cart.ix_cart_items_user_id;

DROP TABLE IF EXISTS cart.cart_items CASCADE;

DROP SCHEMA IF EXISTS cart CASCADE;
