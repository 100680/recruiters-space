-- payment_db/create_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS payment;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status_enum') THEN
    CREATE TYPE payment.payment_status_enum AS ENUM ('pending','authorized','captured','failed','refunded','voided');
  END IF;
END$$;

CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS payment.payment_method_types (
  payment_method_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_type VARCHAR(50) NOT NULL,
  method_name VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_method_types_name_active ON payment.payment_method_types (lower(method_name)) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payment_method_types_is_deleted ON payment.payment_method_types (is_deleted);


-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'payment' AND table_name = 'payment_method_types'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_payment_method_types_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_payment_method_types_update ON payment.payment_method_types';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_payment_method_types_update 
              BEFORE UPDATE ON payment.payment_method_types 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS payment.payments (
  payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL,
  payment_method_type_id BIGINT NOT NULL REFERENCES payment.payment_method_types(payment_method_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  payment_date TIMESTAMPTZ,
  amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  payment_status payment.payment_status_enum NOT NULL DEFAULT 'pending',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE INDEX IF NOT EXISTS ix_payments_order_status_created ON payment.payments (order_id, payment_status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_payments_is_deleted ON payment.payments (is_deleted);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'payment' AND table_name = 'payments'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_payments_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_payments_update ON payment.payments';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_payments_update 
              BEFORE UPDATE ON payment.payments 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_writer') THEN CREATE ROLE svc_payment_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_reader') THEN CREATE ROLE svc_payment_reader NOLOGIN; END IF;
END$$;

GRANT USAGE ON SCHEMA payment TO svc_payment_reader, svc_payment_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA payment TO svc_payment_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA payment TO svc_payment_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_payment_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT ON TABLES TO svc_payment_reader;
