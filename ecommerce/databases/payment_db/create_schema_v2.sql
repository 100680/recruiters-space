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

-- Payment Status reference table
CREATE TABLE IF NOT EXISTS payment.payment_statuses (
  payment_status_id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  status_code VARCHAR(20) NOT NULL,
  status_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  is_terminal BOOLEAN NOT NULL DEFAULT FALSE, -- Indicates if this is a final state
  display_order SMALLINT NOT NULL DEFAULT 0, -- For UI ordering
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_statuses_code ON payment.payment_statuses (UPPER(status_code)) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS ix_payment_statuses_active ON payment.payment_statuses (is_active);

-- Trigger for payment_statuses
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'payment' AND table_name = 'payment_statuses'
  ) THEN
    DROP TRIGGER IF EXISTS trg_payment_statuses_update ON payment.payment_statuses;
    CREATE TRIGGER trg_payment_statuses_update 
      BEFORE UPDATE ON payment.payment_statuses 
      FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();
  END IF;
END$$;

-- Payment Method Types reference table
CREATE TABLE IF NOT EXISTS payment.payment_method_types (
  payment_method_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_type VARCHAR(50) NOT NULL, -- e.g., 'CARD', 'WALLET', 'BANK_TRANSFER', 'UPI'
  method_name VARCHAR(50) NOT NULL, -- e.g., 'Credit Card', 'PayPal', 'Google Pay'
  description VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  display_order SMALLINT NOT NULL DEFAULT 0, -- For UI ordering
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

-- Trigger for payment_method_types
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'payment' AND table_name = 'payment_method_types'
  ) THEN
    DROP TRIGGER IF EXISTS trg_payment_method_types_update ON payment.payment_method_types;
    CREATE TRIGGER trg_payment_method_types_update 
      BEFORE UPDATE ON payment.payment_method_types 
      FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();
  END IF;
END$$;

-- Payments table
CREATE TABLE IF NOT EXISTS payment.payments (
  payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL,
  payment_method_type_id BIGINT NOT NULL,
  payment_status_id SMALLINT NOT NULL,
  payment_date TIMESTAMPTZ,
  amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  currency_code VARCHAR(3) DEFAULT 'USD', -- ISO 4217 currency code
  processing_fee NUMERIC(10,2) DEFAULT 0.00 CHECK (processing_fee >= 0),
  net_amount NUMERIC(10,2) GENERATED ALWAYS AS (amount - processing_fee) STORED,
  transaction_reference VARCHAR(100), -- External payment gateway transaction ID
  gateway_response TEXT, -- Store gateway response for debugging
  failure_reason VARCHAR(500), -- Store failure details
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID DEFAULT uuid_generate_v4(),
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  
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
CREATE INDEX IF NOT EXISTS ix_payments_is_deleted ON payment.payments (is_deleted);

-- Trigger for payments
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'payment' AND table_name = 'payments'
  ) THEN
    DROP TRIGGER IF EXISTS trg_payments_update ON payment.payments;
    CREATE TRIGGER trg_payments_update 
      BEFORE UPDATE ON payment.payments 
      FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();
  END IF;
END$$;

-- Payment Status History table (optional but recommended for audit trail)
CREATE TABLE IF NOT EXISTS payment.payment_status_history (
  payment_status_history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  payment_id BIGINT NOT NULL,
  previous_status_id SMALLINT,
  new_status_id SMALLINT NOT NULL,
  changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  changed_by VARCHAR(100), -- User/system that made the change
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

-- Create roles
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_writer') THEN CREATE ROLE svc_payment_writer NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='svc_payment_reader') THEN CREATE ROLE svc_payment_reader NOLOGIN; END IF;
END$$;

-- Grant permissions
GRANT USAGE ON SCHEMA payment TO svc_payment_reader, svc_payment_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA payment TO svc_payment_writer;
GRANT SELECT ON ALL TABLES IN SCHEMA payment TO svc_payment_reader;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA payment TO svc_payment_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_payment_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT SELECT ON TABLES TO svc_payment_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT USAGE ON SEQUENCES TO svc_payment_writer;

-- Insert default payment statuses
INSERT INTO payment.payment_statuses (status_code, status_name, description, is_terminal, display_order, is_active) 
VALUES 
  ('PENDING', 'Pending', 'Payment is pending processing', FALSE, 1, TRUE),
  ('AUTHORIZED', 'Authorized', 'Payment is authorized but not captured', FALSE, 2, TRUE),
  ('CAPTURED', 'Captured', 'Payment has been captured successfully', FALSE, 3, TRUE),
  ('FAILED', 'Failed', 'Payment processing failed', TRUE, 4, TRUE),
  ('REFUNDED', 'Refunded', 'Payment has been refunded', TRUE, 5, TRUE),
  ('VOIDED', 'Voided', 'Payment authorization has been voided', TRUE, 6, TRUE)
ON CONFLICT (payment_status_id) DO NOTHING;

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

-- Create view for active payments with status and method names
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
  p.payment_date,
  p.amount,
  p.currency_code,
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
WHERE p.is_deleted = FALSE;

GRANT SELECT ON payment.v_payments_detailed TO svc_payment_reader, svc_payment_writer;