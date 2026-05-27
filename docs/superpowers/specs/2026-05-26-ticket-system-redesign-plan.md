# 景区票务系统重构 — 实施计划

Date: 2026-05-26
Based on: 2026-05-26-ticket-system-redesign-design.md
Status: READY

---

## 概述

本次重构在现有系统基础上实施，按依赖关系排序5个Phase。预计总工期 **5-7天**（按每天投入3-4小时估算）。

---

## Phase 1: 数据模型重构

**工期：2天**
**依赖：无**

### Backend

- [ ] 1.1 新增 `Visitor` 实体（id, name, phone, idCard, status, createdAt）
- [ ] 1.2 新增 `VisitorRepository`（JpaRepository）
- [ ] 1.3 新增 `WindowSale` 实体（id, ticketType, quantity, amount, windowId, sellerId, soldAt）
- [ ] 1.4 新增 `WindowSaleRepository`
- [ ] 1.5 新增 `EntryRecord` 实体（id, ticketId, visitorId, entryTime, gate, channel, verifiedBy）
- [ ] 1.6 新增 `EntryRecordRepository`
- [ ] 1.7 修改 `Ticket` 实体：
      - 增加 `windowSaleId`（Long，可空）
      - 增加 `otaOrderId`（Long，可空）
      - `visitorId` 改为可空（年票/月票必填，次票/OTA票可空）
- [ ] 1.8 修改 `TicketRepository`：
      - 新增 `findByVisitorId(Long visitorId)`
      - 新增 `findByWindowSaleId(Long windowSaleId)`
- [ ] 1.9 新增 `TicketType.WALK_IN` 枚举值
- [ ] 1.10 修改 `application.yml`：确保 ddl-auto=validate 或使用 Flyway（生产前必须）

### Database Migration

- [ ] 1.11 编写 Flyway 脚本 `V001__add_visitors_table.sql`
- [ ] 1.12 编写 Flyway 脚本 `V002__add_window_sales_table.sql`
- [ ] 1.13 编写 Flyway 脚本 `V003__add_entry_records_table.sql`
- [ ] 1.14 编写 Flyway 脚本 `V004__modify_tickets_table.sql`（添加外键列、可空visitorId）
- [ ] 1.15 数据迁移：将现有 Ticket 按 type 归类，设置合理的 windowSaleId=NULL / visitorId=NULL

### 前端

- [ ] 1.16 在 `api/index.js` 新增 visitorApi（getAll, getById）

---

## Phase 2: 窗口售票

**工期：1.5天**
**依赖：Phase 1 完成**

### Backend

- [ ] 2.1 新增 `VisitorService.getOrCreate()` — 根据身份证号查找或创建Visitor（年票/月票用）
- [ ] 2.2 新增 `WindowSaleService.sell(WalkInSaleRequest)`
- [ ] 2.3 新增 `WindowSaleController` + `POST /api/window/sell`
- [ ] 2.4 窗口售票创建 Ticket(WALK_IN) + WindowSale，票号用 UUID
- [ ] 2.5 单元测试：WindowSaleService.sell() 正常流程 + 数量校验

### 前端

- [ ] 2.6 新增 `WindowSale.vue` 页面（票种选择+数量+确认）
- [ ] 2.7 在路由中注册 `/window/sale`
- [ ] 2.8 路由守卫：ADMIN/FINANCE/TICKETER 角色可访问

---

## Phase 3: 核销分流

**工期：1天**
**依赖：Phase 1 完成**

### Backend

- [ ] 3.1 修改 `TicketService.verify()` 方法签名：增加 `gate` 参数
- [ ] 3.2 实现 `verifyPass()` — 年票/月票核销逻辑（检查Visitor状态+有效期+入园次数）
- [ ] 3.3 实现 `verifyWalkIn()` — 窗口票核销逻辑（检查当日有效+单次入场）
- [ ] 3.4 实现 `verifyOta()` — OTA票核销逻辑（待OTA对接后完善）
- [ ] 3.5 核销成功后创建 `EntryRecord`（入场事实）
- [ ] 3.6 保留 `EntryLog`（验票事件，结果SUCCESS/FAIL/REJECT）
- [ ] 3.7 `TicketController.verifyTicket()` 更新调用签名

### Database

- [ ] 3.8 确保 `entry_records` 表索引：ticketId, entryTime, visitorId

---

## Phase 4: Visitor 管理

**工期：0.5天**
**依赖：Phase 1 完成**

### Backend

- [ ] 4.1 新增 `VisitorController`
- [ ] 4.2 `GET /api/visitors` — 分页列表（name/phone/idCard搜索）
- [ ] 4.3 `GET /api/visitors/{id}` — 详情（含当前持票）
- [ ] 4.4 `POST /api/visitors` — 手动创建访客档案
- [ ] 4.5 `PUT /api/visitors/{id}` — 更新访客信息
- [ ] 4.6 关联查询：访客详情页显示其所有 Ticket

### 前端

- [ ] 4.7 新增 `Visitors.vue` 页面（列表+详情）
- [ ] 4.8 在路由中注册 `/visitors`

---

## Phase 5: OTA 对接适配

**工期：1天**
**依赖：Phase 1 + Phase 3 完成**

### Backend

- [ ] 5.1 修改 `OtaOrderService.sync()` — 生成 Ticket 时设置 `otaOrderId`
- [ ] 5.2 修改 `OtaOrderService` — 同步成功后创建 EntryLog（sync事件）
- [ ] 5.3 验证 `verifyOta()` 对 OTA 票的核销逻辑

### 前端

- [ ] 5.4 OtaOrders 页面中票号点击可跳转 Ticket 详情（显示来源订单）

---

## 实施顺序图

```
Phase 1 ──────────────────────────────────────────────────► 完成
          │
          ├─► Phase 2（窗口售票）─────► 完成
          │
          ├─► Phase 3（核销分流）────► 完成
          │
          ├─► Phase 4（Visitor管理）─► 完成
          │
          └─► Phase 5（OTA适配）─────► 完成
```

---

## 风险控制

| 风险 | 缓解措施 |
|------|---------|
| 数据迁移丢失 | 迁移前全量备份，Flyway脚本可重复执行（idempotent） |
| 核销逻辑改错影响现有票 | Phase 3 单独分支测试，verifyWalkIn/verifyPass 隔离验证 |
| 前端路由切换导致白屏 | 使用 vue-router 命名路由，不用路径硬跳转 |
| 窗口售票和OTA同时修改 Ticket | WindowSaleId 和 OtaOrderId 互斥，同一Ticket不可能同时有两者 |

---

## 验收标准

- [ ] 窗口可售出 WALK_IN 票，票号可扫码核销
- [ ] 年票/月票核销正确扣减入园次数
- [ ] Visitor 档案可查询并关联其持票
- [ ] EntryRecord 和 EntryLog 同时生成（验票事件 vs 入场事实）
- [ ] 现有数据平滑迁移，无数据丢失