-- V1__create_schema_up.sql
-- Production-safe schema creation for product_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS product;

CREATE TABLE IF NOT EXISTS product.categories (
    category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS product.discount_methods (
    discount_method_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    method_name VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS product.products (
    product_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    category_id BIGINT NOT NULL,
    discount_method_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    correlation_id UUID,
    service_origin VARCHAR(50),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_products_category_id ON product.products (category_id);
CREATE INDEX IF NOT EXISTS ix_products_discount_method_id ON product.products (discount_method_id);
