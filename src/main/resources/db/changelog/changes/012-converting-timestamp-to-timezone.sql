--liquibase formatted sql

--changeset ebudoskij:23
-- Convert USERS table
ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Kyiv';

-- Convert ORDERS table
ALTER TABLE orders
    ALTER COLUMN delivery_date TYPE TIMESTAMPTZ USING delivery_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'Europe/Kyiv';

-- Convert PAYMENTS table
ALTER TABLE payments
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Kyiv';

-- Convert AUDIT_LOG table
ALTER TABLE audit_log
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Kyiv';