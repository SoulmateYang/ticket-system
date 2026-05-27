# Scenic Ticket Platform — 方案B重构设计

Date: 2026-05-26
Status: PROPOSED
Approach: 局部重构（方案B）

---

## 1. Why — 问题与动机

### 当前系统问题

| # | 问题 | 影响 |
|---|------|------|
| 1 | Ticket表混合了年票/月票/次票/OTA票，`visitorId`字段对次票无意义但非空 | 数据不干净，业务逻辑耦合 |
| 2 | 缺少Visitor抽象，年票会员没有独立档案 | 无法扩展CRM、会员体系 |
| 3 | 窗口售票功能缺失，只能在年票/月票页面创建次票 | 现场售票流程缺失 |
| 4 | TicketType业务逻辑混乱，四种票种没有区分处理 | 运维复杂，易出错 |
| 5 | EntryLog语义不清（验票动作？入场事实？） | 报表口径不一致 |
| 6 | OTA订单→Ticket映射缺失 | 对账困难，无法溯源 |

### 改造目标

1. **票种分流** — 年票/月票/窗口票/OTA票走不同的创建和核销流程
2. **窗口售票** — 现场实时售票，支持当日一次入园票
3. **Visitor体系** — 年票/月票持卡人独立档案管理
4. **入场语义清晰** — 区分验票请求与入场事实两层记录

---

## 2. What Changes — 变更清单

### 新增实体

| 实体 | 说明 |
|------|------|
| `Visitor` | 年票/月票持卡人档案（姓名+手机+身份证+创建时间） |
| `WindowSale` | 窗口售票记录（票种+数量+金额+窗口ID+时间） |
| `EntryRecord` | 入场事实记录（who/when/gate/channel，区分验票事件） |

### 修改实体

| 实体 | 变更 |
|------|------|
| `Ticket` | 增加 `windowSaleId` 可选外键；`visitorId` 改为可选（年票/月票必填，次票/OTA票可空） |
| `EntryLog` | 保留验票事件记录（申请+结果），新增 `EntryRecord` 记录入场事实 |

### 新增接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/visitors` | 访客列表（分页+搜索） |
| GET | `/api/visitors/{id}` | 访客详情（含持票列表） |
| POST | `/api/window/sell` | 窗口实时售票 |
| POST | `/api/window/verify` | 窗口票核销（扫码） |

### 修改接口

| 路径 | 变更 |
|------|------|
| `POST /api/tickets/passes` | 年票/月票创建时强制关联Visitor |
| `POST /api/tickets/verify` | 统一核销入口，根据Ticket.type分流处理 |
| `POST /api/tickets/single` | 改为窗口售票 `WindowSale`，不再直接创建Ticket |

---

## 3. Data Model — 数据模型

### Entity Relationship

```
Visitor (1) ←———— (N) Ticket
                      │
                      ├── YEAR_PASS   (关联Visitor)
                      ├── MONTH_PASS  (关联Visitor)
                      ├── WALK_IN    (来自WindowSale)
                      └── OTA_TICKET  (来自OtaOrder)

OtaOrder (1) ———— (N) Ticket

WindowSale (1) ——— (N) Ticket

Ticket (1) ———— (N) EntryLog   (验票事件)
Ticket (1) ———— (N) EntryRecord (入场事实)
```

### Visitor

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | PK |
| name | String(50) | 姓名 |
| phone | String(20) | 手机号 |
| idCard | String(18) | 身份证号（唯一） |
| status | Enum | ACTIVE / SUSPENDED / CANCELLED |
| createdAt | LocalDateTime | 创建时间 |

