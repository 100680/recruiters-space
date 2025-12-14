-- V1__create_schema_down.sql
-- Rollback for payment_db

DROP INDEX IF EXISTS payment.ix_payments_order_id;

DROP TABLE IF EXISTS payment.payments CASCADE;
DROP TABLE IF EXISTS payment.payment_method_types CASCADE;

DROP SCHEMA IF EXISTS payment CASCADE;
