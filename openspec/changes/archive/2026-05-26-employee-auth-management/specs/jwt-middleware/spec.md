## ADDED Requirements

### Requirement: JWT middleware validates tokens on every request
The JWT authentication middleware SHALL intercept every request to protected endpoints, extract and validate the JWT token from the Authorization header.

#### Scenario: Extract Bearer token
- **WHEN** request has Authorization header with value "Bearer <jwt-token>"
- **THEN** middleware extracts the JWT token and validates signature
- **AND** proceeds to check token expiration

#### Scenario: Missing Authorization header
- **WHEN** request has no Authorization header
- **THEN** middleware returns HTTP 401 with error "未提供认证令牌"

#### Scenario: Token validation failure
- **WHEN** request has invalid or expired JWT token
- **THEN** middleware returns HTTP 401 with error "认证令牌无效或已过期"

### Requirement: JWT secret is configurable
The JWT signing key SHALL be configurable via application.yml and MUST NOT be hardcoded.

#### Scenario: Custom JWT secret
- **WHEN** application.yml contains `jwt.secret` configuration
- **THEN** system uses this value for signing and verifying tokens
- **AND** default/empty secret causes application startup failure