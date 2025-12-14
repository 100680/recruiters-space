-- V1__create_schema_up.sql
-- Production-safe schema creation for payment_db

CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE IF NOT EXISTS payment.payment_method_types (
    payment_method_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    method_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS payment.payments (
    payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method_type_id BIGINT NOT NULL,
    payment_date TIMESTAMPTZ,
    amount NUMERIC(10,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_payments_order_id ON payment.payments (order_id);
