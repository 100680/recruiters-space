-- order_db/rollback_schema.sql
DROP INDEX IF EXISTS "order".ix_order_items_product_id;
DROP INDEX IF EXISTS "order".ix_order_items_order_id;
DROP INDEX IF EXISTS "order".uq_order_item_active;
DROP TABLE IF EXISTS "order".order_items CASCADE;
DROP TABLE IF EXISTS "order".orders CASCADE;
DROP TABLE IF EXISTS "order".order_status CASCADE;
DROP SCHEMA IF EXISTS "order" CASCADE;
DROP EXTENSION IF EXISTS "uuid-ossp";
