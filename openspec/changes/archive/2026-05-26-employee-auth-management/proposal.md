## Why

票务系统当前无登录验证，任何人都可以直接访问管理后台，存在严重安全隐患。员工管理功能缺失导致无法区分不同角色的操作权限，无法追溯具体操作人。需要添加员工账号体系和登录验证机制，确保系统安全可控。

## What Changes

- 新增员工（Staff）数据模型：工号、姓名、手机号、密码哈希、角色、状态、创建/更新时间
- 新增登录认证接口：POST `/api/auth/login`，支持手机号+密码登录，返回 JWT Token
- 新增员工管理接口：CRUD 操作（创建、查询、更新、删除）
- 新增角色权限体系：ADMIN（管理员）、TICKETER（验票员）、FINANCE（财务）、OTA（OTA运营）
- 所有管理接口添加 JWT 鉴权中间件
- 密码使用 BCrypt 加密存储
- 当前管理前端无需登录即可访问，改造为必须先登录

## Capabilities

### New Capabilities
- `staff-auth`: 员工账号注册、登录、Token 刷新、密码修改
- `staff-management`: 员工 CRUD、角色分配、状态管理（启用/禁用）
- `role-permission`: 基于角色的权限控制，不同角色访问不同接口
- `jwt-middleware`: 全局 JWT 验证中间件，保护所有管理接口

### Modified Capabilities
- （无现有 specs 目录，无需修改现有能力）

## Impact

- **后端**：新增 `Staff` 实体、`AuthController`、`StaffController`、JWT 工具类、BCrypt 密码工具、SecurityConfig 配置
- **前端**：登录页、员工管理页、请求拦截器（自动携带 Token）、登录状态管理
- **数据库**：新增 `staff` 表，含索引和密码字段
- **依赖**：引入 `jjwt`（JWT 处理）、`spring-security-crypto`（BCrypt）