-- user_db/drop_schema.sql

DO $$
BEGIN
  -- Drop policies if they exist
  IF EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_select_policy' AND schemaname = 'user_schema'
  ) THEN
    EXECUTE 'DROP POLICY user_select_policy ON user_schema.users';
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_insert_policy' AND schemaname = 'user_schema'
  ) THEN
    EXECUTE 'DROP POLICY user_insert_policy ON user_schema.users';
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_update_policy' AND schemaname = 'user_schema'
  ) THEN
    EXECUTE 'DROP POLICY user_update_policy ON user_schema.users';
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_policies 
    WHERE policyname = 'user_delete_policy' AND schemaname = 'user_schema'
  ) THEN
    EXECUTE 'DROP POLICY user_delete_policy ON user_schema.users';
  END IF;
END$$;

-- Drop trigger if exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_users_update'
  ) THEN
    EXECUTE 'DROP TRIGGER trg_users_update ON user_schema.users';
  END IF;
END$$;

-- Drop table
DROP TABLE IF EXISTS user_schema.users CASCADE;

-- Drop function
DROP FUNCTION IF EXISTS update_modified_at_version() CASCADE;

-- Drop schema
DROP SCHEMA IF EXISTS user_schema CASCADE;

-- Drop roles
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_user_writer') THEN
    EXECUTE 'DROP ROLE svc_user_writer';
  END IF;
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_user_reader') THEN
    EXECUTE 'DROP ROLE svc_user_reader';
  END IF;
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_reporting_ro') THEN
    EXECUTE 'DROP ROLE svc_reporting_ro';
  END IF;
END$$;
