-- liquibase formatted sql

--changeset ebudoskij:categories_soft_delete
ALTER TABLE categories
    ADD COLUMN is_deleted BOOLEAN DEFAULT false NOT NULL;

--changeset ebudoskij:products_soft_delete
ALTER TABLE products
    ADD COLUMN is_deleted BOOLEAN DEFAULT false NOT NULL;

--changeset ebudoskij:orders_soft_delete
ALTER TABLE orders
    ADD COLUMN is_deleted BOOLEAN DEFAULT false NOT NULL;

--changeset ebudoskij:additional_items_soft_delete
ALTER TABLE additional_items
    ADD COLUMN is_deleted BOOLEAN DEFAULT false NOT NULL;

--changeset ebudoskij:order_items_soft_delete
ALTER TABLE order_items
    ADD COLUMN is_deleted BOOLEAN DEFAULT false NOT NULL;