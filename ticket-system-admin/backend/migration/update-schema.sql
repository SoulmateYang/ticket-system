ALTER TABLE ota_orders
DROP
COLUMN ota_channel;

ALTER TABLE ota_orders
DROP
COLUMN ticket_type;

ALTER TABLE ota_orders
    ADD ota_channel VARCHAR(20) NOT NULL;

ALTER TABLE staff
DROP
COLUMN `role`;

ALTER TABLE staff
DROP
COLUMN status;

ALTER TABLE staff
    ADD `role` VARCHAR(32) NOT NULL;

ALTER TABLE staff
    ADD status VARCHAR(16) NOT NULL;

ALTER TABLE ota_orders
    ADD ticket_type VARCHAR(20) NOT NULL;

ALTER TABLE tickets
DROP
COLUMN type;

ALTER TABLE tickets
    ADD type VARCHAR(20) NOT NULL;

CREATE INDEX idx_external_order_id ON ota_orders (external_order_id);