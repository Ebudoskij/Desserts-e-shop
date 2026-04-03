--liquibase formatted sql

--changeset ebudoskij:20
ALTER TABLE audit_log DROP CONSTRAINT fk_audit_log_order_id;
ALTER TABLE audit_log DROP CONSTRAINT fk_audit_log_payment_id;
ALTER TABLE audit_log DROP COLUMN order_id;
ALTER TABLE audit_log DROP COLUMN payment_id;


--changeset ebudoskij:21
ALTER TABLE audit_log ADD COLUMN user_id BIGINT;
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_log_user_id FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE audit_log ADD COLUMN entity_id BIGINT;
ALTER TABLE audit_log ADD COLUMN entity_type VARCHAR(50);
ALTER TABLE audit_log ADD COLUMN changes JSONB;
ALTER TABLE audit_log ADD COLUMN ip_address VARCHAR(45);

--changeset ebudoskij:22
-- Index for the Excel export (ordering by time is common)
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);

-- Index for the Entity history
CREATE INDEX idx_audit_log_entity_time ON audit_log (entity_type, entity_id, created_at DESC);

-- Index for security audits (finding actions by a specific user)
CREATE INDEX idx_audit_log_user_id ON audit_log (user_id);