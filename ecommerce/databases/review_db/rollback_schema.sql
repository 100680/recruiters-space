-- review_db/rollback_schema.sql

-- Revoke privileges
REVOKE USAGE ON SCHEMA review FROM svc_review_reader, svc_review_writer;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA review FROM svc_review_writer;
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA review FROM svc_review_reader;

-- Drop roles (if exist)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_review_writer') THEN
    DROP ROLE svc_review_writer;
  END IF;
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_review_reader') THEN
    DROP ROLE svc_review_reader;
  END IF;
END$$;

-- Drop trigger (if exists)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_trigger 
    WHERE tgname = 'trg_reviews_update'
  ) THEN
    EXECUTE 'DROP TRIGGER trg_reviews_update ON review.reviews';
  END IF;
END$$;

-- Drop indexes safely
DROP INDEX IF EXISTS review.uq_review_user_product_active;
DROP INDEX IF EXISTS review.ix_reviews_product_id;
DROP INDEX IF EXISTS review.ix_reviews_is_deleted;

-- Drop table
DROP TABLE IF EXISTS review.reviews CASCADE;

-- Drop function
DROP FUNCTION IF EXISTS update_modified_at_version() CASCADE;

-- Drop schema
DROP SCHEMA IF EXISTS review CASCADE;

-- Drop extension (optional: only if nothing else depends on pgcrypto)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_extension WHERE extname='pgcrypto') THEN
    DROP EXTENSION pgcrypto;
  END IF;
END$$;
