-- V1__create_schema_up.sql
-- Production-safe schema creation for cart_db

CREATE SCHEMA IF NOT EXISTS cart;

CREATE TABLE IF NOT EXISTS cart.cart_items (
    cart_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(100),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_cart_items_user_id ON cart.cart_items (user_id);
