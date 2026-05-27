## ADDED Requirements

### Requirement: Database enum columns SHALL use MySQL ENUM type matching JPA Entity definitions

The system SHALL modify `visitors.status` column to ENUM('ACTIVE','SUSPENDED','CANCELLED') and `window_sales.ticket_type` column to ENUM('YEAR_PASS','MONTH_PASS','SINGLE_USE','WALK_IN','OTA_TICKET') to match JPA Entity enumerated definitions.

#### Scenario: Hibernate schema validation passes after migration
- **WHEN** Spring Boot application starts with `ddl-auto=validate`
- **THEN** Hibernate schema validation completes without errors for all enum columns

#### Scenario: Flyway migration script is idempotent
- **WHEN** migration V005 is executed on a database that already has correct ENUM types
- **THEN** migration completes successfully without error (IF NOT EXISTS behavior)

#### Scenario: Existing data preserved after migration
- **WHEN** migration V005 is executed on a database with existing data
- **THEN** all existing data remains intact and valid