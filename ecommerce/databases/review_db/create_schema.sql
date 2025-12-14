-- review_db/create_schema.sql
CREATE SCHEMA IF NOT EXISTS review;

-- Enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Common trigger function for row versioning and modified timestamp
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Reviews table
CREATE TABLE IF NOT EXISTS review.reviews (
  review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT gen_random_uuid(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

-- Unique index (user can only leave one active review per product)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='review' AND indexname='uq_review_user_product_active') THEN
    EXECUTE 'CREATE UNIQUE INDEX uq_review_user_product_active ON review.reviews (user_id, product_id) WHERE is_deleted = false;';
  END IF;
END$$;

-- Supporting indexes
CREATE INDEX IF NOT EXISTS ix_reviews_product_id ON review.reviews (product_id);
CREATE INDEX IF NOT EXISTS ix_reviews_is_deleted ON review.reviews (is_deleted);

-- Trigger for automatic modified_at + row_version updates
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'review' AND table_name = 'reviews'
  ) THEN
    -- Drop trigger only if it already exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_reviews_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_reviews_update ON review.reviews';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_reviews_update 
              BEFORE UPDATE ON review.reviews 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

-- Roles & permissions
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_review_writer') THEN CREATE ROLE svc_review_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_review_reader') THEN CREATE ROLE svc_review_reader NOLOGIN; END IF;
END$$;

GRANT USAGE ON SCHEMA review TO svc_review_reader, svc_review_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA review TO svc_review_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA review TO svc_review_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_review_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT SELECT ON TABLES TO svc_review_reader;
