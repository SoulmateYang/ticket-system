<!-- /autoplan restore point: C:\Users\ymh\.gstack\projects\SoulmateYang-ticketSystem\main-autoplan-restore-20260526-153203.md -->
# /autoplan Review: Staff Management + JWT Authentication

## Phase 0: Context

**Platform:** GitHub (SoulmateYang/ticketSystem)
**Branch:** main
**Base branch:** main
**CEO Plan:** CEO_PLAN.md (ACTIVE, SELECTIVE EXPANSION, 2026-05-26)
**Design Doc:** DESIGN.md (Cyberpunk UI System, IMPLEMENTED status)
**UI Scope:** YES — Login.vue, Staff.vue, AdminLayout.vue
**DX Scope:** YES — new REST API endpoints, Spring Boot security changes

**What's being built (uncommitted):**
- Staff CRUD module (ADMIN/TICKETER/FINANCE/OTA roles)
- JWT authentication (login, password change, JWT filter)
- Spring Security config (stateless, CORS)
- Frontend: Login.vue + Staff.vue with Cyberpunk design

---

## Phase 1: CEO Review

### Step 0A: Premise Challenge

**Premise 1: "景区票务中台不只是替代人工"**
✅ VALID — The plan correctly identifies that OTA platforms are the real threat. Once美团/抖音 own the customer relationship, the scenic park loses pricing power and customer data.

**Premise 2: "年票/月票管理是核心护城河"**
✅ VALID — Annual/monthly passes create locked-in customers with repeat visits. This is defensible.

**Premise 3: "扫码枪即插即用 + 验票音效是低成本高体验"**
✅ VALID —验票员 satisfaction is critical for accuracy. Sound feedback reduces cognitive load.

**Premise 4: "Phase 2的OTA自动同步"**
⚠️ ASSUMED — 美团/抖音/携程 APIs require third-party distributor agreements. The plan acknowledges this as a prerequisite but doesn't address HOW to get the API access.

### Step 0B: Existing Code Leverage

| Sub-problem | Existing Code | New Work |
|---|---|---|
| Ticket/EntryLog model | ✅ model/ package | Staff model is new |
| Redis for distributed lock | ✅ RedisConfig | Reused |
| REST API pattern | ✅ existing controllers | StaffController follows pattern |
| JWT auth | ❌ NOT EXISTS | Brand new (JwtTokenUtil, JwtAuthenticationFilter, AuthController) |
| Frontend Cyberpunk CSS | ✅ AdminLayout.vue | Login.vue + Staff.vue follow design |

### Step 0C: Dream State Mapping

```
CURRENT STATE (2026-05-26)
景区票务系统：年票/月票/次票核销已实现，OTA对接未完成，员工认证缺失
                          │
                          ▼ [THIS PLAN adds: Staff管理 + JWT认证]
                          │
12-MONTH IDEAL
景区拥有完整票务数据：年票会员画像 + OTA分销数据 + 实时核销看板
无人值守入场：扫码枪 + 音效 + Redis防重 + 人脸识别(Phase 2)
```

### Step 0C-bis: Implementation Alternatives

**APPROACH A: Staff + JWT (Current plan)**
  Summary: Add Staff entity + JWT auth, frontend Login + Staff management pages
  Effort: M
  Risk: Low
  Pros: Complete auth system, role-based access, Cyberpunk UI
  Cons: No refresh token, hardcoded secrets, no SSO integration
  Reuses: Spring Security patterns, existing Redis, Cyberpunk design system

**APPROACH B: Staff + JWT + Refresh Tokens**
  Summary: Same as A + add refresh token rotation
  Effort: L
  Risk: Med (refresh token storage complexity)
  Pros: Better UX, token rotation security
  Cons: More complexity, Redis storage needed
  Reuses: Extends Approach A

**APPROACH C: OAuth2/SSO Integration**
  Summary: Skip local auth, integrate with enterprise SSO
  Effort: XL
  Risk: High (SSO complexity)
  Pros: Single sign-on, enterprise-ready
  Cons: Vendor lock-in, much more complex
  Reuses: None

