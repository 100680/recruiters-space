-- ============================================================================
-- INVENTORY DATABASE SCHEMA FOR HIGH AVAILABILITY E-COMMERCE APPLICATION
-- Normalized Schema with BIGINT Primary Keys
-- All objects created under 'inventory' schema
-- ============================================================================

-- Create inventory schema
CREATE SCHEMA IF NOT EXISTS inventory;

-- Set search path to inventory schema
SET search_path TO inventory;

-- ============================================================================
-- REFERENCE/LOOKUP TABLES
-- ============================================================================

-- Countries Table
CREATE TABLE inventory.countries (
    country_id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(3) UNIQUE NOT NULL,  -- ISO 3166-1 alpha-3
    country_name VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- States/Provinces Table
CREATE TABLE inventory.states (
    state_id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES inventory.countries(country_id),
    state_code VARCHAR(10) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(country_id, state_code)
);

-- Transaction Types Table
CREATE TABLE inventory.transaction_types (
    transaction_type_id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reference Types Table (for linking to cart, order, return, etc.)
CREATE TABLE inventory.reference_types (
    reference_type_id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reservation Status Table
CREATE TABLE inventory.reservation_statuses (
    status_id BIGSERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alert Types Table
CREATE TABLE inventory.alert_types (
    alert_type_id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    severity VARCHAR(10) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alert Status Table
CREATE TABLE inventory.alert_statuses (
    alert_status_id BIGSERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- Warehouses Table
CREATE TABLE inventory.warehouses (
    warehouse_id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(20) UNIQUE NOT NULL,
    warehouse_name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state_id BIGINT REFERENCES inventory.states(state_id),
    country_id BIGINT NOT NULL REFERENCES inventory.countries(country_id),
    postal_code VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory Table (Core inventory tracking)
CREATE TABLE inventory.inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,  -- Foreign key to product_db.products
    warehouse_id BIGINT NOT NULL REFERENCES inventory.warehouses(warehouse_id),
    available_quantity INTEGER NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    total_quantity INTEGER NOT NULL DEFAULT 0 CHECK (total_quantity >= 0),
    reorder_level INTEGER NOT NULL DEFAULT 10,
    reorder_quantity INTEGER NOT NULL DEFAULT 50,
    version INTEGER NOT NULL DEFAULT 1,  -- Optimistic locking
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id),
    CONSTRAINT chk_total_quantity CHECK (total_quantity = available_quantity + reserved_quantity)
);

-- Inventory Transactions Table (Audit trail)
CREATE TABLE inventory.inventory_transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory.inventory(inventory_id),
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL REFERENCES inventory.warehouses(warehouse_id),
    transaction_type_id BIGINT NOT NULL REFERENCES inventory.transaction_types(transaction_type_id),
    quantity_change INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type_id BIGINT REFERENCES inventory.reference_types(reference_type_id),
    reference_id BIGINT,  -- cart_id, order_id, return_id, etc.
    reason TEXT,
    performed_by BIGINT,  -- user_id or system_id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB  -- Additional flexible data
);

-- Reservations Table (Temporary inventory holds)
CREATE TABLE inventory.reservations (
    reservation_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL REFERENCES inventory.warehouses(warehouse_id),
    inventory_id BIGINT NOT NULL REFERENCES inventory.inventory(inventory_id),
    reference_type_id BIGINT NOT NULL REFERENCES inventory.reference_types(reference_type_id),
    reference_id BIGINT NOT NULL,  -- cart_id or order_id
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status_id BIGINT NOT NULL REFERENCES inventory.reservation_statuses(status_id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fulfilled_at TIMESTAMP
);

-- Inventory Alerts Table (Low stock notifications)
CREATE TABLE inventory.inventory_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory.inventory(inventory_id),
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL REFERENCES inventory.warehouses(warehouse_id),
    alert_type_id BIGINT NOT NULL REFERENCES inventory.alert_types(alert_type_id),
    current_quantity INTEGER NOT NULL,
    threshold_quantity INTEGER,
    message TEXT,
    alert_status_id BIGINT NOT NULL REFERENCES inventory.alert_statuses(alert_status_id),
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- VIEWS
-- ============================================================================

-- Product Availability View (Aggregated inventory across warehouses)
CREATE VIEW inventory.product_availability AS
SELECT 
    product_id,
    SUM(available_quantity) as total_available,
    SUM(reserved_quantity) as total_reserved,
    SUM(total_quantity) as total_stock,
    COUNT(DISTINCT warehouse_id) as warehouse_count,
    MAX(updated_at) as last_updated
FROM inventory.inventory
WHERE available_quantity > 0
GROUP BY product_id;

-- Inventory with Details View
CREATE VIEW inventory.inventory_details AS
SELECT 
    i.inventory_id,
    i.product_id,
    i.warehouse_id,
    w.warehouse_code,
    w.warehouse_name,
    c.country_name,
    s.state_name,
    i.available_quantity,
    i.reserved_quantity,
    i.total_quantity,
    i.reorder_level,
    i.version,
    i.updated_at
FROM inventory.inventory i
JOIN inventory.warehouses w ON i.warehouse_id = w.warehouse_id
JOIN inventory.countries c ON w.country_id = c.country_id
LEFT JOIN inventory.states s ON w.state_id = s.state_id;

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Inventory indexes
CREATE INDEX idx_inventory_product ON inventory.inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory.inventory(warehouse_id);
CREATE INDEX idx_inventory_available ON inventory.inventory(available_quantity) WHERE available_quantity > 0;
CREATE INDEX idx_inventory_low_stock ON inventory.inventory(product_id, warehouse_id) 
    WHERE available_quantity <= reorder_level;

-- Transaction indexes
CREATE INDEX idx_transactions_inventory ON inventory.inventory_transactions(inventory_id);
CREATE INDEX idx_transactions_product ON inventory.inventory_transactions(product_id);
CREATE INDEX idx_transactions_reference ON inventory.inventory_transactions(reference_type_id, reference_id);
CREATE INDEX idx_transactions_created ON inventory.inventory_transactions(created_at DESC);
CREATE INDEX idx_transactions_type ON inventory.inventory_transactions(transaction_type_id);

-- Reservation indexes
CREATE INDEX idx_reservations_product ON inventory.reservations(product_id);
CREATE INDEX idx_reservations_reference ON inventory.reservations(reference_type_id, reference_id);
CREATE INDEX idx_reservations_status ON inventory.reservations(status_id);
CREATE INDEX idx_reservations_expires ON inventory.reservations(expires_at, status_id);
CREATE INDEX idx_reservations_inventory ON inventory.reservations(inventory_id);

-- Alert indexes
CREATE INDEX idx_alerts_inventory ON inventory.inventory_alerts(inventory_id);
CREATE INDEX idx_alerts_product ON inventory.inventory_alerts(product_id);
CREATE INDEX idx_alerts_status ON inventory.inventory_alerts(alert_status_id);
CREATE INDEX idx_alerts_type ON inventory.inventory_alerts(alert_type_id);

-- Warehouse indexes
CREATE INDEX idx_warehouses_country ON inventory.warehouses(country_id);
CREATE INDEX idx_warehouses_state ON inventory.warehouses(state_id);
CREATE INDEX idx_warehouses_active ON inventory.warehouses(is_active) WHERE is_active = true;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION inventory.update_timestamp()
RETURNS TRIGGER AS 
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Apply update timestamp triggers
CREATE TRIGGER trg_warehouses_updated 
    BEFORE UPDATE ON inventory.warehouses 
    FOR EACH ROW EXECUTE FUNCTION inventory.update_timestamp();

CREATE TRIGGER trg_inventory_updated 
    BEFORE UPDATE ON inventory.inventory 
    FOR EACH ROW EXECUTE FUNCTION inventory.update_timestamp();

CREATE TRIGGER trg_reservations_updated 
    BEFORE UPDATE ON inventory.reservations 
    FOR EACH ROW EXECUTE FUNCTION inventory.update_timestamp();

-- ============================================================================
-- STORED PROCEDURES FOR INVENTORY OPERATIONS
-- ============================================================================

-- Reserve inventory (for cart)
CREATE OR REPLACE FUNCTION inventory.reserve_inventory(
    p_product_id BIGINT,
    p_warehouse_id BIGINT,
    p_reference_type_code VARCHAR(50),
    p_reference_id BIGINT,
    p_quantity INTEGER,
    p_expiry_minutes INTEGER DEFAULT 30
) 
RETURNS BIGINT AS 
$$
DECLARE
    v_inventory_id BIGINT;
    v_reservation_id BIGINT;
    v_current_version INTEGER;
    v_reference_type_id BIGINT;
    v_transaction_type_id BIGINT;
    v_status_id BIGINT;
    v_qty_before INTEGER;
BEGIN
    -- Get reference type ID
    SELECT reference_type_id INTO v_reference_type_id
    FROM inventory.reference_types WHERE type_code = p_reference_type_code;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Invalid reference type: %', p_reference_type_code;
    END IF;
    
    -- Get active status ID
    SELECT status_id INTO v_status_id
    FROM inventory.reservation_statuses WHERE status_code = 'ACTIVE';
    
    -- Get inventory with row lock
    SELECT inventory_id, version, available_quantity 
    INTO v_inventory_id, v_current_version, v_qty_before
    FROM inventory.inventory 
    WHERE product_id = p_product_id 
        AND warehouse_id = p_warehouse_id 
        AND available_quantity >= p_quantity
    FOR UPDATE;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Insufficient inventory available';
    END IF;
    
    -- Update inventory with optimistic locking
    UPDATE inventory.inventory 
    SET available_quantity = available_quantity - p_quantity,
        reserved_quantity = reserved_quantity + p_quantity,
        version = version + 1
    WHERE inventory_id = v_inventory_id 
        AND version = v_current_version;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Inventory was modified by another transaction';
    END IF;
    
    -- Create reservation
    INSERT INTO inventory.reservations (
        product_id, warehouse_id, inventory_id, 
        reference_type_id, reference_id, quantity, 
        status_id, expires_at
    ) VALUES (
        p_product_id, p_warehouse_id, v_inventory_id,
        v_reference_type_id, p_reference_id, p_quantity,
        v_status_id,
        CURRENT_TIMESTAMP + (p_expiry_minutes || ' minutes')::INTERVAL
    ) RETURNING reservation_id INTO v_reservation_id;
    
    -- Get transaction type ID
    SELECT transaction_type_id INTO v_transaction_type_id
    FROM inventory.transaction_types WHERE type_code = 'RESERVE';
    
    -- Log transaction
    INSERT INTO inventory.inventory_transactions (
        inventory_id, product_id, warehouse_id, transaction_type_id,
        quantity_change, quantity_before, quantity_after,
        reference_type_id, reference_id, reason
    ) VALUES (
        v_inventory_id, p_product_id, p_warehouse_id, v_transaction_type_id,
        -p_quantity, v_qty_before, v_qty_before - p_quantity,
        v_reference_type_id, p_reference_id, 'Inventory reserved'
    );
    
    RETURN v_reservation_id;
END;
$$
LANGUAGE plpgsql;

-- Release reservation (cancel cart/order)
CREATE OR REPLACE FUNCTION inventory.release_reservation(p_reservation_id BIGINT) 
RETURNS BOOLEAN AS 
$$
DECLARE
    v_reservation RECORD;
    v_cancelled_status_id BIGINT;
    v_active_status_id BIGINT;
    v_transaction_type_id BIGINT;
BEGIN
    -- Get cancelled status ID
    SELECT status_id INTO v_cancelled_status_id
    FROM inventory.reservation_statuses WHERE status_code = 'CANCELLED';
    
    -- Get active status ID for the query
    SELECT status_id INTO v_active_status_id
    FROM inventory.reservation_statuses WHERE status_code = 'ACTIVE';
    
    -- Get reservation details
    SELECT * INTO v_reservation 
    FROM inventory.reservations 
    WHERE reservation_id = p_reservation_id 
        AND status_id = v_active_status_id
    FOR UPDATE;
    
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;
    
    -- Update inventory
    UPDATE inventory.inventory 
    SET available_quantity = available_quantity + v_reservation.quantity,
        reserved_quantity = reserved_quantity - v_reservation.quantity,
        version = version + 1
    WHERE inventory_id = v_reservation.inventory_id;
    
    -- Update reservation status
    UPDATE reservations 
    SET status_id = v_cancelled_status_id, 
        updated_at = CURRENT_TIMESTAMP
    WHERE reservation_id = p_reservation_id;
    
    -- Get transaction type ID
    SELECT transaction_type_id INTO v_transaction_type_id
    FROM inventory.transaction_types WHERE type_code = 'RELEASE';
    
    -- Log transaction
    INSERT INTO inventory.inventory_transactions (
        inventory_id, product_id, warehouse_id, transaction_type_id,
        quantity_change, quantity_before, quantity_after,
        reference_type_id, reference_id, reason
    ) VALUES (
        v_reservation.inventory_id, v_reservation.product_id, 
        v_reservation.warehouse_id, v_transaction_type_id,
        v_reservation.quantity, 0, v_reservation.quantity,
        v_reservation.reference_type_id, v_reservation.reference_id, 
        'Reservation released'
    );
    
    RETURN TRUE;
END;
$$
LANGUAGE plpgsql;

-- Fulfill reservation (complete order)
CREATE OR REPLACE FUNCTION inventory.fulfill_reservation(p_reservation_id BIGINT) 
RETURNS BOOLEAN AS 
$$
DECLARE
    v_reservation RECORD;
    v_fulfilled_status_id BIGINT;
    v_active_status_id BIGINT;
    v_transaction_type_id BIGINT;
BEGIN
    -- Get status IDs
    SELECT status_id INTO v_fulfilled_status_id
    FROM inventory.reservation_statuses WHERE status_code = 'FULFILLED';
    
    SELECT status_id INTO v_active_status_id
    FROM inventory.reservation_statuses WHERE status_code = 'ACTIVE';
    
    -- Get reservation
    SELECT * INTO v_reservation 
    FROM inventory.reservations 
    WHERE reservation_id = p_reservation_id 
        AND status_id = v_active_status_id
    FOR UPDATE;
    
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;
    
    -- Update inventory - deduct reserved quantity
    UPDATE inventory.inventory 
    SET reserved_quantity = reserved_quantity - v_reservation.quantity,
        total_quantity = total_quantity - v_reservation.quantity,
        version = version + 1
    WHERE inventory_id = v_reservation.inventory_id;
    
    -- Update reservation
    UPDATE inventory.reservations 
    SET status_id = v_fulfilled_status_id,
        fulfilled_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE reservation_id = p_reservation_id;
    
    -- Get transaction type ID
    SELECT transaction_type_id INTO v_transaction_type_id
    FROM inventory.transaction_types WHERE type_code = 'DEDUCT';
    
    -- Log transaction
    INSERT INTO inventory.inventory_transactions (
        inventory_id, product_id, warehouse_id, transaction_type_id,
        quantity_change, quantity_before, quantity_after,
        reference_type_id, reference_id, reason
    ) VALUES (
        v_reservation.inventory_id, v_reservation.product_id, 
        v_reservation.warehouse_id, v_transaction_type_id,
        -v_reservation.quantity, 0, -v_reservation.quantity,
        v_reservation.reference_type_id, v_reservation.reference_id, 
        'Order fulfilled'
    );
    
    RETURN TRUE;
END;
$$
LANGUAGE plpgsql;

-- ============================================================================
-- SEED DATA FOR LOOKUP TABLES
-- ============================================================================

-- Insert Countries
INSERT INTO inventory.countries (country_code, country_name) VALUES
    ('USA', 'United States of America'),
    ('CAN', 'Canada'),
    ('MEX', 'Mexico'),
    ('GBR', 'United Kingdom'),
    ('DEU', 'Germany'),
    ('FRA', 'France'),
    ('IND', 'India'),
    ('CHN', 'China'),
    ('JPN', 'Japan'),
    ('AUS', 'Australia');

-- Insert States (USA examples)
INSERT INTO inventory.states (country_id, state_code, state_name) VALUES
    ((SELECT country_id FROM inventory.countries WHERE country_code = 'USA'), 'CA', 'California'),
    ((SELECT country_id FROM inventory.countries WHERE country_code = 'USA'), 'NY', 'New York'),
    ((SELECT country_id FROM inventory.countries WHERE country_code = 'USA'), 'TX', 'Texas'),
    ((SELECT country_id FROM inventory.countries WHERE country_code = 'USA'), 'FL', 'Florida'),
    ((SELECT country_id FROM inventory.countries WHERE country_code = 'USA'), 'IL', 'Illinois');

-- Insert Transaction Types
INSERT INTO inventory.transaction_types (type_code, type_name, description) VALUES
    ('RESERVE', 'Reserve Inventory', 'Inventory reserved for cart or order'),
    ('RELEASE', 'Release Reservation', 'Reservation cancelled or expired'),
    ('DEDUCT', 'Deduct Inventory', 'Inventory deducted for fulfilled order'),
    ('RESTOCK', 'Restock Inventory', 'New inventory added to warehouse'),
    ('ADJUST', 'Adjust Inventory', 'Manual inventory adjustment'),
    ('RETURN', 'Return to Inventory', 'Product returned by customer'),
    ('DAMAGE', 'Damaged Inventory', 'Inventory marked as damaged'),
    ('LOST', 'Lost Inventory', 'Inventory marked as lost');

-- Insert Reference Types
INSERT INTO inventory.reference_types (type_code, type_name, description) VALUES
    ('CART', 'Shopping Cart', 'Reference to shopping cart'),
    ('ORDER', 'Order', 'Reference to order'),
    ('RETURN', 'Return', 'Reference to return request'),
    ('MANUAL', 'Manual Adjustment', 'Manual inventory adjustment'),
    ('SYSTEM', 'System Adjustment', 'Automated system adjustment');

-- Insert Reservation Statuses
INSERT INTO inventory.reservation_statuses (status_code, status_name, description) VALUES
    ('ACTIVE', 'Active', 'Reservation is active'),
    ('EXPIRED', 'Expired', 'Reservation has expired'),
    ('FULFILLED', 'Fulfilled', 'Reservation fulfilled by order'),
    ('CANCELLED', 'Cancelled', 'Reservation cancelled');

-- Insert Alert Types
INSERT INTO inventory.alert_types (type_code, type_name, description, severity) VALUES
    ('LOW_STOCK', 'Low Stock', 'Inventory below reorder level', 'MEDIUM'),
    ('OUT_OF_STOCK', 'Out of Stock', 'Inventory depleted', 'HIGH'),
    ('OVERSTOCK', 'Overstock', 'Excess inventory', 'LOW'),
    ('REORDER_NEEDED', 'Reorder Needed', 'Reorder required', 'HIGH');

-- Insert Alert Statuses
INSERT INTO inventory.alert_statuses (status_code, status_name, description) VALUES
    ('OPEN', 'Open', 'Alert is open and requires attention'),
    ('ACKNOWLEDGED', 'Acknowledged', 'Alert has been acknowledged'),
    ('RESOLVED', 'Resolved', 'Alert has been resolved'),
    ('IGNORED', 'Ignored', 'Alert has been ignored');

-- Insert Sample Warehouses
INSERT INTO inventory.warehouses (warehouse_code, warehouse_name, city, state_id, country_id) VALUES
    ('WH-US-EAST', 'East Coast Distribution Center', 'New York', 
     (SELECT state_id FROM inventory.states WHERE state_code = 'NY'), 
     (SELECT country_id FROM inventory.countries WHERE country_code = 'USA')),
    ('WH-US-WEST', 'West Coast Distribution Center', 'Los Angeles', 
     (SELECT state_id FROM inventory.states WHERE state_code = 'CA'), 
     (SELECT country_id FROM inventory.countries WHERE country_code = 'USA')),
    ('WH-EU-CENTRAL', 'European Distribution Center', 'Berlin', 
     NULL, 
     (SELECT country_id FROM inventory.countries WHERE country_code = 'DEU'));

-- ============================================================================
-- GRANT PERMISSIONS (Optional - adjust based on your security requirements)
-- ============================================================================
-- GRANT USAGE ON SCHEMA inventory TO your_application_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA inventory TO your_application_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA inventory TO your_application_user;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA inventory TO your_application_user;

-- ============================================================================
-- EXAMPLE: Insert inventory for products
-- ============================================================================
-- INSERT INTO inventory.inventory (product_id, warehouse_id, available_quantity, total_quantity, reorder_level)
-- VALUES (1, 1, 100, 100, 20);

-- ============================================================================
-- VERIFY SCHEMA CREATION
-- ============================================================================
-- SELECT * FROM inventory.countries;
-- SELECT * FROM inventory.warehouses;
-- SELECT * FROM inventory.inventory;