### Ticket

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | PK |
| type | TicketType | YEAR_PASS / MONTH_PASS / WALK_IN / OTA_TICKET |
| ticketCode | String(64) | 票号（QR码内容） |
| visitorId | Long | FK → Visitor（年票/月票必填，其他可空） |
| visitorName | String(50) | 持票人姓名（冗余，年票/月票来自Visitor） |
| phone | String(20) | 联系电话 |
| validFrom | LocalDateTime | 有效期起始 |
| validTo | LocalDateTime | 有效期截止 |
| maxEntries | Integer | 最大入园次数（年票/月票） |
| usedEntries | Integer | 已入园次数 |
| status | String(20) | AVAILABLE / USED / EXPIRED / CANCELLED |
| channel | String(20) | WINDOW / MINIAPP / MEITUAN / DOUYIN / CTRIP |
| windowSaleId | Long | FK → WindowSale（窗口售票必填） |
| otaOrderId | Long | FK → OtaOrder（OTA票必填） |
| createdAt | LocalDateTime | 创建时间 |

### WindowSale

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | PK |
| ticketType | TicketType | 售票类型（仅WALK_IN） |
| quantity | Integer | 售出数量 |
| amount | BigDecimal | 售价金额 |
| windowId | String(20) | 窗口编号 |
| sellerId | Long | FK → Staff（售票员工） |
| soldAt | LocalDateTime | 售出时间 |

### EntryRecord

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | PK |
| ticketId | Long | FK → Ticket |
| visitorId | String(18) | 访客身份证（冗余，便于查询） |
| entryTime | LocalDateTime | 入场时间 |
| gate | String(20) | 入口编号 |
| channel | String(20) | 销售渠道 |
| verifiedBy | String(20) | 核销员工ID |

### EntryLog（保持不变）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | PK |
| ticketId | Long | FK → Ticket |
| ticketCode | String(64) | 票号（冗余） |
| entryTime | LocalDateTime | 验票时间 |
| entryType | String(20) | 验票类型 |
| channel | String(20) | 渠道 |
| result | String(20) | SUCCESS / FAIL / REJECT |
| remark | String(200) | 备注 |
| verifiedBy | String(20) | 核销员工ID |
| deviceId | String(50) | 设备ID |

---

## 4. Business Flows — 业务流程

### 4.1 窗口售票（WALK_IN）

```
游客 → 窗口选择票种 + 数量 → 员工售出 → 生成WindowSale + Ticket(WALK_IN)
                                    → Ticket.status = AVAILABLE
                                    → EntryLog记录售出事件
```

**核销：**
```
游客扫码 → verify(ticketCode) → Ticket.type == WALK_IN
         → 检查 validTo >= today
         → 检查 status == AVAILABLE
         → EntryLog(result=SUCCESS) + EntryRecord
         → Ticket.usedEntries++ 或 status=USED（当日票）
```

### 4.2 OTA同步

```
OTA平台推送订单 → OtaOrderService.sync()
→ 生成OtaOrder + Ticket(OTA_TICKET)
→ Ticket.status = AVAILABLE（待激活或直接可用）
→ EntryLog记录同步事件
```

### 4.3 年票/月票

```
游客办理 → 创建Visitor + Ticket(YEAR_PASS/MONTH_PASS)
        → Ticket.visitorId = Visitor.id
        → Ticket.maxEntries = 365/30（年卡/月卡）
        → 激活前 status=PENDING
```

**核销：**
```
扫码 → verify(ticketCode) → Ticket.type == YEAR_PASS/MONTH_PASS
    → 检查 Visitor.status == ACTIVE
    → 检查 validTo >= today
    → 检查 usedEntries < maxEntries
    → EntryLog + EntryRecord
    → Ticket.usedEntries++
```

---

## 5. API Design — 接口设计

### 新增接口

#### `POST /api/window/sell`
窗口实时售票

Request:
```json
{
  "ticketType": "WALK_IN",
  "quantity": 1,
  "amount": 99.00,
  "windowId": "WINDOW-01",
  "validTo": "2026-05-26T23:59:59"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "windowSaleId": 1,
    "tickets": [
      { "id": 101, "ticketCode": "uuid...", "type": "WALK_IN", "status": "AVAILABLE" }
    ]
  }
}
```

#### `POST /api/window/verify`
窗口票核销（扫码枪触发）

