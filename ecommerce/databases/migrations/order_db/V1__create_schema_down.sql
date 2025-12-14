-- V1__create_schema_down.sql
-- Rollback for order_db

DROP INDEX IF EXISTS "order".ix_order_items_order_id;
DROP INDEX IF EXISTS "order".ix_orders_order_date;
DROP INDEX IF EXISTS "order".ix_orders_user_id;

DROP TABLE IF EXISTS "order".order_items CASCADE;
DROP TABLE IF EXISTS "order".orders CASCADE;
DROP TABLE IF EXISTS "order".order_status CASCADE;

DROP SCHEMA IF EXISTS "order" CASCADE;

DROP EXTENSION IF EXISTS "uuid-ossp";
