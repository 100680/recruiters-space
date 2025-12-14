-- payment_db/rollback_schema.sql

-- =============================
-- 1. Drop Triggers
-- =============================
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_payment_method_types_update') THEN
    EXECUTE 'DROP TRIGGER trg_payment_method_types_update ON payment.payment_method_types';
  END IF;

  IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_payments_update') THEN
    EXECUTE 'DROP TRIGGER trg_payments_update ON payment.payments';
  END IF;
END$$;

-- =============================
-- 2. Drop Indexes
-- =============================
DROP INDEX IF EXISTS payment.uq_payment_method_types_name_active;
DROP INDEX IF EXISTS payment.ix_payment_method_types_is_deleted;

DROP INDEX IF EXISTS payment.ix_payments_order_status_created;
DROP INDEX IF EXISTS payment.ix_payments_is_deleted;

-- =============================
-- 3. Drop Tables (reverse dependency order)
-- =============================
DROP TABLE IF EXISTS payment.payments CASCADE;
DROP TABLE IF EXISTS payment.payment_method_types CASCADE;

-- =============================
-- 4. Drop Function
-- =============================
DROP FUNCTION IF EXISTS update_modified_at_version() CASCADE;

-- =============================
-- 5. Drop Type (ENUM)
-- =============================
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status_enum') THEN
    DROP TYPE payment.payment_status_enum;
  END IF;
END$$;

-- =============================
-- 6. Drop Roles
-- =============================
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_writer') THEN
    DROP ROLE svc_payment_writer;
  END IF;
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_reader') THEN
    DROP ROLE svc_payment_reader;
  END IF;
END$$;

-- =============================
-- 7. Drop Schema
-- =============================
DROP SCHEMA IF EXISTS payment CASCADE;

-- =============================
-- 8. Drop Extension (optional: only if not shared)
-- =============================
DROP EXTENSION IF EXISTS "uuid-ossp";
