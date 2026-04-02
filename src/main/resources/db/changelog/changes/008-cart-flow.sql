--liquibase formatted sql

--changeset ebudoskij:16
ALTER TABLE orders ALTER COLUMN delivery_address DROP NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_date DROP NOT NULL;
