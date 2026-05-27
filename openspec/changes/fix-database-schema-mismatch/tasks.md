## 1. Create Flyway Migration Script

- [x] 1.1 Create `V005__fix_enum_types.sql` in `backend/src/main/resources/db/migration/`
- [x] 1.2 Include ALTER TABLE statements for visitors.status and window_sales.ticket_type
- [x] 1.3 Add IF NOT EXISTS / conditional logic for idempotency

## 2. Verify Migration

- [x] 2.1 Run migration against local MySQL database
- [x] 2.2 Confirm Hibernate schema validation passes on application startup
- [x] 2.3 Verify existing data is preserved