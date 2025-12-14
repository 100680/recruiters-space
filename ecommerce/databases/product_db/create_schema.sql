-- product_db/create_schema_updated.sql
-- Updated PostgreSQL Products Schema with all issues fixed

-- Create required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Create schema
CREATE SCHEMA IF NOT EXISTS product;

-- Enhanced trigger function with validation and audit protection
CREATE OR REPLACE FUNCTION product.update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  -- Validate that required fields haven't been nullified
  IF TG_TABLE_NAME = 'products' AND (NEW.name IS NULL OR NEW.price IS NULL OR NEW.stock IS NULL) THEN
    RAISE EXCEPTION 'Required fields (name, price, stock) cannot be null';
  END IF;
  
  IF TG_TABLE_NAME = 'categories' AND NEW.name IS NULL THEN
    RAISE EXCEPTION 'Category name cannot be null';
  END IF;
  
  IF TG_TABLE_NAME = 'discount_methods' AND NEW.method_name IS NULL THEN
    RAISE EXCEPTION 'Discount method name cannot be null';
  END IF;
  
  -- Update modification tracking
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  
  -- Protect audit fields from modification
  NEW.created_at := OLD.created_at;
  
  -- Protect correlation_id for products table
  IF TG_TABLE_NAME = 'products' THEN
    NEW.correlation_id := OLD.correlation_id;
  END IF;
  
  -- Set modified_by if app.current_user is available
  IF current_setting('app.current_user', true) IS NOT NULL THEN
    IF TG_TABLE_NAME = 'products' THEN
      NEW.modified_by := current_setting('app.current_user', true);
    END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Categories table with enhanced constraints
CREATE TABLE IF NOT EXISTS product.categories (
  category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL CHECK (trim(name) != ''),
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by VARCHAR(100),
  modified_by VARCHAR(100),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
  -- Ensure deleted_at consistency
  CONSTRAINT chk_categories_deleted_at_consistency 
    CHECK ((is_deleted = false AND deleted_at IS NULL) OR 
           (is_deleted = true AND deleted_at IS NOT NULL))
);

-- Categories indexes
CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_name_active 
  ON product.categories (lower(trim(name))) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_categories_is_deleted 
  ON product.categories (is_deleted);
CREATE INDEX IF NOT EXISTS ix_categories_name_gin 
  ON product.categories USING gin(to_tsvector('english', name)) WHERE NOT is_deleted;

