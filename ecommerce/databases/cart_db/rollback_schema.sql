-- order_db/rollback_schema.sql

-- =============================
-- 1. Drop Triggers
-- =============================
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_orders_update'
  ) THEN
    EXECUTE 'DROP TRIGGER trg_orders_update ON order_schema.orders';
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_order_items_update'
  ) THEN
    EXECUTE 'DROP TRIGGER trg_order_items_update ON order_schema.order_items';
  END IF;
END$$;

-- =============================
-- 2. Drop Indexes
-- =============================
DROP INDEX IF EXISTS order_schema.uq_order_status_name_active;
DROP INDEX IF EXISTS order_schema.ix_order_status_is_deleted;

DROP INDEX IF EXISTS order_schema.ix_orders_user_date;
DROP INDEX IF EXISTS order_schema.ix_orders_status_id;
DROP INDEX IF EXISTS order_schema.ix_orders_is_deleted;

DROP INDEX IF EXISTS order_schema.uq_order_item_active;
DROP INDEX IF EXISTS order_schema.ix_order_items_order_id;
DROP INDEX IF EXISTS order_schema.ix_order_items_product_id;

-- =============================
-- 3. Drop Tables (reverse order because of dependencies)
-- =============================
DROP TABLE IF EXISTS order_schema.order_items CASCADE;
DROP TABLE IF EXISTS order_schema.orders CASCADE;
DROP TABLE IF EXISTS order_schema.order_status CASCADE;

-- =============================
-- 4. Drop Function
-- =============================
DROP FUNCTION IF EXISTS update_modified_at_version() CASCADE;

-- =============================
-- 5. Drop Roles
-- =============================
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_order_writer') THEN
    DROP ROLE svc_order_writer;
  END IF;
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_order_reader') THEN
    DROP ROLE svc_order_reader;
  END IF;
END$$;

-- =============================
-- 6. Drop Schema
-- =============================
DROP SCHEMA IF EXISTS order_schema CASCADE;

-- =============================
-- 7. Drop Extension (optional, only if you don’t want it anymore)
-- =============================
DROP EXTENSION IF EXISTS "uuid-ossp";
