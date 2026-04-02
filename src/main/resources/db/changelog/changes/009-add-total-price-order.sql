--liquibase formatted sql

--changeset ebudoskij:17
ALTER TABLE orders ADD COLUMN total_price DECIMAL(10, 2);