**RECOMMENDATION:** Approach A with critical security fixes (env vars, refresh token deferred to Phase 2)

### Step 0D: Mode-Specific Analysis (SELECTIVE EXPANSION)

**Hold Scope Analysis:**
The plan scope (Staff + JWT) is appropriate. The additions (role-based access) are well-scoped.

**Expansion candidates identified:**
1. Add refresh token mechanism (reduces lockout risk)
2. Move secrets to environment variables (security hygiene)
3. Add audit logging for staff changes (security observability)

### Step 0E: Temporal Interrogation

```
HOUR 1: JWT secret exposed in git history → secrets rotate/revoke procedure needed NOW
HOUR 2-3: Staff deletion cascades? → soft delete vs hard delete decision needed
HOUR 4-5: Frontend token storage → localStorage vs httpOnly cookie
HOUR 6+: What happens when JWT expires mid-session? → refresh token or re-login UX
```

### Step 0F: Mode Selection

Mode already set: **SELECTIVE EXPANSION** (per CEO_PLAN.md status ACTIVE)

### CEO Review Sections

**Section 1: Architecture**

```
STAFF AUTH ARCHITECTURE (NEW COMPONENTS):
┌─────────────────────────────────────────────────────────────┐
│  Vue3 Frontend                                              │
│  ├── Login.vue (JWT login, token storage)                   │
│  └── Staff.vue (CRUD, role management)                     │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP + Bearer Token
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Security Filter Chain                               │
│  ├── JwtAuthenticationFilter (OncePerRequest)              │
│  ├── SecurityConfig (stateless, CORS)                       │
│  └── JwtAuthenticationEntryPoint (401 handler)             │
└─────────────────────────────────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌─────────────────┐ ┌─────────────┐ ┌─────────────────┐
│ AuthController   │ │StaffController│ │ TicketController │
│ /api/auth/login  │ │ /api/staff/* │ │ /api/tickets/*   │
│ /api/auth/password│ │             │ │ (existing)       │
└─────────────────┘ └─────────────┘ └─────────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│  StaffRepository ←── Staff entity (new)                    │
│  StaffService (new)                                         │
└─────────────────────────────────────────────────────────────┘
```

**Section 2: Error & Rescue Map**

| Method/Codepath | What Can Go Wrong | Exception Class | Rescued? | User Sees |
|---|---|---|---|---|
| AuthController#login | Invalid phone | BusinessException("AUTH_FAILED") | Y | "手机号或密码错误" |
| AuthController#login | Wrong password | BusinessException("AUTH_FAILED") | Y | "手机号或密码错误" |
| AuthController#login | Suspended account | BusinessException("ACCOUNT_SUSPENDED") | Y | "账号已停用" |
| AuthController#password | Wrong current password | BusinessException("INVALID_PASSWORD") | Y | "当前密码错误" |
| JwtTokenUtil#validateToken | Expired token | returns false | Y (implicit) | 401 redirect |
| JwtTokenUtil#validateToken | Malformed token | returns false | Y (implicit) | 401 redirect |
| StaffService#create | Duplicate phone | BusinessException("DUPLICATE_PHONE") | Y | "手机号已注册" |
| StaffService#create | Duplicate employeeNo | BusinessException("DUPLICATE_EMPLOYEE_NO") | Y | "工号已存在" |
| StaffService#delete | Staff not found | BusinessException("STAFF_NOT_FOUND") | Y | "员工不存在" |
| JwtAuthenticationFilter | No Authorization header | (no exception, continues chain) | Y | Continues unauthenticated |
| GlobalExceptionHandler | Uncaught exception | generic Exception | Y | "系统异常，请稍后重试" |

⚠️ **GAP: No handling for JwtException (expired, malformed, wrong signature)** — validateToken catches all JwtException but returns false silently. No logging of WHY validation failed. Security audit trail missing.

**Section 3: Security & Threat Model**

