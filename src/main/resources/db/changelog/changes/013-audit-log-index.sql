--liquibase formatted sql

--changeset ebudoskij:23
-- Supports fast filtering by action type in the audit log admin UI
CREATE INDEX IF NOT EXISTS idx_audit_log_action_type ON audit_log (action_type);