-- Products table with enhanced precision and audit fields
CREATE TABLE IF NOT EXISTS product.products (
  product_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL CHECK (trim(name) != ''),
  description TEXT,
  price NUMERIC(15,4) NOT NULL CHECK (price >= 0 AND price <= 999999999999.9999), -- Enhanced precision and max limit
  stock INT NOT NULL CHECK (stock >= 0),
  reorder_level INT NOT NULL DEFAULT 10 CHECK (reorder_level >= 0),
  category_id BIGINT NOT NULL REFERENCES product.categories(category_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  image_url VARCHAR(500), -- Increased length for longer URLs
  sku VARCHAR(50) UNIQUE, -- Added SKU(Stock Keeping Unit) field. SKUs are usually human-readable codes like TSHIRT-BLUE-M that uniquely identify a product variant.
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  created_by VARCHAR(100),
  modified_by VARCHAR(100),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
  -- Ensure deleted_at consistency
  CONSTRAINT chk_products_deleted_at_consistency 
    CHECK ((is_deleted = false AND deleted_at IS NULL) OR 
           (is_deleted = true AND deleted_at IS NOT NULL)),
           
  -- Ensure reorder level makes business sense
  CONSTRAINT chk_products_reorder_level_reasonable
    CHECK (reorder_level <= stock * 0.5 OR stock = 0)
);

-- Products indexes for performance
CREATE INDEX IF NOT EXISTS ix_products_category_id 
  ON product.products (category_id) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_products_is_deleted 
  ON product.products (is_deleted);
CREATE INDEX IF NOT EXISTS ix_products_name_gin 
  ON product.products USING gin(to_tsvector('english', name)) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_products_price 
  ON product.products (price) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_products_low_stock 
  ON product.products (stock, reorder_level) WHERE stock <= reorder_level AND NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_products_active_by_category 
  ON product.products (category_id, name) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_products_sku 
  ON product.products (sku) WHERE sku IS NOT NULL;

-- Discount methods table
CREATE TABLE IF NOT EXISTS product.discount_methods (
  discount_method_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_name VARCHAR(50) NOT NULL CHECK (trim(method_name) != ''),
  description TEXT,
  is_percentage BOOLEAN NOT NULL DEFAULT FALSE, -- Track if discount is percentage or fixed amount
  max_discount_value NUMERIC(15,4), -- Maximum allowed discount for this method
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by VARCHAR(100),
  modified_by VARCHAR(100),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
  -- Ensure deleted_at consistency
  CONSTRAINT chk_discount_methods_deleted_at_consistency 
    CHECK ((is_deleted = false AND deleted_at IS NULL) OR 
           (is_deleted = true AND deleted_at IS NOT NULL))
);

-- Discount methods indexes
CREATE UNIQUE INDEX IF NOT EXISTS uq_discount_methods_name_active 
  ON product.discount_methods (lower(trim(method_name))) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_discount_methods_is_deleted 
  ON product.discount_methods (is_deleted);

-- Product discounts table with enhanced validation
CREATE TABLE IF NOT EXISTS product.product_discounts (
  product_discount_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES product.products(product_id) ON UPDATE CASCADE ON DELETE CASCADE,
  discount_method_id BIGINT NOT NULL REFERENCES product.discount_methods(discount_method_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  discount_value NUMERIC(15,4) NOT NULL CHECK (discount_value >= 0),
  start_date TIMESTAMPTZ NOT NULL,
  end_date TIMESTAMPTZ,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  discount_period tstzrange GENERATED ALWAYS AS (tstzrange(start_date, COALESCE(end_date, 'infinity'::timestamptz), '[)')) STORED,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_by VARCHAR(100),
  modified_by VARCHAR(100),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
  -- Ensure valid date range
  CONSTRAINT chk_product_discounts_valid_dates 
    CHECK (end_date IS NULL OR end_date > start_date),
    
  -- Ensure reasonable discount values
  CONSTRAINT chk_product_discounts_reasonable_value 
    CHECK (discount_value <= 100000), -- Adjust based on business needs
    
  -- Ensure deleted_at consistency
  CONSTRAINT chk_product_discounts_deleted_at_consistency 
    CHECK ((is_deleted = false AND deleted_at IS NULL) OR 
           (is_deleted = true AND deleted_at IS NOT NULL))
);

-- Product discounts indexes
CREATE INDEX IF NOT EXISTS ix_product_discounts_product_id 
  ON product.product_discounts (product_id);
CREATE INDEX IF NOT EXISTS ix_product_discounts_active 
  ON product.product_discounts (active) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_product_discounts_is_deleted 
  ON product.product_discounts (is_deleted);
CREATE INDEX IF NOT EXISTS ix_product_discounts_period_gist 
  ON product.product_discounts USING gist (discount_period);
CREATE INDEX IF NOT EXISTS ix_product_discounts_dates 
  ON product.product_discounts (start_date, end_date) WHERE active AND NOT is_deleted;
CREATE INDEX IF NOT EXISTS ix_active_discounts_by_product 
  ON product.product_discounts (product_id, discount_value) 
  WHERE active AND NOT is_deleted;

-- Price history table for tracking price changes
CREATE TABLE IF NOT EXISTS product.product_price_history (
  price_history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES product.products(product_id) ON DELETE CASCADE,
  old_price NUMERIC(15,4),
  new_price NUMERIC(15,4) NOT NULL,
  price_change_reason VARCHAR(100),
  changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  changed_by VARCHAR(100),
  correlation_id UUID DEFAULT uuid_generate_v4()
);

CREATE INDEX IF NOT EXISTS ix_price_history_product_id 
  ON product.product_price_history (product_id, changed_at DESC);

-- Function to log price changes
CREATE OR REPLACE FUNCTION product.log_price_change()
RETURNS TRIGGER AS $$
BEGIN
  IF OLD.price != NEW.price THEN
    INSERT INTO product.product_price_history 
      (product_id, old_price, new_price, changed_by, correlation_id)
    VALUES 
      (NEW.product_id, OLD.price, NEW.price, NEW.modified_by, NEW.correlation_id);
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Add exclusion constraint to prevent overlapping discounts
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'excl_product_discount_no_overlap') THEN
    EXECUTE '
      ALTER TABLE product.product_discounts
      ADD CONSTRAINT excl_product_discount_no_overlap
      EXCLUDE USING gist (
        product_id WITH =,
        discount_period WITH &&
      )
      WHERE (active AND is_deleted = false);
    ';
  END IF;
END$$;

-- Create all triggers with safe drop/recreate logic
DO $$
DECLARE
  trigger_configs TEXT[][] := ARRAY[
    ['product.categories', 'trg_categories_update', 'update_modified_at_version()'],
    ['product.products', 'trg_products_update', 'update_modified_at_version()'],
    ['product.products', 'trg_products_price_history', 'log_price_change()'],
    ['product.discount_methods', 'trg_discount_methods_update', 'update_modified_at_version()'],
    ['product.product_discounts', 'trg_product_discounts_update', 'update_modified_at_version()']
  ];
  config TEXT[];
BEGIN
  FOREACH config SLICE 1 IN ARRAY trigger_configs
  LOOP
    -- Drop trigger if exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger t
      JOIN pg_class c ON t.tgrelid = c.oid
      JOIN pg_namespace n ON c.relnamespace = n.oid
      WHERE t.tgname = config[2]
      AND n.nspname || '.' || c.relname = config[1]
    ) THEN
      EXECUTE format('DROP TRIGGER %s ON %s', config[2], config[1]);
    END IF;

    -- Create trigger
    IF config[2] = 'trg_products_price_history' THEN
      EXECUTE format('CREATE TRIGGER %s AFTER UPDATE ON %s FOR EACH ROW EXECUTE FUNCTION product.%s', 
        config[2], config[1], config[3]);
    ELSE
      EXECUTE format('CREATE TRIGGER %s BEFORE UPDATE ON %s FOR EACH ROW EXECUTE FUNCTION product.%s', 
        config[2], config[1], config[3]);
    END IF;
  END LOOP;