| Threat | Likelihood | Impact | Mitigated? |
|---|---|---|---|
| Hardcoded JWT secret in application.yml | HIGH | CRITICAL | ❌ NOT MITIGATED — secret in git history forever |
| Hardcoded DB password in application.yml | HIGH | CRITICAL | ❌ NOT MITIGATED — credentials in git |
| Redis has no password | MED | HIGH | ❌ NOT MITIGATED — anyone on network can access |
| Brute force login attack | MED | HIGH | ❌ NOT MITIGATED — no rate limiting on /api/auth/login |
| JWT token theft (XSS) | MED | HIGH | ❌ NOT MITIGATED — token in localStorage vulnerable to XSS |
| Privilege escalation (TICKETER → ADMIN) | LOW | CRITICAL | ✅ Mitigated by @PreAuthorize on all staff endpoints |
| Staff data exposure via IDOR | LOW | MED | ✅ Mitigated — authenticated users only see their own data |
| SQL injection in staff search | LOW | CRITICAL | ✅ Mitigated by JPA parameterized queries |
| CSRF attack on password change | LOW | MED | ⚠️ PARTIAL — CSRF disabled globally (csrf.disable()) |
| Token stored in localStorage | MED | MED | ⚠️ XSS risk — httpOnly cookie preferred |

⚠️ **CRITICAL: JWT secret and DB password are hardcoded in application.yml and will be in git history forever. These MUST be moved to environment variables before merge.**

⚠️ **CRITICAL: No rate limiting on /api/auth/login — vulnerable to brute force attacks.**

**Section 4: Data Flow & Interaction Edge Cases**

| Interaction | Edge Case | Handled? | How? |
|---|---|---|---|
| Login | Double-click submit | ❌ GAP | No button disable on frontend |
| Login | Expired JWT during session | ❌ GAP | No refresh, user must re-login |
| Login | Network failure mid-request | ⚠️ PARTIAL | Frontend shows error, no retry |
| Staff create | Duplicate phone | ✅ | BusinessException |
| Staff create | Password too short | ❌ GAP | No validation in StaffCreateRequest |
| Staff delete | Delete self | ❌ GAP | Can delete own account |
| Staff delete | Delete last ADMIN | ❌ GAP | No protection — system could have zero admins |
| Pagination | Page out of range | ❌ GAP | Returns empty, no error |

**Section 5: Code Quality**

**DRY Violations:**
- StaffResponse and LoginResponse.StaffDto are nearly identical — could share a base DTO
- `BusinessException("STAFF_NOT_FOUND", "员工不存在")` repeated 4 times in StaffService — could be a helper method

**Naming Issues:**
- `BusinessException` is vague — better: `StaffNotFoundException`, `DuplicatePhoneException`
- `StaffCreateRequest.password` field is plaintext — but it's immediately hashed. Name is accurate but consider `passwordPlaintext` for extra clarity

**Complexity:**
- StaffService methods are all < 20 lines ✅
- JwtTokenUtil is simple ✅
- JwtAuthenticationFilter is clean ✅

