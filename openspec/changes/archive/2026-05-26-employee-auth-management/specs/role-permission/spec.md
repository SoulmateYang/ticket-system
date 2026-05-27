## ADDED Requirements

### Requirement: Role-based access control
The system SHALL enforce role-based access control where each staff role determines which endpoints they can access.

#### Scenario: Admin has full access
- **WHEN** admin sends any request to `/api/**`
- **THEN** system allows the request to proceed

#### Scenario: Ticketer can access ticket verification endpoints
- **WHEN** ticketer sends POST `/api/tickets/verify`
- **THEN** system allows the request
- **WHEN** ticketer sends DELETE `/api/staff/{id}`
- **THEN** system returns HTTP 403 Forbidden

#### Scenario: Finance can read financial data
- **WHEN** finance sends GET `/api/tickets/passes`
- **THEN** system allows the request
- **WHEN** finance sends POST `/api/staff`
- **THEN** system returns HTTP 403 Forbidden

#### Scenario: OTA operator manages orders
- **WHEN** ota sends GET `/api/ota/orders`
- **THEN** system allows the request
- **WHEN** ota sends POST `/api/tickets/passes`
- **THEN** system returns HTTP 403 Forbidden

### Requirement: Unauthenticated requests are rejected
The system SHALL reject any request to protected endpoints without a valid JWT token.

#### Scenario: No token provided
- **WHEN** request to `/api/tickets/passes` has no Authorization header
- **THEN** system returns HTTP 401 with error message "未提供认证令牌"

#### Scenario: Invalid token format
- **WHEN** request has Authorization header with malformed JWT
- **THEN** system returns HTTP 401 with error message "无效的认证令牌"