-- cart_db/modify_schema.sql
ALTER TABLE cart.cart_items ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS ix_cart_items_expires_at ON cart.cart_items (expires_at);
