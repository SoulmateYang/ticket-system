-- 添加 window_sale_id 和 ota_order_id 外键列
ALTER TABLE tickets
    ADD COLUMN window_sale_id BIGINT,
    ADD COLUMN ota_order_id BIGINT,
    ADD INDEX idx_ticket_window_sale (window_sale_id),
    ADD INDEX idx_ticket_ota_order (ota_order_id);

-- 将 visitor_id 改为可空（已有数据中次票/OTA票的 visitor_id 设为 NULL）
UPDATE tickets SET visitor_id = NULL WHERE visitor_id = '' OR visitor_id IS NOT NULL AND ticket_type IN ('SINGLE_USE', 'OTA_TICKET');