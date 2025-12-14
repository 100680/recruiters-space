-- order_db/modify_schema.sql
CREATE SCHEMA IF NOT EXISTS _migrate;

CREATE TABLE IF NOT EXISTS _migrate.orders_parent (
  order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  status_id BIGINT NOT NULL,
  order_date TIMESTAMPTZ NOT NULL,
  total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
) PARTITION BY RANGE (order_date);

CREATE OR REPLACE FUNCTION order.create_monthly_partition(p_year int, p_month int)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  start_dt timestamptz := make_date(p_year, p_month, 1)::timestamptz;
  end_dt timestamptz := (start_dt + INTERVAL '1 month');
  part_name text := format('_migrate.orders_%s', to_char(start_dt, 'YYYY_MM'));
BEGIN
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF _migrate.orders_parent FOR VALUES FROM (%%L) TO (%%L)', part_name) USING start_dt, end_dt;
END;
$$;
