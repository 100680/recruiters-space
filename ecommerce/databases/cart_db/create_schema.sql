-- cart_db/create_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS cart;

CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS cart.cart_items (
  cart_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  session_id VARCHAR(100),
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL CHECK (quantity > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  CONSTRAINT chk_cart_identity CHECK (user_id IS NOT NULL OR session_id IS NOT NULL)
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='cart' AND indexname='uq_cart_user_product_active') THEN
    EXECUTE 'CREATE UNIQUE INDEX uq_cart_user_product_active ON cart.cart_items (user_id, product_id) WHERE session_id IS NULL AND is_deleted = false;';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='cart' AND indexname='uq_cart_session_product_active') THEN
    EXECUTE 'CREATE UNIQUE INDEX uq_cart_session_product_active ON cart.cart_items (session_id, product_id) WHERE user_id IS NULL AND is_deleted = false;';
  END IF;
END$$;


CREATE INDEX IF NOT EXISTS ix_cart_items_user_id ON cart.cart_items (user_id);
CREATE INDEX IF NOT EXISTS ix_cart_items_session_id ON cart.cart_items (session_id);
CREATE INDEX IF NOT EXISTS ix_cart_items_is_deleted ON cart.cart_items (is_deleted);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'cart' AND table_name = 'cart_items'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_cart_items_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_cart_items_update ON cart.cart_items';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_cart_items_update 
              BEFORE UPDATE ON cart.cart_items 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;


DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_cart_writer') THEN CREATE ROLE svc_cart_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_cart_reader') THEN CREATE ROLE svc_cart_reader NOLOGIN; END IF;
END$$;

GRANT USAGE ON SCHEMA cart TO svc_cart_reader, svc_cart_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA cart TO svc_cart_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA cart TO svc_cart_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA cart GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_cart_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA cart GRANT SELECT ON TABLES TO svc_cart_reader;
