-- liquibase formatted sql

-- changeset ebudoskij:products_customizable
ALTER TABLE products
    ADD COLUMN customizable BOOLEAN DEFAULT true NOT NULL;