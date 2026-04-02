--liquibase formatted sql

--changeset ebudoskij:18
ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP;

--changeset ebudoskij:19
INSERT INTO order_statuses(name) VALUES ('CANCELLED') ON CONFLICT (name) DO NOTHING;
