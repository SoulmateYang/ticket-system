# 景区票务系统重构 Phase 1 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成数据模型重构，新增 Visitor/WindowSale/EntryRecord 实体，修改 Ticket 表结构，新增 Flyway 迁移脚本。

**Architecture:** Phase 1 专注数据库Schema变更。新增3个实体（Visitor/WindowSale/EntryRecord），修改1个实体（Ticket），按现有分层架构（model/repository/service/controller）组织代码。

**Tech Stack:** Spring Boot 3.2 + Spring Data JPA + Flyway + MySQL 8.0

---

## 文件结构

```
Backend — 新增文件
- src/main/java/com/scenic/ticket/model/Visitor.java          (NEW)
- src/main/java/com/scenic/ticket/model/WindowSale.java        (NEW)
- src/main/java/com/scenic/ticket/model/EntryRecord.java       (NEW)
- src/main/java/com/scenic/ticket/repository/VisitorRepository.java    (NEW)
- src/main/java/com/scenic/ticket/repository/WindowSaleRepository.java (NEW)
- src/main/java/com/scenic/ticket/repository/EntryRecordRepository.java (NEW)
- src/main/resources/db/migration/V001__add_visitors_table.sql          (NEW)
- src/main/resources/db/migration/V002__add_window_sales_table.sql     (NEW)
- src/main/resources/db/migration/V003__add_entry_records_table.sql    (NEW)
- src/main/resources/db/migration/V004__modify_tickets_table.sql       (NEW)

Backend — 修改文件
- src/main/java/com/scenic/ticket/model/TicketType.java:10-24   (增加 WALK_IN 枚举值)
- src/main/java/com/scenic/ticket/model/Ticket.java              (新增 windowSaleId, otaOrderId 字段)
- src/main/java/com/scenic/ticket/repository/TicketRepository.java (新增 findByVisitorId, findByWindowSaleId)
- src/main/java/com/scenic/ticket/service/TicketService.java   (logEntry 参数增加 ticketId Long)
- src/main/resources/application.yml                             (启用 Flyway, 配置 ddl-auto=none)

Frontend
- src/api/index.js (新增 visitorApi)
```

---

## Task 1: 添加 TicketType.WALK_IN 枚举值

**Files:**
- Modify: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/model/TicketType.java:10-24`

**Steps:**

- [ ] **Step 1: 添加 WALK_IN 枚举值**

```java
public enum TicketType {
    YEAR_PASS("年票"),
    MONTH_PASS("月票"),
    SINGLE_USE("次票"),
    WALK_IN("窗口票"),    // 新增：窗口即时售票
    OTA_TICKET("OTA票");

    private final String description;

    TicketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/scenic/ticket/model/TicketType.java
git commit -m "feat: add TicketType.WALK_IN for window sales"
```

---

## Task 2: 新增 Visitor 实体

**Files:**
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/model/Visitor.java`
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/repository/VisitorRepository.java`

**Steps:**

- [ ] **Step 1: 创建 Visitor 实体**

```java
package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 年票/月票持卡人档案
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visitors", indexes = {
    @Index(name = "idx_id_card", columnList = "idCard", unique = true),
    @Index(name = "idx_phone", columnList = "phone")
})
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 身份证号（唯一） */
    @Column(nullable = false, unique = true, length = 18)
    private String idCard;

    /** 状态：ACTIVE / SUSPENDED / CANCELLED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建 VisitorRepository**

```java
package com.scenic.ticket.repository;

import com.scenic.ticket.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByIdCard(String idCard);
    Optional<Visitor> findByPhone(String phone);
    boolean existsByIdCard(String idCard);
}
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/scenic/ticket/model/Visitor.java
git add src/main/java/com/scenic/ticket/repository/VisitorRepository.java
git commit -m "feat: add Visitor entity for annual/monthly pass holders"
```

---

## Task 3: 新增 WindowSale 实体

**Files:**
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/model/WindowSale.java`
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/repository/WindowSaleRepository.java`

**Steps:**

- [ ] **Step 1: 创建 WindowSale 实体**

```java
package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 窗口售票记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "window_sales", indexes = {
    @Index(name = "idx_window_id", columnList = "windowId"),
    @Index(name = "idx_sold_at", columnList = "soldAt")
})
public class WindowSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 票种类型（仅 WALK_IN） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    /** 售出数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 售价金额 */
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /** 窗口编号 */
    @Column(length = 20)
    private String windowId;

    /** 售票员工ID（冗余存储） */
    @Column(length = 20)
    private String sellerId;

    /** 售出时间 */
    @Column(nullable = false)
    private LocalDateTime soldAt;

    @PrePersist
    protected void onCreate() {
        if (soldAt == null) soldAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建 WindowSaleRepository**

```java
package com.scenic.ticket.repository;

import com.scenic.ticket.model.WindowSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WindowSaleRepository extends JpaRepository<WindowSale, Long> {
}
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/scenic/ticket/model/WindowSale.java
git add src/main/java/com/scenic/ticket/repository/WindowSaleRepository.java
git commit -m "feat: add WindowSale entity for walk-in ticket sales"
```

---

## Task 4: 新增 EntryRecord 实体

**Files:**
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/model/EntryRecord.java`
- Create: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/repository/EntryRecordRepository.java`

**Steps:**

- [ ] **Step 1: 创建 EntryRecord 实体**

```java
package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 入场事实记录
 * 区别于 EntryLog（验票事件），EntryRecord 记录实际入场行为
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "entry_records", indexes = {
    @Index(name = "idx_er_ticket_id", columnList = "ticketId"),
    @Index(name = "idx_er_entry_time", columnList = "entryTime"),
    @Index(name = "idx_er_visitor_id", columnList = "visitorId")
})
public class EntryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联票ID */
    @Column(nullable = false)
    private Long ticketId;

    /** 访客身份证（冗余存储，便于查询） */
    @Column(length = 18)
    private String visitorId;

    /** 入场时间 */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /** 入口/闸机编号 */
    @Column(length = 20)
    private String gate;

    /** 渠道 */
    @Column(length = 20)
    private String channel;

    /** 核销员工ID（冗余存储） */
    @Column(length = 20)
    private String verifiedBy;

    @PrePersist
    protected void onCreate() {
        if (entryTime == null) entryTime = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建 EntryRecordRepository**

```java
package com.scenic.ticket.repository;

import com.scenic.ticket.model.EntryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntryRecordRepository extends JpaRepository<EntryRecord, Long> {
    List<EntryRecord> findByTicketId(Long ticketId);
    List<EntryRecord> findByVisitorId(String visitorId);
}
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/scenic/ticket/model/EntryRecord.java
git add src/main/java/com/scenic/ticket/repository/EntryRecordRepository.java
git commit -m "feat: add EntryRecord entity for entry facts"
```

---

## Task 5: 修改 Ticket 实体（新增外键字段 + visitorId 可空）

**Files:**
- Modify: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/model/Ticket.java`

**Steps:**

- [ ] **Step 1: 在 Ticket.java 中新增字段**

在 `private String faceTemplate;` 之后添加：

```java
/**
 * 关联窗口售出记录（窗口售票必填，其他票种可空）
 */
@Column
private Long windowSaleId;

/**
 * 关联OTA订单（OTA票必填，其他票种可空）
 */
@Column
private Long otaOrderId;
```

同时将 `visitorId` 字段的 `@Column(nullable = false)` 改为可空：

```java
/**
 * 访客ID（身份证号，年票/月票必需，次票/OTA票可空）
 */
@Column(length = 18)
private String visitorId;
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/scenic/ticket/model/Ticket.java
git commit -m "feat: add windowSaleId and otaOrderId to Ticket, make visitorId optional"
```

---

## Task 6: 修改 TicketRepository（新增查询方法）

**Files:**
- Modify: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/repository/TicketRepository.java`

**Steps:**

- [ ] **Step 1: 在 TicketRepository 接口末尾添加两个方法**

```java
List<Ticket> findByVisitorId(Long visitorId);

List<Ticket> findByWindowSaleId(Long windowSaleId);
```

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/scenic/ticket/repository/TicketRepository.java
git commit -m "feat: add findByVisitorId and findByWindowSaleId to TicketRepository"
```

---

## Task 7: 配置 Flyway 并编写数据库迁移脚本

**Files:**
- Modify: `ticket-system-admin/backend/src/main/resources/application.yml`
- Create: `ticket-system-admin/backend/src/main/resources/db/migration/V001__add_visitors_table.sql`
- Create: `ticket-system-admin/backend/src/main/resources/db/migration/V002__add_window_sales_table.sql`
- Create: `ticket-system-admin/backend/src/main/resources/db/migration/V003__add_entry_records_table.sql`
- Create: `ticket-system-admin/backend/src/main/resources/db/migration/V004__modify_tickets_table.sql`

**Steps:**

- [ ] **Step 1: 修改 application.yml 配置 Flyway**

找到 spring.jpa.hibernate.ddl-auto 相关配置，改为：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate   # 生产用 validate，由 Flyway 管理 schema
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

注意：开发环境临时测试可保持 `ddl-auto: update`，但迁移脚本需在生产环境使用前执行。

- [ ] **Step 2: 创建 V001__add_visitors_table.sql**

```sql
CREATE TABLE visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    id_card VARCHAR(18) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    INDEX idx_id_card (id_card),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 3: 创建 V002__add_window_sales_table.sql**

```sql
CREATE TABLE window_sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10,2),
    window_id VARCHAR(20),
    seller_id VARCHAR(20),
    sold_at DATETIME NOT NULL,
    INDEX idx_window_id (window_id),
    INDEX idx_sold_at (sold_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 4: 创建 V003__add_entry_records_table.sql**

```sql
CREATE TABLE entry_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    visitor_id VARCHAR(18),
    entry_time DATETIME NOT NULL,
    gate VARCHAR(20),
    channel VARCHAR(20),
    verified_by VARCHAR(20),
    INDEX idx_er_ticket_id (ticket_id),
    INDEX idx_er_entry_time (entry_time),
    INDEX idx_er_visitor_id (visitor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 5: 创建 V004__modify_tickets_table.sql**

```sql
-- 添加 window_sale_id 和 ota_order_id 外键列
ALTER TABLE tickets
    ADD COLUMN window_sale_id BIGINT,
    ADD COLUMN ota_order_id BIGINT,
    ADD INDEX idx_ticket_window_sale (window_sale_id),
    ADD INDEX idx_ticket_ota_order (ota_order_id);

-- 将 visitor_id 改为可空（已有数据中次票/OTA票的 visitor_id 设为 NULL）
UPDATE tickets SET visitor_id = NULL WHERE visitor_id = '' OR visitor_id IS NOT NULL AND ticket_type IN ('SINGLE_USE', 'OTA_TICKET');
```

Run: `cd ticket-system-admin/backend && mvn flyway:migrate -q` (开发环境测试迁移)
Expected: 每条SQL执行成功

- [ ] **Step 6: 提交**

```bash
git add src/main/resources/application.yml
git add src/main/resources/db/migration/
git commit -m "feat: add Flyway migration scripts for Phase 1"
```

---

## Task 8: 前端新增 Visitor API

**Files:**
- Modify: `ticket-system-admin/frontend/src/api/index.js`

**Steps:**

- [ ] **Step 1: 在 api/index.js 中新增 visitorApi**

在 staffApi 定义之后添加：

```javascript
// Visitor APIs
export const visitorApi = {
  getAll: (params) => apiClient.get('/visitors', { params }),
  getById: (id) => apiClient.get(`/visitors/${id}`)
}
```

Run: `cd ticket-system-admin/frontend && npm run build 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 2: 提交**

```bash
git add src/api/index.js
git commit -m "feat: add visitorApi to frontend"
```

---

## Task 9: 修改 TicketService.logEntry 签名（传入 ticketId）

**Files:**
- Modify: `ticket-system-admin/backend/src/main/java/com/scenic/ticket/service/TicketService.java:280-294`

**Steps:**

- [ ] **Step 1: 更新 logEntry 方法签名**

修改方法签名为：
```java
private void logEntry(Long ticketId, String ticketCode, String visitorId, String ticketType,
                      String result, String remark, String verifiedBy, String deviceId)
```

确保 `.ticketId(ticketId)` 在 EntryLog.builder() 中被设置。

Run: `cd ticket-system-admin/backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/scenic/ticket/service/TicketService.java
git commit -m "fix: add ticketId to logEntry method signature"
```

---

## 实施顺序

```
Task 1 (WALK_IN) → Task 2 (Visitor) → Task 3 (WindowSale) → Task 4 (EntryRecord)
    → Task 5 (Ticket 修改) → Task 6 (TicketRepository) → Task 7 (Flyway) → Task 8 (前端API) → Task 9 (logEntry)
```

---

## 验收标准

- [ ] `TicketType.WALK_IN` 枚举值存在
- [ ] `Visitor` / `WindowSale` / `EntryRecord` 三个新实体可正常 JPA 持久化
- [ ] `Ticket` 表新增 `window_sale_id` 和 `ota_order_id` 列，`visitor_id` 已允许 NULL
- [ ] Flyway 迁移脚本可在干净数据库上执行成功
- [ ] 前端 `visitorApi` 可调用 `GET /api/visitors`
- [ ] 所有代码 `mvn compile` 通过