Request:
```json
{
  "ticketCode": "uuid-xxx",
  "verifiedBy": "STAFF001",
  "deviceId": "SCANNER-01",
  "gate": "GATE-A"
}
```

Response:
```json
{
  "success": true,
  "code": "SUCCESS",
  "ticketCode": "uuid-xxx",
  "ticketType": "WALK_IN",
  "visitorName": null,
  "entryTime": "2026-05-26T14:30:00"
}
```

#### `GET /api/visitors`
访客列表

Params: `page, size, name, phone, idCard`

#### `GET /api/visitors/{id}`
访客详情（含所有持票）

---

## 6. Architecture — 架构

### 模块划分

```
src/main/java/com/scenic/ticket/
├── model/              # 实体
│   ├── Visitor.java   (NEW)
│   ├── Ticket.java     (MODIFIED)
│   ├── WindowSale.java (NEW)
│   ├── EntryRecord.java (NEW)
│   └── EntryLog.java   (KEEP)
├── repository/
│   ├── VisitorRepository.java     (NEW)
│   ├── WindowSaleRepository.java  (NEW)
│   ├── EntryRecordRepository.java (NEW)
│   └── TicketRepository.java      (MODIFIED)
├── service/
│   ├── VisitorService.java    (NEW)
│   ├── WindowSaleService.java (NEW)
│   ├── TicketService.java     (MODIFIED — 分流处理)
│   └── OtaOrderService.java   (MODIFIED)
├── controller/
│   ├── VisitorController.java    (NEW)
│   ├── WindowSaleController.java (NEW)
│   └── TicketController.java    (MODIFIED)
└── dto/
    ├── WindowSaleRequest.java  (NEW)
    └── ...
```

### TicketService.verify() 分流逻辑

```java
public VerifyResult verifyTicket(String ticketCode, String verifiedBy, String deviceId, String gate) {
    Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
            .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "票不存在"));

    return switch (ticket.getType()) {
        case YEAR_PASS, MONTH_PASS -> verifyPass(ticket, verifiedBy, deviceId, gate);
        case WALK_IN -> verifyWalkIn(ticket, verifiedBy, deviceId, gate);
        case OTA_TICKET -> verifyOta(ticket, verifiedBy, deviceId, gate);
    };
}
```

---

## 7. Implementation Phases — 实施阶段

### Phase 1: 数据模型重构（1-2天）
- 新增 Visitor / WindowSale / EntryRecord 实体
- 修改 Ticket 表结构（外键+可选字段调整）
- 新增 Repository
- 数据迁移脚本（将现有Ticket按type归类）

### Phase 2: 窗口售票（1-2天）
- WindowSaleService + Controller
- `POST /api/window/sell`
- 前端窗口售票页面

### Phase 3: 核销分流（1天）
- TicketService.verify() 按type分流
- EntryRecord 生成
- 前端验票界面适配

### Phase 4: Visitor管理（0.5天）
- VisitorService + Controller
- 前端访客管理页面

### Phase 5: OTA对接（1天）
- OtaOrderService 适配新模型
- Order → Ticket 关联

---

## 8. Backward Compatibility — 向后兼容

- 现有 `POST /api/tickets/single` 接口保留，指向 WindowSale
- 现有 Ticket 数据通过 `windowSaleId=NULL, visitorId=NULL` 区分历史数据
- 前端无需强制刷新，渐进迁移

---

## 9. Open Questions

1. **WindowSale 是否需要支付流水？** 当前简化版不含支付，后续可扩展 paymentId 外键
2. **OTA订单同步的触发方式？** 定时轮询 vs Webhook？当前是定时轮询
3. **Visitor 身份信息是否需要实名认证？** 当前为可选字段
4. **EntryRecord 是否需要关联 Staff？** 当前加了 verifiedBy 字段冗余存储

---

## 10. Non-Goals

- 不实现完整CRM（积分、等级、优惠券）
- 不实现人脸识别验票（Phase 2+ 硬件相关）
- 不实现完整财务报表（对账功能后期单独做）