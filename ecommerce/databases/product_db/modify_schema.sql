-- product_db/modify_schema.sql
-- 1) Add sku column and unique active constraint (nullable + backfill)
ALTER TABLE product.products ADD COLUMN IF NOT EXISTS sku VARCHAR(64);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='product' AND indexname='uq_products_sku_active') THEN
    EXECUTE 'CREATE UNIQUE INDEX uq_products_sku_active ON product.products (sku) WHERE (sku IS NOT NULL AND is_deleted = false);';
  END IF;
END$$;

-- 2) Install trigram and add GIN index for name fuzzy search (optional)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS ix_products_name_trgm ON product.products USING gin (name gin_trgm_ops);
