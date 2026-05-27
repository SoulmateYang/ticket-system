## ADDED Requirements

### Requirement: Staff can login with phone and password
The system SHALL allow staff members to authenticate using their phone number and password. Upon successful authentication, the system SHALL return a JWT token for subsequent API calls.

#### Scenario: Successful login
- **WHEN** staff submits valid phone and password to POST `/api/auth/login`
- **THEN** system returns HTTP 200 with JWT token in response body
- **AND** token payload contains staff id, role, and expiration time

#### Scenario: Invalid credentials
- **WHEN** staff submits invalid phone or password to POST `/api/auth/login`
- **THEN** system returns HTTP 401 with error message "手机号或密码错误"

#### Scenario: Suspended account login
- **WHEN** staff with status SUSPENDED submits valid credentials
- **THEN** system returns HTTP 403 with error message "账号已停用"

### Requirement: Password must be stored securely
The system SHALL store passwords using BCrypt hashing with a work factor of at least 10. Plain text passwords SHALL NOT be stored or logged.

#### Scenario: Password hashing
- **WHEN** staff registers or updates password
- **THEN** system SHALL hash password with BCrypt before storing
- **AND** original password SHALL NOT appear in any log or response

### Requirement: JWT token contains staff identity
The JWT token payload SHALL contain staff id, employee number, name, role, and expiration time (exp). The token signature SHALL be verified using HS256 algorithm.

#### Scenario: Token payload structure
- **WHEN** system generates JWT after successful login
- **THEN** token payload SHALL contain fields: sub (staff id), employeeNo, name, role, exp, iat
- **AND** token expiration SHALL be set to 24 hours from issuance

### Requirement: System rejects expired or invalid tokens
The system SHALL reject requests with missing, expired, or tampered JWT tokens when accessing protected endpoints.

#### Scenario: Missing token
- **WHEN** request to protected endpoint has no Authorization header
- **THEN** system returns HTTP 401 with error message "未提供认证令牌"

#### Scenario: Expired token
- **WHEN** request to protected endpoint has expired JWT token
- **THEN** system returns HTTP 401 with error message "认证令牌已过期"