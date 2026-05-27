-- V005: Fix enum column types to match JPA Entity definitions
-- Problem: Hibernate 6.x schema validation fails when enum columns use varchar instead of ENUM type
-- Solution: Convert visitors.status and window_sales.ticket_type to proper ENUM types

-- Fix visitors.status column: varchar -> ENUM('ACTIVE','SUSPENDED','CANCELLED')
ALTER TABLE visitors MODIFY COLUMN status ENUM('ACTIVE','SUSPENDED','CANCELLED') NOT NULL DEFAULT 'ACTIVE';

-- Fix window_sales.ticket_type column: varchar -> ENUM('YEAR_PASS','MONTH_PASS','SINGLE_USE','WALK_IN','OTA_TICKET')
ALTER TABLE window_sales MODIFY COLUMN ticket_type ENUM('YEAR_PASS','MONTH_PASS','SINGLE_USE','WALK_IN','OTA_TICKET') NOT NULL;