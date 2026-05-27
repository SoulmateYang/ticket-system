-- Staff table for employee authentication and authorization
CREATE TABLE IF NOT EXISTS staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_no VARCHAR(32) NOT NULL UNIQUE COMMENT '工号',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    phone VARCHAR(16) NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 密码哈希',
    role VARCHAR(32) NOT NULL COMMENT '角色：ADMIN/TICKETER/FINANCE/OTA',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/SUSPENDED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_staff_phone (phone),
    INDEX idx_staff_employee_no (employee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- Default admin account: ADMIN001 / admin123
-- Password hash generated with BCrypt (work factor 10)
-- INSERT INTO staff (employee_no, name, phone, password_hash, role, status)
-- VALUES ('ADMIN001', '系统管理员', '13800000000', '$2a$10$...', 'ADMIN', 'ACTIVE');