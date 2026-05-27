## 1. Backend Setup

- [x] 1.1 Add jjwt and spring-security-crypto dependencies to pom.xml
- [x] 1.2 Add JWT secret configuration to application.yml
- [x] 1.3 Create Staff entity (id, employeeNo, name, phone, passwordHash, role, status, createdAt, updatedAt)
- [x] 1.4 Create StaffRepository interface
- [x] 1.5 Create StaffRole enum (ADMIN, TICKETER, FINANCE, OTA)
- [x] 1.6 Create StaffStatus enum (ACTIVE, SUSPENDED)

## 2. Security Infrastructure

- [x] 2.1 Create BCryptPasswordEncoder bean configuration
- [x] 2.2 Create JwtTokenUtil class (generate, validate, parse claims)
- [x] 2.3 Create JwtAuthenticationFilter (oncePerRequestFilter)
- [x] 2.4 Create SecurityConfig with whitelist and JWT filter

## 3. Auth API

- [x] 3.1 Create LoginRequest DTO
- [x] 3.2 Create AuthController with POST /api/auth/login
- [x] 3.3 Create PasswordChangeRequest DTO
- [x] 3.4 Add PUT /api/auth/password endpoint
- [x] 3.5 Create default admin account on startup (CommandLineRunner)

## 4. Staff Management API

- [x] 4.1 Create StaffResponse DTO (exclude passwordHash)
- [x] 4.2 Create StaffCreateRequest DTO
- [x] 4.3 Create StaffController with CRUD endpoints
- [x] 4.4 Add role-based endpoint protection
- [x] 4.5 Add StaffNotFoundException and global handler

## 5. Database

- [x] 5.1 Create V1__create_staff_table.sql migration
- [x] 5.2 Add Flyway or init script reference to application.yml

## 6. Frontend - Auth

- [x] 6.1 Create LoginView.vue with Cyberpunk styling
- [x] 6.2 Add LoginRequest to api/index.js
- [x] 6.3 Create useAuthStore with token management
- [x] 6.4 Add request interceptor to attach JWT token
- [x] 6.5 Add 401 response handler to redirect to login

## 7. Frontend - Staff Management

- [x] 7.1 Create StaffView.vue with CRUD table
- [x] 7.2 Add staff API methods (fetch, create, update, delete)
- [x] 7.3 Add router guard for protected routes
- [x] 7.4 Add role-based menu visibility

## 8. Testing

- [ ] 8.1 Unit test JwtTokenUtil (generate, validate, parse)
- [ ] 8.2 Unit test BCryptPasswordEncoder
- [ ] 8.3 Integration test POST /api/auth/login (valid/invalid/suspended)
- [ ] 8.4 Integration test protected endpoint without token
- [ ] 8.5 Integration test role-based access