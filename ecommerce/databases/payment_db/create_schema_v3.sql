-- payment_db/create_schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS payment;

-- Function to update modified_at and row_version
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Currency Codes reference table (ISO 4217)
CREATE TABLE IF NOT EXISTS payment.currency_codes (
  currency_code_id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  currency_code CHAR(3) NOT NULL,
  currency_name VARCHAR(100) NOT NULL,
  currency_symbol VARCHAR(10),
  numeric_code CHAR(3),
  minor_unit SMALLINT DEFAULT 2,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  display_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_currency_codes_code ON payment.currency_codes (UPPER(currency_code)) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS ix_currency_codes_active ON payment.currency_codes (is_active);

DROP TRIGGER IF EXISTS trg_currency_codes_update ON payment.currency_codes;
CREATE TRIGGER trg_currency_codes_update 
  BEFORE UPDATE ON payment.currency_codes 
  FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Payment Status reference table
CREATE TABLE IF NOT EXISTS payment.payment_statuses (
  payment_status_id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  status_code VARCHAR(20) NOT NULL,
  status_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
  display_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_statuses_code ON payment.payment_statuses (UPPER(status_code)) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS ix_payment_statuses_active ON payment.payment_statuses (is_active);

DROP TRIGGER IF EXISTS trg_payment_statuses_update ON payment.payment_statuses;
CREATE TRIGGER trg_payment_statuses_update 
  BEFORE UPDATE ON payment.payment_statuses 
  FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Payment Method Types reference table
CREATE TABLE IF NOT EXISTS payment.payment_method_types (
  payment_method_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_type VARCHAR(50) NOT NULL,
  method_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  display_order SMALLINT NOT NULL DEFAULT 0,
  requires_card_details BOOLEAN NOT NULL DEFAULT FALSE,
  requires_bank_details BOOLEAN NOT NULL DEFAULT FALSE,
  processing_fee_percentage NUMERIC(5,2) DEFAULT 0.00 CHECK (processing_fee_percentage >= 0 AND processing_fee_percentage <= 100),
  min_amount NUMERIC(10,2) DEFAULT 0.00 CHECK (min_amount >= 0),
  max_amount NUMERIC(10,2) CHECK (max_amount IS NULL OR max_amount >= min_amount),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_method_types_name_active ON payment.payment_method_types (LOWER(method_name)) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payment_method_types_type ON payment.payment_method_types (method_type) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payment_method_types_active ON payment.payment_method_types (is_active) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payment_method_types_is_deleted ON payment.payment_method_types (is_deleted);

DROP TRIGGER IF EXISTS trg_payment_method_types_update ON payment.payment_method_types;
CREATE TRIGGER trg_payment_method_types_update 
  BEFORE UPDATE ON payment.payment_method_types 
  FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Payments table
CREATE TABLE IF NOT EXISTS payment.payments (
  payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL,
  payment_method_type_id BIGINT NOT NULL,
  payment_status_id SMALLINT NOT NULL,
  currency_code_id SMALLINT NOT NULL,
  payment_date TIMESTAMPTZ,
  amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  processing_fee NUMERIC(10,2) DEFAULT 0.00 CHECK (processing_fee >= 0),
  net_amount NUMERIC(10,2) GENERATED ALWAYS AS (amount - processing_fee) STORED,
  transaction_reference VARCHAR(100),
  gateway_response TEXT,
  failure_reason VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
  CONSTRAINT fk_payments_currency_code 
    FOREIGN KEY (currency_code_id) 
    REFERENCES payment.currency_codes(currency_code_id) 
    ON UPDATE RESTRICT ON DELETE RESTRICT,
    
  CONSTRAINT fk_payments_payment_method_type 
    FOREIGN KEY (payment_method_type_id) 
    REFERENCES payment.payment_method_types(payment_method_type_id) 
    ON UPDATE RESTRICT ON DELETE RESTRICT,
    
  CONSTRAINT fk_payments_payment_status 
    FOREIGN KEY (payment_status_id) 
    REFERENCES payment.payment_statuses(payment_status_id) 
    ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_payments_order_status_created ON payment.payments (order_id, payment_status_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_payments_correlation_id ON payment.payments (correlation_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payments_transaction_ref ON payment.payments (transaction_reference) WHERE is_deleted = false AND transaction_reference IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_payments_status ON payment.payments (payment_status_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payments_payment_date ON payment.payments (payment_date) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payments_currency ON payment.payments (currency_code_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS ix_payments_is_deleted ON payment.payments (is_deleted);

DROP TRIGGER IF EXISTS trg_payments_update ON payment.payments;
CREATE TRIGGER trg_payments_update 
  BEFORE UPDATE ON payment.payments 
  FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Payment Status History table
CREATE TABLE IF NOT EXISTS payment.payment_status_history (
  payment_status_history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  payment_id BIGINT NOT NULL,
  previous_status_id SMALLINT,
  new_status_id SMALLINT NOT NULL,
  changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  changed_by VARCHAR(100),
  reason VARCHAR(500),
  correlation_id UUID,
  
  CONSTRAINT fk_payment_status_history_payment 
    FOREIGN KEY (payment_id) 
    REFERENCES payment.payments(payment_id) 
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_payment_status_history_previous_status 
    FOREIGN KEY (previous_status_id) 
    REFERENCES payment.payment_statuses(payment_status_id) 
    ON UPDATE RESTRICT ON DELETE RESTRICT,
    
  CONSTRAINT fk_payment_status_history_new_status 
    FOREIGN KEY (new_status_id) 
    REFERENCES payment.payment_statuses(payment_status_id) 
    ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_payment_status_history_payment ON payment.payment_status_history (payment_id, changed_at DESC);
CREATE INDEX IF NOT EXISTS ix_payment_status_history_status ON payment.payment_status_history (new_status_id, changed_at DESC);

-- Create roles (ignore error if they already exist)
DO $create_roles$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'svc_payment_writer') THEN
    CREATE ROLE svc_payment_writer NOLOGIN;
  END IF;
  
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'svc_payment_reader') THEN
    CREATE ROLE svc_payment_reader NOLOGIN;
  END IF;
END
$create_roles$;

-- Grant permissions
GRANT USAGE ON SCHEMA payment TO svc_payment_reader, svc_payment_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA payment TO svc_payment_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA payment TO svc_payment_reader;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA payment TO svc_payment_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_payment_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT ON TABLES TO svc_payment_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT USAGE ON SEQUENCES TO svc_payment_writer;

-- Insert common currency codes
INSERT INTO payment.currency_codes (currency_code, currency_name, currency_symbol, numeric_code, minor_unit, is_active, display_order) 
VALUES 
  ('USD', 'US Dollar', '$', '840', 2, TRUE, 1),
  ('EUR', 'Euro', '€', '978', 2, TRUE, 2),
  ('GBP', 'British Pound Sterling', '£', '826', 2, TRUE, 3),
  ('JPY', 'Japanese Yen', '¥', '392', 0, TRUE, 4),
  ('CNY', 'Chinese Yuan', '¥', '156', 2, TRUE, 5),
  ('INR', 'Indian Rupee', '₹', '356', 2, TRUE, 6),
  ('CAD', 'Canadian Dollar', '$', '124', 2, TRUE, 7),
  ('AUD', 'Australian Dollar', '$', '036', 2, TRUE, 8),
  ('CHF', 'Swiss Franc', 'CHF', '756', 2, TRUE, 9),
  ('SGD', 'Singapore Dollar', '$', '702', 2, TRUE, 10),
  ('HKD', 'Hong Kong Dollar', '$', '344', 2, TRUE, 11),
  ('NZD', 'New Zealand Dollar', '$', '554', 2, TRUE, 12),
  ('SEK', 'Swedish Krona', 'kr', '752', 2, TRUE, 13),
  ('NOK', 'Norwegian Krone', 'kr', '578', 2, TRUE, 14),
  ('MXN', 'Mexican Peso', '$', '484', 2, TRUE, 15),
  ('ZAR', 'South African Rand', 'R', '710', 2, TRUE, 16),
  ('BRL', 'Brazilian Real', 'R$', '986', 2, TRUE, 17),
  ('KRW', 'South Korean Won', '₩', '410', 0, TRUE, 18),
  ('AED', 'UAE Dirham', 'د.إ', '784', 2, TRUE, 19),
  ('SAR', 'Saudi Riyal', '﷼', '682', 2, TRUE, 20)
ON CONFLICT DO NOTHING;

-- Insert default payment statuses
INSERT INTO payment.payment_statuses (status_code, status_name, description, is_terminal, display_order, is_active) 
VALUES 
  ('PENDING', 'Pending', 'Payment is pending processing', FALSE, 1, TRUE),
  ('AUTHORIZED', 'Authorized', 'Payment is authorized but not captured', FALSE, 2, TRUE),
  ('CAPTURED', 'Captured', 'Payment has been captured successfully', FALSE, 3, TRUE),
  ('FAILED', 'Failed', 'Payment processing failed', TRUE, 4, TRUE),
  ('REFUNDED', 'Refunded', 'Payment has been refunded', TRUE, 5, TRUE),
  ('VOIDED', 'Voided', 'Payment authorization has been voided', TRUE, 6, TRUE)
ON CONFLICT DO NOTHING;

-- Insert sample payment method types
INSERT INTO payment.payment_method_types (method_type, method_name, description, is_active, requires_card_details, processing_fee_percentage, min_amount, max_amount)
VALUES 
  ('CARD', 'Credit Card', 'Credit card payment', TRUE, TRUE, 2.90, 1.00, 999999.99),
  ('CARD', 'Debit Card', 'Debit card payment', TRUE, TRUE, 1.50, 1.00, 999999.99),
  ('WALLET', 'PayPal', 'PayPal digital wallet', TRUE, FALSE, 2.90, 1.00, 10000.00),
  ('WALLET', 'Google Pay', 'Google Pay digital wallet', TRUE, FALSE, 1.50, 1.00, 5000.00),
  ('BANK_TRANSFER', 'Bank Transfer', 'Direct bank transfer', TRUE, TRUE, 0.50, 10.00, 999999.99),
  ('UPI', 'UPI Payment', 'Unified Payments Interface', TRUE, FALSE, 0.00, 1.00, 100000.00)
ON CONFLICT DO NOTHING;

-- Create view for active payments with details
CREATE OR REPLACE VIEW payment.v_payments_detailed AS
SELECT 
  p.payment_id,
  p.order_id,
  p.payment_method_type_id,
  pmt.method_type,
  pmt.method_name AS payment_method_name,
  p.payment_status_id,
  ps.status_code,
  ps.status_name AS payment_status_name,
  ps.is_terminal AS is_terminal_status,
  p.currency_code_id,
  cc.currency_code,
  cc.currency_name,
  cc.currency_symbol,
  p.payment_date,
  p.amount,
  p.processing_fee,
  p.net_amount,
  p.transaction_reference,
  p.correlation_id,
  p.service_origin,
  p.created_at,
  p.modified_at,
  p.is_deleted,
  p.row_version
FROM payment.payments p
INNER JOIN payment.payment_method_types pmt ON p.payment_method_type_id = pmt.payment_method_type_id
INNER JOIN payment.payment_statuses ps ON p.payment_status_id = ps.payment_status_id
INNER JOIN payment.currency_codes cc ON p.currency_code_id = cc.currency_code_id
WHERE p.is_deleted = FALSE;

GRANT SELECT ON payment.v_payments_detailed TO svc_payment_reader, svc_payment_writer;