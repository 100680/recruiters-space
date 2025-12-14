-- product_db/rollback_schema.sql

-- Revoke privileges
REVOKE ALL ON SCHEMA product FROM svc_product_reader, svc_product_writer;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA product FROM svc_product_writer, svc_product_reader;

-- Drop roles (if exist)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_product_writer') THEN
    EXECUTE 'DROP ROLE svc_product_writer';
  END IF;

  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_product_reader') THEN
    EXECUTE 'DROP ROLE svc_product_reader';
  END IF;
END$$;

-- Drop triggers
DO $$
BEGIN
  -- categories
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_categories_update') THEN
    EXECUTE 'DROP TRIGGER trg_categories_update ON product.categories';
  END IF;

  -- products
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_products_update') THEN
    EXECUTE 'DROP TRIGGER trg_products_update ON product.products';
  END IF;

  -- discount_methods
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_discount_methods_update') THEN
    EXECUTE 'DROP TRIGGER trg_discount_methods_update ON product.discount_methods';
  END IF;

  -- product_discounts
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_product_discounts_update') THEN
    EXECUTE 'DROP TRIGGER trg_product_discounts_update ON product.product_discounts';
  END IF;
END$$;

-- Drop constraints
ALTER TABLE IF EXISTS product.product_discounts
  DROP CONSTRAINT IF EXISTS excl_product_discount_no_overlap;

-- Drop indexes
DROP INDEX IF EXISTS ix_product_discounts_period_gist;
DROP INDEX IF EXISTS ix_product_discounts_is_deleted;
DROP INDEX IF EXISTS ix_product_discounts_active;
DROP INDEX IF EXISTS ix_product_discounts_product_id;

DROP INDEX IF EXISTS ix_discount_methods_is_deleted;
DROP INDEX IF EXISTS uq_discount_methods_name_active;

DROP INDEX IF EXISTS ix_products_is_deleted;
DROP INDEX IF EXISTS ix_products_category_id;

DROP INDEX IF EXISTS ix_categories_is_deleted;
DROP INDEX IF EXISTS uq_categories_name_active;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS product.product_discounts CASCADE;
DROP TABLE IF EXISTS product.discount_methods CASCADE;
DROP TABLE IF EXISTS product.products CASCADE;
DROP TABLE IF EXISTS product.categories CASCADE;

-- Drop function
DROP FUNCTION IF EXISTS update_modified_at_version();

-- Finally drop schema
DROP SCHEMA IF EXISTS product CASCADE;

-- Optionally drop extensions (only if not needed by other schemas)
-- DROP EXTENSION IF EXISTS btree_gist;
-- DROP EXTENSION IF EXISTS "uuid-ossp";
