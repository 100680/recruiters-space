-- order_db/create_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS "order_schema";

CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS order_schema.order_status (
  status_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  status_name VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_order_status_name_active ON order_schema.order_status (lower(status_name)) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_order_status_is_deleted ON order_schema.order_status (is_deleted);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'order_schema' AND table_name = 'orders'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_orders_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_orders_update ON order_schema.orders';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_orders_update
              BEFORE UPDATE ON order_schema.orders
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;



CREATE TABLE IF NOT EXISTS order_schema.orders (
  order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  status_id BIGINT NOT NULL REFERENCES order_schema.order_status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_orders_user_date ON order_schema.orders (user_id, order_date DESC);
CREATE INDEX IF NOT EXISTS ix_orders_status_id ON order_schema.orders (status_id);
CREATE INDEX IF NOT EXISTS ix_orders_is_deleted ON order_schema.orders (is_deleted);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'order_schema' AND table_name = 'orders'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_orders_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_orders_update ON order_schema.orders';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_orders_update 
              BEFORE UPDATE ON order_schema.orders 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS order_schema.order_items (
  order_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES order_schema.orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL CHECK (quantity > 0),
  price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  discount_method_id BIGINT,
  discount_value NUMERIC(10,2),
  final_price NUMERIC(10,2) NOT NULL CHECK (final_price >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='order_schema' AND indexname='uq_order_item_active') THEN
    EXECUTE 'CREATE UNIQUE INDEX uq_order_item_active ON order_schema.order_items (order_id, product_id) WHERE is_deleted = false;';
  END IF;
END$$;

CREATE INDEX IF NOT EXISTS ix_order_items_order_id ON order_schema.order_items (order_id);
CREATE INDEX IF NOT EXISTS ix_order_items_product_id ON order_schema.order_items (product_id);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'order_schema' AND table_name = 'order_items'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_order_items_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_order_items_update ON order_schema.order_items';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_order_items_update 
              BEFORE UPDATE ON order_schema.order_items 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_order_writer') THEN CREATE ROLE svc_order_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_order_reader') THEN CREATE ROLE svc_order_reader NOLOGIN; END IF;
END$$;

GRANT USAGE ON SCHEMA order_schema TO svc_order_reader, svc_order_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA order_schema TO svc_order_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA order_schema TO svc_order_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA order_schema GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_order_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA order_schema GRANT SELECT ON TABLES TO svc_order_reader;
