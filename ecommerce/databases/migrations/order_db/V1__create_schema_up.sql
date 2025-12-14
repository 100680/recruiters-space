-- V1__create_schema_up.sql
-- Production-safe schema creation for order_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS "order";

CREATE TABLE IF NOT EXISTS "order".order_status (
    status_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS "order".orders (
    order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    order_date TIMESTAMPTZ DEFAULT NOW(),
    total_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    correlation_id UUID,
    service_origin VARCHAR(50),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_orders_user_id ON "order".orders (user_id);
CREATE INDEX IF NOT EXISTS ix_orders_order_date ON "order".orders (order_date);

CREATE TABLE IF NOT EXISTS "order".order_items (
    order_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_order_items_order_id ON "order".order_items (order_id);
