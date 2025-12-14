-- V1__create_schema_up.sql
-- Production-safe schema creation for review_db

CREATE SCHEMA IF NOT EXISTS review;

CREATE TABLE IF NOT EXISTS review.reviews (
    review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    modified_at TIMESTAMPTZ DEFAULT NOW(),
    row_version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_reviews_product_id ON review.reviews (product_id);
