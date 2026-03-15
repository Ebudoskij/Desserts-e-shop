-- liquibase formatted sql

--changeset ebudoskij:populate-roles-table
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

