CREATE TABLE window_sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10,2),
    window_id VARCHAR(20),
    seller_id VARCHAR(20),
    sold_at DATETIME NOT NULL,
    INDEX idx_window_id (window_id),
    INDEX idx_sold_at (sold_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;