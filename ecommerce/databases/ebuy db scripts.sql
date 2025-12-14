\set ON_ERROR_STOP on

-- ================================
-- Create databases if missing
-- ================================
SELECT 'CREATE DATABASE user_db'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='user_db')\gexec
SELECT 'CREATE DATABASE product_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='product_db')\gexec
SELECT 'CREATE DATABASE order_db'   WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='order_db')\gexec
SELECT 'CREATE DATABASE review_db'  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='review_db')\gexec
SELECT 'CREATE DATABASE payment_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='payment_db')\gexec
SELECT 'CREATE DATABASE cart_db'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='cart_db')\gexec


-- =========================================================
-- 1) USER DB
-- =========================================================
\c user_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS "user";

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS "user".users (
  user_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  email          VARCHAR(100) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  address        VARCHAR(255),
  phone          VARCHAR(20),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at     TIMESTAMPTZ,
  is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version    BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);

CREATE INDEX IF NOT EXISTS ix_users_email      ON "user".users (email);
CREATE INDEX IF NOT EXISTS ix_users_is_deleted ON "user".users (is_deleted);

DROP TRIGGER IF EXISTS trg_users_update ON "user".users;
CREATE TRIGGER trg_users_update
BEFORE UPDATE ON "user".users
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();


-- =========================================================
-- 2) PRODUCT DB
-- =========================================================
\c product_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS product;

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Categories
CREATE TABLE IF NOT EXISTS product.categories (
  category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at  TIMESTAMPTZ,
  is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_categories_name       ON product.categories (name);
CREATE INDEX IF NOT EXISTS ix_categories_is_deleted ON product.categories (is_deleted);

DROP TRIGGER IF EXISTS trg_categories_update ON product.categories;
CREATE TRIGGER trg_categories_update
BEFORE UPDATE ON product.categories
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Products
CREATE TABLE IF NOT EXISTS product.products (
  product_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name           VARCHAR(100)  NOT NULL,
  description    TEXT,
  price          NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  stock          INT           NOT NULL CHECK (stock >= 0),
  category_id    BIGINT        NOT NULL REFERENCES product.categories(category_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  image_url      VARCHAR(255),
  created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  modified_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at     TIMESTAMPTZ,
  is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version    BIGINT        NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_products_category_id  ON product.products (category_id);
CREATE INDEX IF NOT EXISTS ix_products_is_deleted   ON product.products (is_deleted);

DROP TRIGGER IF EXISTS trg_products_update ON product.products;
CREATE TRIGGER trg_products_update
BEFORE UPDATE ON product.products
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Discount methods
CREATE TABLE IF NOT EXISTS product.discount_methods (
  discount_method_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_name        VARCHAR(50) NOT NULL UNIQUE, -- e.g., Percentage, FixedAmount, BOGO
  description        TEXT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at         TIMESTAMPTZ,
  is_deleted         BOOLEAN NOT NULL DEFAULT FALSE,
  row_version        BIGINT  NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_discount_methods_is_deleted ON product.discount_methods (is_deleted);

DROP TRIGGER IF EXISTS trg_discount_methods_update ON product.discount_methods;
CREATE TRIGGER trg_discount_methods_update
BEFORE UPDATE ON product.discount_methods
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Product discounts
CREATE TABLE IF NOT EXISTS product.product_discounts (
  product_discount_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id          BIGINT        NOT NULL REFERENCES product.products(product_id) ON UPDATE CASCADE ON DELETE CASCADE,
  discount_method_id  BIGINT        NOT NULL REFERENCES product.discount_methods(discount_method_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  discount_value      NUMERIC(10,2) NOT NULL CHECK (discount_value >= 0),
  start_date          TIMESTAMPTZ   NOT NULL,
  end_date            TIMESTAMPTZ   CHECK (end_date IS NULL OR end_date >= start_date),
  active              BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  modified_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at          TIMESTAMPTZ,
  is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
  row_version         BIGINT        NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_product_discounts_product_id ON product.product_discounts (product_id);
CREATE INDEX IF NOT EXISTS ix_product_discounts_active     ON product.product_discounts (active);
CREATE INDEX IF NOT EXISTS ix_product_discounts_is_deleted ON product.product_discounts (is_deleted);

DROP TRIGGER IF EXISTS trg_product_discounts_update ON product.product_discounts;
CREATE TRIGGER trg_product_discounts_update
BEFORE UPDATE ON product.product_discounts
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();


-- =========================================================
-- 3) ORDER DB
-- =========================================================
\c order_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS "order";

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Status
CREATE TABLE IF NOT EXISTS "order".order_status (
  status_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  status_name VARCHAR(50) NOT NULL UNIQUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at  TIMESTAMPTZ,
  is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
  row_version BIGINT  NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_order_status_is_deleted ON "order".order_status (is_deleted);

DROP TRIGGER IF EXISTS trg_order_status_update ON "order".order_status;
CREATE TRIGGER trg_order_status_update
BEFORE UPDATE ON "order".order_status
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Orders
CREATE TABLE IF NOT EXISTS "order".orders (
  order_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id       BIGINT        NOT NULL, -- logical reference to user_db
  status_id     BIGINT        NOT NULL REFERENCES "order".order_status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  order_date    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  total_amount  NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  modified_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMPTZ,
  is_deleted    BOOLEAN       NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version   BIGINT        NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_orders_user_id     ON "order".orders (user_id);
CREATE INDEX IF NOT EXISTS ix_orders_order_date  ON "order".orders (order_date);
CREATE INDEX IF NOT EXISTS ix_orders_status_id   ON "order".orders (status_id);
CREATE INDEX IF NOT EXISTS ix_orders_is_deleted  ON "order".orders (is_deleted);

DROP TRIGGER IF EXISTS trg_orders_update ON "order".orders;
CREATE TRIGGER trg_orders_update
BEFORE UPDATE ON "order".orders
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- Order items (with discount snapshot)
CREATE TABLE IF NOT EXISTS "order".order_items (
  order_item_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id            BIGINT        NOT NULL REFERENCES "order".orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
  product_id          BIGINT        NOT NULL, -- logical reference to product_db
  quantity            INT           NOT NULL CHECK (quantity > 0),
  price               NUMERIC(10,2) NOT NULL CHECK (price >= 0),         -- unit price before discount
  discount_method_id  BIGINT,                                             -- method used (optional, no cross-DB FK)
  discount_value      NUMERIC(10,2),                                      -- % or fixed amount based on method
  final_price         NUMERIC(10,2) NOT NULL CHECK (final_price >= 0),    -- total after discount * quantity
  created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  modified_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at          TIMESTAMPTZ,
  is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
  row_version         BIGINT        NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  CONSTRAINT uq_order_item UNIQUE (order_id, product_id)
);
CREATE INDEX IF NOT EXISTS ix_order_items_order_id            ON "order".order_items (order_id);
CREATE INDEX IF NOT EXISTS ix_order_items_discount_method_id  ON "order".order_items (discount_method_id);
CREATE INDEX IF NOT EXISTS ix_order_items_is_deleted          ON "order".order_items (is_deleted);

DROP TRIGGER IF EXISTS trg_order_items_update ON "order".order_items;
CREATE TRIGGER trg_order_items_update
BEFORE UPDATE ON "order".order_items
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();


-- =========================================================
-- 4) REVIEW DB
-- =========================================================
\c review_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS review;

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS review.reviews (
  review_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     BIGINT      NOT NULL, -- logical reference to user_db
  product_id  BIGINT      NOT NULL, -- logical reference to product_db
  rating      INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment     TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at  TIMESTAMPTZ,
  is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version BIGINT      NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  CONSTRAINT uq_review_user_product UNIQUE (user_id, product_id)
);
CREATE INDEX IF NOT EXISTS ix_reviews_product_id ON review.reviews (product_id);
CREATE INDEX IF NOT EXISTS ix_reviews_is_deleted ON review.reviews (is_deleted);

DROP TRIGGER IF EXISTS trg_reviews_update ON review.reviews;
CREATE TRIGGER trg_reviews_update
BEFORE UPDATE ON review.reviews
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();


-- =========================================================
-- 5) PAYMENT DB
-- =========================================================
\c payment_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS payment;

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS payment.payment_method_types (
  payment_method_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  method_name            VARCHAR(50) NOT NULL UNIQUE,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at             TIMESTAMPTZ,
  is_deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
  row_version            BIGINT      NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_payment_method_types_is_deleted ON payment.payment_method_types (is_deleted);

DROP TRIGGER IF EXISTS trg_payment_method_types_update ON payment.payment_method_types;
CREATE TRIGGER trg_payment_method_types_update
BEFORE UPDATE ON payment.payment_method_types
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

CREATE TABLE IF NOT EXISTS payment.payments (
  payment_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id               BIGINT        NOT NULL, -- logical reference to order_db
  payment_method_type_id BIGINT        NOT NULL REFERENCES payment.payment_method_types(payment_method_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  payment_date           TIMESTAMPTZ,  -- allow null for pending/created
  amount                 NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  payment_status         VARCHAR(50)   NOT NULL CHECK (payment_status IN ('pending','authorized','captured','failed','refunded','voided')),
  created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  modified_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at             TIMESTAMPTZ,
  is_deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
  correlation_id         UUID,
  service_origin         VARCHAR(50),
  row_version            BIGINT        NOT NULL DEFAULT 1 CHECK (row_version >= 1)
);
CREATE INDEX IF NOT EXISTS ix_payments_order_id            ON payment.payments (order_id);
CREATE INDEX IF NOT EXISTS ix_payments_status_created_at   ON payment.payments (payment_status, created_at);
CREATE INDEX IF NOT EXISTS ix_payments_is_deleted          ON payment.payments (is_deleted);

DROP TRIGGER IF EXISTS trg_payments_update ON payment.payments;
CREATE TRIGGER trg_payments_update
BEFORE UPDATE ON payment.payments
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();


-- =========================================================
-- 6) CART DB
-- =========================================================
\c cart_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS cart;

-- shared trigger function
CREATE OR REPLACE FUNCTION update_modified_at_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_at := NOW();
  NEW.row_version := COALESCE(OLD.row_version, 0) + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS cart.cart_items (
  cart_item_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id       BIGINT,
  session_id    VARCHAR(100),
  product_id    BIGINT NOT NULL, -- logical reference to product_db
  quantity      INT    NOT NULL CHECK (quantity > 0),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMPTZ,
  is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version   BIGINT  NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  CONSTRAINT chk_cart_identity CHECK (user_id IS NOT NULL OR session_id IS NOT NULL)
);

-- One product per identity
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_user_product
  ON cart.cart_items (user_id, product_id)
  WHERE session_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_session_product
  ON cart.cart_items (session_id, product_id)
  WHERE user_id IS NULL;

CREATE INDEX IF NOT EXISTS ix_cart_items_user_id     ON cart.cart_items (user_id);
CREATE INDEX IF NOT EXISTS ix_cart_items_is_deleted  ON cart.cart_items (is_deleted);

DROP TRIGGER IF EXISTS trg_cart_items_update ON cart.cart_items;
CREATE TRIGGER trg_cart_items_update
BEFORE UPDATE ON cart.cart_items
FOR EACH ROW EXECUTE FUNCTION update_modified_at_version();

-- ================= END OF PACKAGE =================
