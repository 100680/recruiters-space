-- V1__create_schema_down.sql
-- Rollback for user_db schema changes from V1__create_schema_up.sql

DROP INDEX IF EXISTS "user".ix_users_email;

DROP TABLE IF EXISTS "user".users CASCADE;

DROP SCHEMA IF EXISTS "user" CASCADE;

DROP EXTENSION IF EXISTS "uuid-ossp";
