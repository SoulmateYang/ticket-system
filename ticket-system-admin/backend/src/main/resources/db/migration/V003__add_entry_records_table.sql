CREATE TABLE entry_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    visitor_id VARCHAR(18),
    entry_time DATETIME NOT NULL,
    gate VARCHAR(20),
    channel VARCHAR(20),
    verified_by VARCHAR(20),
    INDEX idx_er_ticket_id (ticket_id),
    INDEX idx_er_entry_time (entry_time),
    INDEX idx_er_visitor_id (visitor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;