-- V1__create_schema_down.sql
-- Rollback for review_db

DROP INDEX IF EXISTS review.ix_reviews_product_id;

DROP TABLE IF EXISTS review.reviews CASCADE;

DROP SCHEMA IF EXISTS review CASCADE;
