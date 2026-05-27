## ADDED Requirements

### Requirement: Admin can create staff account
The system SHALL allow administrators to create new staff accounts with employee number, name, phone, initial password, and role.

#### Scenario: Create staff successfully
- **WHEN** admin submits POST `/api/staff` with employeeNo, name, phone, password, and role
- **THEN** system creates staff record and returns HTTP 201 with created staff data (excluding password)
- **AND** staff can login with the provided credentials

#### Scenario: Duplicate phone or employee number
- **WHEN** admin submits POST `/api/staff` with phone or employeeNo that already exists
- **THEN** system returns HTTP 409 with error message "手机号已注册" or "工号已存在"

### Requirement: Admin can query staff list
The system SHALL allow administrators to list all staff members with pagination support.

#### Scenario: List staff with pagination
- **WHEN** admin sends GET `/api/staff?page=0&size=10`
- **THEN** system returns paginated list of staff (excluding password_hash)
- **AND** response includes total count, page number, and page size

#### Scenario: Search staff by name or phone
- **WHEN** admin sends GET `/api/staff?search=张三`
- **THEN** system returns staff members whose name or phone matches the search term

### Requirement: Admin can update staff information
The system SHALL allow administrators to update staff name, phone, role, and status.

#### Scenario: Update staff role
- **WHEN** admin sends PUT `/api/staff/{id}/role` with new role
- **THEN** system updates staff role and returns HTTP 200

#### Scenario: Suspend staff account
- **WHEN** admin sends PUT `/api/staff/{id}/suspend`
- **THEN** system sets staff status to SUSPENDED
- **AND** suspended staff cannot login

#### Scenario: Reactivate staff account
- **WHEN** admin sends PUT `/api/staff/{id}/activate`
- **THEN** system sets staff status to ACTIVE
- **AND** staff can login again

### Requirement: Admin can delete staff
The system SHALL allow administrators to permanently delete staff accounts. Soft delete is not required for Phase 1.

#### Scenario: Delete staff
- **WHEN** admin sends DELETE `/api/staff/{id}`
- **THEN** system permanently removes staff record
- **AND** returns HTTP 204

### Requirement: Staff can change own password
The system SHALL allow authenticated staff to change their own password.

#### Scenario: Change own password
- **WHEN** authenticated staff sends PUT `/api/auth/password` with currentPassword and newPassword
- **THEN** system verifies current password, updates to new password (BCrypt hashed)
- **AND** returns HTTP 200

#### Scenario: Wrong current password
- **WHEN** staff submits PUT `/api/auth/password` with incorrect currentPassword
- **THEN** system returns HTTP 400 with error message "当前密码错误"