## Context

次票（QR码票）和年/月票创建功能保存失败，同时操作结果的错误/成功提示用户无法清楚感知。

### 已发现的问题

1. **Pass创建字段名不匹配**：`PassList.vue` 的 `createForm` 发送 `idCard` 字段，但 `TicketCreateRequest` 期望 `visitorId`（第29行），导致后端 `visitorId` 为 null，触发 `@NotBlank` 校验失败

2. **次票创建校验无感知**：`SingleTicketCreateRequest` 有 `@NotBlank`（performanceName）和 `@NotNull`（quantity）校验，但前端表单未做预校验，且后端返回 400 错误时前端只显示"创建失败"，不显示具体校验错误信息

3. **弹窗不明显**：`catch` 块只显示固定文案，无法区分是网络错误、服务器错误还是业务错误

### 后端接口现状

- `POST /api/tickets/passes` — 创建年/月票 ✓
- `GET /api/tickets/passes` — **不存在**（只有 `GET /api/tickets/passes/visitor/{visitorId}`）
- `POST /api/tickets/single` — 批量创建次票 ✓

## Goals / Non-Goals

**Goals:**
- 修复 Pass 创建时 `idCard` → `visitorId` 字段映射问题
- 修复次票创建前端校验，确保必填字段有值才发请求
- 优化错误处理，将后端返回的具体错误信息展示给用户
- 添加 `GET /api/tickets/passes` 列表接口（分页）
- 年/月卡列表能正确加载显示

**Non-Goals:**
- 不改动现有后端校验逻辑（只改前端匹配）
- 不改动数据库模型

## Decisions

1. **前端字段修正**：`PassList.vue` 的 `createForm` 中 `idCard` 字段重命名为 `visitorId`，对应后端 DTO

2. **前端预校验**：次票创建时，若 `performanceName` 为空或 `quantity` <= 0，先用 `ElMessage.error()` 提示具体错误，不发请求

3. **错误信息透传**：Catch 块中优先使用 `e.response?.data?.message`（后端返回的错误信息），fallback 到固定文案

4. **新增 Pass 列表接口**：在 `TicketController` 添加 `GET /api/tickets/passes` 分页接口，对应 `GET /tickets/passes`（复用现有的 `findAll` 或新建方法）

## Risks / Trade-offs

- 后端 DTO `visitorId` 实际存的是身份证号，命名不够直观但已成型，暂不改
- 添加列表接口需要确认是否有安全要求（是否需要 ADMIN 角色）