**Under-engineering:**
- No input validation on password length in StaffCreateRequest
- No pagination validation (negative page numbers?)
- No sorting/filtering in StaffService findAll ( Pageable passed directly)
- StaffController DELETE is idempotent but returns 204 always (even if staff didn't exist — though Service checks first)

**Section 6: Test Review**

**NEW CODEPATHS requiring tests:**
```
1. AuthController.login() — success path
2. AuthController.login() — wrong password
3. AuthController.login() — invalid phone
4. AuthController.login() — suspended account
5. AuthController.changePassword() — success
6. AuthController.changePassword() — wrong current password
7. StaffController.list() — pagination
8. StaffController.create() — success
9. StaffController.create() — duplicate phone
10. StaffController.delete() — success
11. StaffController.delete() — not found
12. StaffController.updateRole() — success
13. StaffController.suspend() / activate()
14. JwtTokenUtil.generateToken()
15. JwtTokenUtil.validateToken() — valid
16. JwtTokenUtil.validateToken() — expired
17. JwtTokenUtil.validateToken() — malformed
18. JwtAuthenticationFilter — valid token
19. JwtAuthenticationFilter — no token
20. JwtAuthenticationFilter — invalid token
```

**TEST GAPS:**
- ❌ No test files exist for new auth/staff code
- ❌ No integration tests for JWT filter chain
- ❌ No tests for StaffService validation logic
- ❌ No frontend tests for Login.vue or Staff.vue

**Section 7: Performance**

- Staff list endpoint uses Pageable — good ✅
- No N+1 query issue (JPA lazy loading not triggered) ✅
- Redis connection pool: max-active 8 — adequate for single instance ⚠️ (not for multi-instance)
- JWT validation is fast (HMAC-SHA) ✅
- No connection pooling issues visible ✅

**Section 8: Observability**

- GlobalExceptionHandler logs business exceptions at WARN level ✅
- GlobalExceptionHandler logs generic exceptions at ERROR with stack trace ✅
- ⚠️ No audit logging for sensitive operations (login success/failure, password change, staff deletion)
- ⚠️ No metrics for login attempts, failures, staff modifications

**Section 9: Deployment**

- Stateless JWT — no sticky session needed ✅
- CORS configured for localhost:5173/5174/3000 — needs to be env-configured ⚠️
- JWT secret must be rotated before production ⚠️
- DB password must be rotated before production ⚠️
- No health check endpoint visible

**Section 10: Rollback**

- Git revert would work for code changes ✅
- JWT tokens issued before secret rotation would be invalid — all users forced re-login ⚠️
- Database schema changes (new staff table) — JPA ddl-auto: update is risky for production ⚠️

---

## Phase 2: Design Review (UI Scope Detected)

### Design Scope Assessment

**Implemented views (from DESIGN.md):**
| View | Status |
|---|---|
| Dashboard | ✅ |
| 票据列表 | ✅ |
| 年/月卡管理 | ✅ |
| OTA订单 | ✅ |
| 入园记录 | ✅ |
| 系统设置 | ⬜ |

**Staff.vue Design Evaluation:**

**Pass 1: Information Hierarchy**
- Page header with title "员工管理" + English subtitle ✅
- Search panel with 3 fields (name, phone, role) ✅
- Data table with 8 columns ✅
- Pagination ✅
- Create dialog ✅
- **Rating: 8/10** — Clear hierarchy, consistent with design system

**Pass 2: Missing States**
- Loading state: ❌ Not shown while fetching
- Empty state: ❌ No empty state message when table is empty
- Error state: ⚠️ Shows "获取员工列表失败" toast but doesn't update UI state
- Submitting state: ✅ submit button disabled during create

**Pass 3: Cyberpunk Design Compliance**
- Uses CSS variables from design system ✅
- Uses correct color palette (--accent: #00F5FF, --danger: #FF2D6A) ✅
- Uses JetBrains Mono for IDs/codes ✅
- Uses Noto Serif SC for text ✅
- Animation delays on table rows ✅
- Dialog enter animation ✅
- **CRITICAL VIOLATION: Uses Element Plus (ElMessage, ElMessageBox)** — DESIGN.md explicitly forbids Element Plus in all views except EntryLogs. Staff.vue should use native HTML dialogs + custom toast.

**Pass 4: Component Inventory Compliance**
- Page header: ✅ matches spec
- Search panel: ✅ matches spec
- Data table: ✅ matches spec
- Status badges: ✅ matches spec
- Action buttons: ✅ matches spec
- Dialog: ✅ matches spec (except for Element Plus)
- Pagination: ✅ matches spec

**Pass 5: Accessibility**
- No keyboard navigation visible
- No ARIA labels on interactive elements
- Focus management in dialog: not implemented

**Pass 6: Responsive Strategy**
- Fixed pixel widths (48px padding) — not responsive
- No mobile breakpoint handling

**Pass 7: Design System Consistency**
- ✅ Colors, typography, spacing match DESIGN.md
- ❌ Element Plus components break consistency

### Design Litmus Scorecard

| Dimension | Claude | Codex | Consensus |
|---|---|---|---|
| 1. Information hierarchy | 8/10 | 7/10 | 7.5/10 |
| 2. Missing states | 4/10 | 5/10 | 4.5/10 |
| 3. Cyberpunk compliance | 5/10 | 6/10 | 5.5/10 (Element Plus violation) |
| 4. Component inventory | 8/10 | 8/10 | 8/10 |
| 5. Accessibility | 3/10 | 3/10 | 3/10 |
| 6. Responsive | 4/10 | 4/10 | 4/10 |

---

## Phase 3: Engineering Review

### Scope Challenge

**Files changed (uncommitted):**
- Backend: Staff.java, StaffRole.java, StaffStatus.java, StaffRepository.java, StaffService.java, StaffController.java, AuthController.java, JwtTokenUtil.java, JwtAuthenticationFilter.java, SecurityConfig.java, GlobalExceptionHandler.java (modified), application.yml (modified), pom.xml (modified)
- Frontend: Login.vue, Staff.vue, router/index.js (modified), api/index.js (modified), layouts/AdminLayout.vue (modified)

**New files count: ~18 files**
**Total impact: MEDIUM** — Auth system is well-scoped but security issues are critical

### Architecture Diagram

```
SPRING BOOT APPLICATION CONTEXT:
┌──────────────────────────────────────────────────────────────┐
│ SecurityConfig                                               │
│ ├── JwtAuthenticationFilter ←── OncePerRequestFilter        │
│ ├── JwtAuthenticationEntryPoint ←── 401 responses           │
│ └── SecurityFilterChain ←── stateless, CORS                 │
└──────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌─────────────────┐   ┌─────────────────┐
│AuthController │    │ StaffController │   │ TicketController │
│/api/auth/*   │    │ /api/staff/*   │   │ /api/tickets/* │
└───────────────┘    └─────────────────┘   └─────────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐    ┌─────────────────┐   ┌─────────────────┐
│StaffRepository│    │ StaffRepository│   │ TicketRepository│
│ (read-only    │    │ (full CRUD)    │   │ (existing)      │
│  for auth)    │    │                │   │                 │
└───────────────┘    └─────────────────┘   └─────────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
     MySQL ←──────────── Staff table ←──────── Ticket tables
```

### Eng Consensus Table

| Dimension | Claude | Codex | Consensus |
|---|---|---|---|
| 1. Architecture sound? | YES (minor) | YES | CONFIRMED |
| 2. Test coverage sufficient? | NO (0 tests) | NO | CONFIRMED — 0 tests |
| 3. Performance risks addressed? | YES | YES | CONFIRMED |
| 4. Security threats covered? | NO (critical gaps) | NO | DISAGREE — security gaps |
| 5. Error paths handled? | MOSTLY | MOSTLY | CONFIRMED |
| 6. Deployment risk manageable? | MED (secrets) | MED | CONFIRMED |

### Critical Eng Findings

**CRITICAL (must fix before merge):**
1. **Secrets in source code** — JWT secret + DB password in application.yml
2. **No rate limiting on /api/auth/login** — brute force vulnerability
3. **No test coverage** — all new code is untested

**HIGH (should fix before Phase 2):**
1. **No refresh token** — users locked out after 24h
2. **No audit logging** — login attempts, staff changes not logged
3. **Element Plus in Staff.vue** — violates design constraint
4. **Delete last ADMIN protection** — system could have zero admins
5. **No password validation** — no min/max length

**MEDIUM:**
1. **No refresh token rotation** — security improvement
2. **No health check endpoint** — deployment observability
3. **CORS origins hardcoded** — should be env-configured
4. **JPA ddl-auto: update in production** — risky

---

## Phase 3.5: Developer Experience Review

### DX Scope Assessment

**Product type:** Internal tool (staff admin + ticket verification)
**Primary developer:** Full-stack developer maintaining this system
**TTHW (Time to Hello World):** ~2 min (mvn spring-boot:run + npm run dev)

### DX Litmus Scorecard

| Dimension | Claude | Codex | Consensus |
|---|---|---|---|
| 1. Getting started < 5 min? | YES (3 min) | YES | CONFIRMED |
| 2. API/CLI naming guessable? | YES | YES | CONFIRMED |
| 3. Error messages actionable? | PARTIAL | PARTIAL | CONFIRMED |
| 4. Docs findable & complete? | PARTIAL | PARTIAL | 5/10 |
| 5. Upgrade path safe? | YES | YES | CONFIRMED |
| 6. Dev environment friction-free? | YES | YES | CONFIRMED |

### DX Findings

**Strengths:**
- REST API naming is consistent and RESTful ✅
-分层架构清晰 (Controller → Service → Repository) ✅
- Spring Boot conventions followed ✅
- No complex build setup ✅

**Gaps:**
- No API documentation (Swagger/OpenAPI) — developers don't know what endpoints exist
- application.yml has hardcoded dev values (DB password, Redis IP) — production values missing
- No DB migration scripts — JPA ddl-auto: update is not production-safe
- No docker-compose for local dev environment
- No README for staff management module (expected endpoints, auth flow)

---

## Decision Audit Trail

| # | Phase | Decision | Classification | Principle | Rationale |
|---|-------|----------|-----------|-----------|----------|
| 1 | CEO | Accept Staff+JWT scope | ACCEPTED | P1 (completeness) | Auth is prerequisite for all Phase 2 features |
| 2 | CEO | Defer refresh token to Phase 2 | DEFERRED | P3 (pragmatic) | 24h JWT is acceptable for internal tool |
| 3 | CEO | Defer SSO to Phase 3+ | DEFERRED | P2 (scope control) | Over-engineering for internal tool |
| 4 | Eng | Move JWT secret to env var | AUTO-DECIDED | P6 (security first) | Hardcoded secrets are critical vulnerability |
| 5 | Eng | Move DB password to env var | AUTO-DECIDED | P6 (security first) | Hardcoded credentials are critical vulnerability |
| 6 | Eng | Add rate limiting to /api/auth/login | AUTO-DECIDED | P6 (security first) | Brute force protection needed |
| 7 | Eng | Add unit tests for auth module | AUTO-DECIDED | P1 (completeness) | 0% test coverage is unacceptable |
| 8 | Design | Replace Element Plus with native toast | AUTO-DECIDED | P5 (explicit over clever) | DESIGN.md explicitly forbids Element Plus |
| 9 | Design | Add loading/empty/error states to Staff.vue | AUTO-DECIDED | P1 (completeness) | Missing states cause poor UX |
| 10 | DX | Add OpenAPI docs | TASTE DECISION | P1 (completeness) | DX improvement, not blocking |

---

## NOT in Scope

Items deferred to TODOS.md:
- Refresh token mechanism (Phase 2)
- SSO integration (Phase 3+)
- OpenAPI documentation (post-merge)
- Health check endpoint (post-merge)
- Docker-compose for local dev (post-merge)
- DB migration scripts (post-merge)

---

## What Already Exists

| Sub-problem | Existing Code |
|---|---|
| JWT token generation/validation | ❌ New — JwtTokenUtil |
| Spring Security filter chain | ❌ New — SecurityConfig |
| Staff entity + repository | ✅ New — follows existing pattern |
| Redis distributed lock | ✅ ticket-system (existing) |
| Global exception handling | ✅ GlobalExceptionHandler (modified) |
| Cyberpunk CSS design system | ✅ AdminLayout.vue (modified) |
| REST API conventions | ✅ existing controllers |

---

## Failure Modes Registry

| Failure Mode | Likelihood | Impact | Mitigation |
|---|---|---|---|
| JWT secret exposed in git history | CRITICAL | HIGH | Rotate secret, move to env var |
| Brute force login attack | MEDIUM | HIGH | Add rate limiting |
| Users locked out after JWT expiry | MEDIUM | MEDIUM | Add refresh token (Phase 2) |
| Element Plus causes design inconsistency | LOW | LOW | Replace with native components |
| No admin left after staff deletions | LOW | CRITICAL | Add "cannot delete last admin" check |
| Redis password-less access | MEDIUM | HIGH | Enable Redis auth |
| XSS steals JWT from localStorage | MEDIUM | HIGH | Use httpOnly cookie (Phase 2) |

---

## Test Plan Artifact

**Location:** `~/.gstack/projects/SoulmateYang-ticketSystem/test-plan-staff-auth-20260526.md`

**To be written:**
```
STAFF AUTH MODULE — TEST PLAN

Unit Tests Required:
1. StaffServiceTest
   - create(): success, duplicate phone, duplicate employeeNo, invalid role
   - delete(): success, not found
   - updateRole(): success, invalid role
   - suspend()/activate(): success
   - findById(): success, not found

2. JwtTokenUtilTest
   - generateToken(): claims populated correctly
   - validateToken(): valid, expired, malformed
   - isTokenExpired(): true, false
   - getStaffIdFromToken(): correct extraction
   - getRoleFromToken(): correct extraction

3. JwtAuthenticationFilterTest
   - valid Bearer token → authentication set
   - no Authorization header → continue chain
   - invalid token → continue chain unauthenticated
   - expired token → continue chain unauthenticated

4. AuthControllerTest
   - POST /api/auth/login success → 200 + token
   - POST /api/auth/login wrong password → 400
   - POST /api/auth/login invalid phone → 400
   - POST /api/auth/login suspended account → 400
   - PUT /api/auth/password success → 200
   - PUT /api/auth/password wrong current → 400

Integration Tests:
1. Full login flow with real DB
2. JWT protected endpoints — authorized + unauthorized
3. Staff CRUD with authentication

Frontend Tests (if added):
1. Login.vue — render, error states
2. Staff.vue — CRUD operations, pagination
```

---

## Cross-Phase Themes

**Theme 1: Secrets in Source Code**
- CEO: flagged as critical
- Eng: flagged as critical
- DX: flagged as DX gap
→ **HIGH CONFIDENCE** — this MUST be fixed before merge

**Theme 2: Authentication UX (no refresh token)**
- CEO: deferred to Phase 2
- Eng: noted as risk
- DX: not blocking
→ **MEDIUM CONFIDENCE** — acceptable for internal tool, fix in Phase 2

---

## Completion Summary

| Phase | Status | Key Findings |
|---|---|---|
| CEO | ✅ COMPLETE | Scope appropriate, auth is prerequisite |
| Design | ✅ COMPLETE (with concerns) | Element Plus violation, missing states |
| Eng | ✅ COMPLETE | Critical security gaps (secrets, rate limiting, 0 tests) |
| DX | ✅ COMPLETE | Good structure, missing docs |

**OVERALL VERDICT:** Plan is sound but implementation has critical security gaps that MUST be fixed before merge.

---

## Critical Fixes Required Before Merge

### P0 (Block Merge):
1. Move `jwt.secret` to environment variable
2. Move `spring.datasource.password` to environment variable
3. Add rate limiting to `/api/auth/login` endpoint
4. Add at least one unit test for core auth flow

### P1 (Fix Before Phase 2):
1. Replace Element Plus with native toast in Staff.vue
2. Add refresh token mechanism
3. Add audit logging for login attempts and staff changes
4. Add "cannot delete last admin" protection
5. Add password length validation

### P2 (Post-Merge Improvements):
1. Enable Redis password authentication
2. Add OpenAPI documentation
3. Add health check endpoint
4. Docker-compose for local dev
5. DB migration scripts (Flyway)

---

## GSTACK REVIEW REPORT

| Run | Date | Verdict | Status | Critical | High | Medium |
|-----|------|---------|--------|----------|------|--------|
| 1 | 2026-05-26 | NEEDS_WORK | issues_open | 3 | 5 | 5 |

---
