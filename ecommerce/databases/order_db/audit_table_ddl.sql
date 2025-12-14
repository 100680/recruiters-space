-- Create the order_schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS order_schema;

-- Create the audit_logs table
CREATE TABLE IF NOT EXISTS order_schema.audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    level VARCHAR(20) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    user_id BIGINT,
    description VARCHAR(500),
    audit_data TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes as specified in the JPA entity
CREATE INDEX IF NOT EXISTS idx_audit_entity 
ON order_schema.audit_logs (entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_user 
ON order_schema.audit_logs (user_id);

CREATE INDEX IF NOT EXISTS idx_audit_timestamp 
ON order_schema.audit_logs (created_at);

CREATE INDEX IF NOT EXISTS idx_audit_action 
ON order_schema.audit_logs (action);

CREATE INDEX IF NOT EXISTS idx_audit_level 
ON order_schema.audit_logs (level);

-- Add comments for documentation
COMMENT ON TABLE order_schema.audit_logs IS 'Audit log table for tracking all order item operations';
COMMENT ON COLUMN order_schema.audit_logs.audit_id IS 'Primary key - auto-generated audit log identifier';
COMMENT ON COLUMN order_schema.audit_logs.action IS 'Type of action performed (enum AuditAction)';
COMMENT ON COLUMN order_schema.audit_logs.level IS 'Severity/importance level (enum AuditLevel)';
COMMENT ON COLUMN order_schema.audit_logs.entity_type IS 'Type of entity being audited';
COMMENT ON COLUMN order_schema.audit_logs.entity_id IS 'ID of the entity being audited';
COMMENT ON COLUMN order_schema.audit_logs.user_id IS 'ID of the user who performed the action';
COMMENT ON COLUMN order_schema.audit_logs.description IS 'Human-readable description of the action';
COMMENT ON COLUMN order_schema.audit_logs.audit_data IS 'JSON or serialized data of the changes';
COMMENT ON COLUMN order_schema.audit_logs.ip_address IS 'IP address from which the action was performed';
COMMENT ON COLUMN order_schema.audit_logs.user_agent IS 'User agent string from the client';
COMMENT ON COLUMN order_schema.audit_logs.session_id IS 'Session identifier';
COMMENT ON COLUMN order_schema.audit_logs.created_at IS 'Timestamp when the audit log was created';

-- Optional: Create a function to automatically set created_at if not provided
CREATE OR REPLACE FUNCTION order_schema.set_audit_created_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.created_at IS NULL THEN
        NEW.created_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Optional: Create trigger to automatically set created_at
CREATE TRIGGER trigger_set_audit_created_at
    BEFORE INSERT ON order_schema.audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION order_schema.set_audit_created_at();

-- Grant permissions (adjust as needed for your application)
-- GRANT SELECT, INSERT ON order_schema.audit_logs TO your_application_user;
-- GRANT USAGE ON SEQUENCE order_schema.audit_logs_audit_id_seq TO your_application_user;