END$$;

-- Utility functions for business logic
CREATE OR REPLACE FUNCTION product.get_active_discount(p_product_id BIGINT)
RETURNS TABLE(discount_value NUMERIC(15,4), method_name VARCHAR(50)) AS $$
  SELECT pd.discount_value, dm.method_name
  FROM product.product_discounts pd
  JOIN product.discount_methods dm ON pd.discount_method_id = dm.discount_method_id
  WHERE pd.product_id = p_product_id 
    AND pd.active 
    AND NOT pd.is_deleted
    AND NOT dm.is_deleted
    AND pd.start_date <= NOW() 
    AND (pd.end_date IS NULL OR pd.end_date > NOW())
  ORDER BY pd.discount_value DESC
  LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION product.get_effective_price(p_product_id BIGINT)
RETURNS NUMERIC(15,4) AS $$
DECLARE
  base_price NUMERIC(15,4);
  discount_info RECORD;
  effective_price NUMERIC(15,4);
BEGIN
  -- Get base price
  SELECT price INTO base_price 
  FROM product.products 
  WHERE product_id = p_product_id AND NOT is_deleted;
  
  IF base_price IS NULL THEN
    RETURN NULL;
  END IF;
  
  -- Get active discount
  SELECT * INTO discount_info 
  FROM product.get_active_discount(p_product_id);
  
  IF discount_info.discount_value IS NULL THEN
    RETURN base_price;
  END IF;
  
  -- Calculate effective price based on discount method
  SELECT CASE 
    WHEN dm.is_percentage THEN 
      base_price * (1 - LEAST(discount_info.discount_value, 100) / 100.0)
    ELSE 
      GREATEST(base_price - discount_info.discount_value, 0)
  END INTO effective_price
  FROM product.discount_methods dm
  WHERE dm.method_name = discount_info.method_name;
  
  RETURN COALESCE(effective_price, base_price);
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- Health monitoring views
CREATE OR REPLACE VIEW product.product_health AS
SELECT 
  COUNT(*) as total_products,
  COUNT(*) FILTER (WHERE stock = 0) as out_of_stock,
  COUNT(*) FILTER (WHERE stock <= reorder_level) as low_stock,
  COUNT(*) FILTER (WHERE is_deleted) as deleted_products,
  AVG(price) as average_price,
  COUNT(DISTINCT category_id) as active_categories
