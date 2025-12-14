-- ================================
-- Create databases if missing
-- ================================
SELECT 'CREATE DATABASE user_db'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='user_db')\gexec
SELECT 'CREATE DATABASE product_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='product_db')\gexec
SELECT 'CREATE DATABASE order_db'   WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='order_db')\gexec
SELECT 'CREATE DATABASE review_db'  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='review_db')\gexec
SELECT 'CREATE DATABASE payment_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='payment_db')\gexec
SELECT 'CREATE DATABASE cart_db'    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='cart_db')\gexec