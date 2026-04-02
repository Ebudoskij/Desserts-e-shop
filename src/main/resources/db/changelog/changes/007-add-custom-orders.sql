--liquibase formatted sql

--changeset ebudoskij:14
ALTER TABLE order_items
ADD COLUMN custom_decor BOOLEAN DEFAULT FALSE NOT NULL,
ADD COLUMN custom_decor_description VARCHAR(500),
ADD COLUMN custom_decor_price DECIMAL(10, 2),
ADD COLUMN admin_comment VARCHAR(500);

--changeset ebudoskij:15
INSERT INTO order_statuses (name) VALUES 
('CREATED'),
('PENDING_CUSTOM_REVIEW'),
('WAITING_FOR_CLIENT_APPROVAL'),
('CONFIRMED'),
('REJECTED'),
('PAID'),
('PREPARING'),
('SENT'),
('DELIVERED'),
('RECEIVED')
ON CONFLICT (name) DO NOTHING;
