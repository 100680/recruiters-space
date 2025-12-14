-- user_db/create_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS user_schema;

-- Common trigger function for row versioning and modified timestamp
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Users table
CREATE TABLE IF NOT EXISTS user_schema.users (
  user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  address VARCHAR(255),
  phone VARCHAR(20),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$')
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active 
  ON user_schema.users (lower(email)) WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS ix_users_is_deleted 
  ON user_schema.users (is_deleted);

-- Safely drop & recreate trigger only if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'user_schema' AND table_name = 'users'
  ) THEN
    -- Drop trigger if exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger 
      WHERE tgname = 'trg_users_update'
    ) THEN
      EXECUTE 'DROP TRIGGER trg_users_update ON user_schema.users';
    END IF;

    -- Recreate trigger
    EXECUTE 'CREATE TRIGGER trg_users_update 
              BEFORE UPDATE ON user_schema.users 
              FOR EACH ROW EXECUTE FUNCTION update_modified_at_version()';
  END IF;
END$$;

-- RLS
ALTER TABLE user_schema.users ENABLE ROW LEVEL SECURITY;

-- Policy: SELECT
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_select_policy'
      AND schemaname = 'user_schema'
      AND tablename = 'users'
  ) THEN
    EXECUTE $pol$
      CREATE POLICY user_select_policy ON user_schema.users 
      FOR SELECT USING (
        current_role IN ('svc_user_writer','svc_user_reader') OR
        (current_setting('jwt.claims.user_id', true) IS NOT NULL 
         AND user_id = (current_setting('jwt.claims.user_id', true))::bigint)
      )
    $pol$;
  END IF;
END$$;

-- Policy: INSERT
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_insert_policy'
      AND schemaname = 'user_schema'
      AND tablename = 'users'
  ) THEN
    EXECUTE $pol$
      CREATE POLICY user_insert_policy ON user_schema.users 
      FOR INSERT WITH CHECK (current_role = 'svc_user_writer')
    $pol$;
  END IF;
END$$;

-- Policy: UPDATE
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_update_policy'
      AND schemaname = 'user_schema'
      AND tablename = 'users'
  ) THEN
    EXECUTE $pol$
      CREATE POLICY user_update_policy ON user_schema.users 
      FOR UPDATE USING (
        current_role = 'svc_user_writer' OR 
        (current_setting('jwt.claims.user_id', true) IS NOT NULL 
         AND user_id = (current_setting('jwt.claims.user_id', true))::bigint)
      )
    $pol$;
  END IF;
END$$;

-- Policy: DELETE
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_delete_policy'
      AND schemaname = 'user_schema'
      AND tablename = 'users'
  ) THEN
    EXECUTE $pol$
      CREATE POLICY user_delete_policy ON user_schema.users 
      FOR DELETE USING (current_role = 'svc_user_writer')
    $pol$;
  END IF;
END$$;

-- Roles & Grants
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_user_writer') THEN CREATE ROLE svc_user_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_user_reader') THEN CREATE ROLE svc_user_reader NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_reporting_ro') THEN CREATE ROLE svc_reporting_ro NOLOGIN; END IF;
END$$;

GRANT USAGE ON SCHEMA user_schema TO svc_user_reader, svc_user_writer, svc_reporting_ro;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA user_schema TO svc_user_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA user_schema TO svc_user_reader, svc_reporting_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_user_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT SELECT ON TABLES TO svc_user_reader, svc_reporting_ro;
