## 1. Bug Fixes

- [x] 1.1 修复 PassList.vue 的 createForm 字段：`idCard` → `visitorId`
- [x] 1.2 修复 TicketList.vue 次票创建前端校验：检查 quantity > 0 和 performanceName 非空
- [x] 1.3 修复 PassList.vue catch 块：显示 `e.response?.data?.message` 而非固定文案
- [x] 1.4 修复 TicketList.vue catch 块：显示 `e.response?.data?.message` 而非固定文案（已在1.2中一并修复）

## 2. Backend Enhancement

- [x] 2.1 在 TicketController 添加 `GET /api/tickets/passes` 分页列表接口
- [x] 2.2 在 TicketService 添加 `findAllPasses(Pageable)` 方法

## 3. Verification

- [x] 3.1 启动后端，验证年/月卡创建成功
- [x] 3.2 启动后端，验证次票创建成功
- [x] 3.3 验证错误信息能正确显示给用户
- [x] 3.4 验证年/月卡列表能正确分页加载
