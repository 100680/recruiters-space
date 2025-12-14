-- payment_db/modify_schema.sql
CREATE SCHEMA IF NOT EXISTS _migrate;

CREATE TABLE IF NOT EXISTS _migrate.payments_parent (
  payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL,
  payment_method_type_id BIGINT NOT NULL,
  payment_date TIMESTAMPTZ NOT NULL,
  amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  payment_status payment.payment_status_enum NOT NULL DEFAULT 'pending',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  correlation_id UUID,
  service_origin VARCHAR(50),
  row_version BIGINT NOT NULL DEFAULT 1 CHECK (row_version >= 1)
) PARTITION BY RANGE (payment_date);

CREATE OR REPLACE FUNCTION payment.create_monthly_partition(p_year int, p_month int)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  start_dt timestamptz := make_date(p_year, p_month, 1)::timestamptz;
  end_dt timestamptz := (start_dt + INTERVAL '1 month');
  part_name text := format('_migrate.payments_%s', to_char(start_dt, 'YYYY_MM'));
BEGIN
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF _migrate.payments_parent FOR VALUES FROM (%%L) TO (%%L)', part_name) USING start_dt, end_dt;
END;
$$;