FROM product.products;

CREATE OR REPLACE VIEW product.discount_health AS
SELECT 
  COUNT(*) as total_discounts,
  COUNT(*) FILTER (WHERE active AND start_date <= NOW() AND (end_date IS NULL OR end_date > NOW())) as active_discounts,
  COUNT(*) FILTER (WHERE active AND end_date < NOW()) as expired_but_active,
  COUNT(*) FILTER (WHERE is_deleted) as deleted_discounts
FROM product.product_discounts;

-- Create roles with enhanced security
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_product_writer') THEN 
    CREATE ROLE svc_product_writer NOLOGIN; 
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_product_reader') THEN 
    CREATE ROLE svc_product_reader NOLOGIN; 
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_product_admin') THEN 
    CREATE ROLE svc_product_admin NOLOGIN; 
  END IF;
END$$;

-- Grant permissions
GRANT USAGE ON SCHEMA product TO svc_product_reader, svc_product_writer, svc_product_admin;

-- Reader permissions
GRANT SELECT ON ALL TABLES IN SCHEMA product TO svc_product_reader;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA product TO svc_product_reader;

-- Writer permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA product TO svc_product_writer;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA product TO svc_product_writer;
GRANT EXECUTE ON FUNCTION product.get_active_discount(BIGINT) TO svc_product_writer;
GRANT EXECUTE ON FUNCTION product.get_effective_price(BIGINT) TO svc_product_writer;

-- Admin permissions
GRANT ALL ON SCHEMA product TO svc_product_admin;
GRANT ALL ON ALL TABLES IN SCHEMA product TO svc_product_admin;
GRANT ALL ON ALL SEQUENCES IN SCHEMA product TO svc_product_admin;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA product TO svc_product_admin;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT SELECT ON TABLES TO svc_product_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT SELECT ON SEQUENCES TO svc_product_reader;

ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_product_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT USAGE, SELECT ON SEQUENCES TO svc_product_writer;

ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON TABLES TO svc_product_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON SEQUENCES TO svc_product_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA product GRANT ALL ON FUNCTIONS TO svc_product_admin;

-- Add helpful comments
COMMENT ON SCHEMA product IS 'Product catalog schema with categories, products, and discount management';
COMMENT ON TABLE product.products IS 'Core product catalog with pricing, inventory, and audit trails';
COMMENT ON TABLE product.product_discounts IS 'Time-based discount rules with overlap prevention';
COMMENT ON TABLE product.product_price_history IS 'Complete price change audit trail';
COMMENT ON COLUMN product.products.correlation_id IS 'UUID for request tracing across microservices';
COMMENT ON COLUMN product.products.reorder_level IS 'Stock level that triggers reorder alerts';
COMMENT ON FUNCTION product.get_effective_price(BIGINT) IS 'Calculate final price including active discounts';

-- Final success message
DO $$
BEGIN
  RAISE NOTICE 'Product schema created successfully with all enhancements applied!';
END$$;