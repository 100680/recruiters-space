-- V1__create_schema_up.sql
-- Production-safe schema creation for user_db
-- Assumes database "user_db" already exists

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS "user";

CREATE TABLE IF NOT EXISTS "user".users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    correlation_id UUID,
    service_origin VARCHAR(50),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_users_email ON "user".users (email);
