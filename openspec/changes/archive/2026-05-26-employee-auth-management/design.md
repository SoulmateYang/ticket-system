## Context

票务系统 Phase 1 已完成基础功能（年/月卡管理、次票QR核销、OTA订单同步），但管理后台完全开放，无访问控制。任何人知道后台地址即可操作，存在数据安全和审计风险。

**当前状态**：
- 后台所有接口无鉴权，前端路由无登录态校验
- 无员工账号概念，无法区分操作人
- 密码等敏感信息明文或未加密

**约束**：
- 现有系统不停服，需要平滑迁移
- 已有数据不受影响，新认证系统不影响旧功能
- 前端需适配 Cyberpunk UI 风格

## Goals / Non-Goals

**Goals:**
- 员工账号体系（CRUD）+ 手机号+密码登录
- JWT Token 无状态认证，保护所有 `/api/**` 接口
- 基于角色的权限控制（ADMIN/TICKETER/FINANCE/OTA）
- 密码 BCrypt 加密存储，不可逆
- 前端登录页 + Token 自动携带

**Non-Goals:**
- 不实现 OAuth2 / SSO / 第三方登录
- 不实现细粒度资源级权限（角色即权限，不做字段级控制）
- 不做双因素认证
- 不做登录失败锁定（后续迭代）

## Decisions

### 决策 1：JWT vs Session
**选择：JWT**

- 无状态，扩展性好，天然适合分布式
- Token 包含用户身份信息，无需每次查库验证
- Spring Security 生态成熟，支持好

**替代方案**：
- Session：适合单体，但无法水平扩展，需要 Redis 等外部存储

### 决策 2：BCrypt 密码哈希
**选择：Spring Security Crypto BCryptPasswordEncoder**

- 内置，无需引入额外库
- 自动加盐，防彩虹表攻击

### 决策 3：角色模型
**选择：单角色（Staff.role）**

- 简单实用，满足当前需求
- 每人一个角色，通过角色判断接口权限

```
ADMIN: 所有接口
TICKETER: 验票相关接口
FINANCE: 财务数据只读
OTA: OTA订单管理
```

### 决策 4：登录接口路径
**选择：POST `/api/auth/login`**

- 清晰简洁，认证相关走 auth 前缀
- Token 有效期 24 小时

### 决策 5：数据库 Schema
**新增 `staff` 表**：
```sql
CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_no VARCHAR(32) UNIQUE NOT NULL,  -- 工号
    name VARCHAR(64) NOT NULL,                -- 姓名
    phone VARCHAR(16) UNIQUE NOT NULL,        -- 手机号（登录账号）
    password_hash VARCHAR(255) NOT NULL,      -- BCrypt 密码
    role VARCHAR(32) NOT NULL,               -- ADMIN/TICKETER/FINANCE/OTA
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_employee_no (employee_no)
);
```

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 前端登录态泄漏 | Token 仅存 localStorage，HTTPS 传输 |
| 忘记密码无找回 | 管理员可重置（后续加邮件/短信） |
| JWT 泄露 | Token 有效期 24h，不过期不续期 |
| 水平扩展 Session 问题 | JWT 无状态，无此问题 |

## Migration Plan

**Phase 1（认证骨架）**：
1. 引入 jjwt、spring-security-crypto 依赖
2. 创建 Staff 实体和 Repository
3. 实现 BCrypt 密码工具和 JWT 工具类
4. 实现 AuthController（login 接口）
5. 配置 Spring Security 白名单（/api/auth/login 免鉴权）
6. 单元测试覆盖

**Phase 2（鉴权中间件 + 前端）**：
1. 实现 JwtAuthenticationFilter
2. 配置 SecurityConfig 启用全局鉴权
3. 开发前端登录页（Cyberpunk 风格）
4. 前端请求拦截器携带 Token
5. 员工管理 CRUD 页面
6. 端到端测试

**回滚方案**：注释掉 SecurityConfig 中的 `.addFilterBefore(jwtFilter, ...)` 即可临时禁用鉴权，恢复无认证状态。

## Open Questions

1. **初始管理员账号如何创建？** — 数据库初始化 SQL 插入，或启动时自动创建一个默认 ADMIN 账号（admin/admin123）
2. **Token 刷新机制？** — 当前设计 Token 有效期 24h，过期需重新登录。是否需要 Refresh Token？
3. **验票端是否也用同一套认证？** — 否，验票端使用独立设备，未来可能用硬件绑定或人脸识别