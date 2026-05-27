# 年/月卡有效期字段及次票分页查询 功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提交两组变更：1) 前端年/月卡创建表单增加有效期字段 2) 后端新增次票分页查询接口

**Architecture:** 前端在创建年/月卡时增加 validFrom/validTo 日期选择；后端新增 GET /api/tickets 分页查询接口返回所有票据（次票）

**Tech Stack:** Spring Boot 3.2 + Java 17 + Vue 3 + Element Plus

---

## 文件结构

```
backend/src/main/java/com/scenic/ticket/
├── controller/
│   └── TicketController.java          # 新增 getAllTickets 方法
├── service/
│   └── TicketService.java             # 已实现 findAllTickets 方法（无改动）
└── repository/
    └── TicketRepository.java          # 使用 JPA findAll 默认方法

frontend/src/
└── views/
    └── PassList.vue                    # 创建表单增加 validFrom/validTo 字段
```

---

## Task 1: 提交后端分页查询次票接口

**Files:**
- Modify: `backend/src/main/java/com/scenic/ticket/controller/TicketController.java:122-136`

- [ ] **Step 1: 验证代码变更正确性**

检查 `TicketController.java` 第 122-136 行新增的 `getAllTickets` 方法：

```java
/**
 * 分页查询所有票据（次票）
 */
@GetMapping
public ResponseEntity<Map<String, Object>> getAllTickets(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    Page<TicketResponse> tickets = ticketService.findAllTickets(PageRequest.of(page, size));
    return ResponseEntity.ok(Map.of(
            "success", true,
            "data", tickets.getContent(),
            "totalElements", tickets.getTotalElements(),
            "totalPages", tickets.getTotalPages()
    ));
}
```

- [ ] **Step 2: 验证 TicketService.findAllTickets 实现**

检查 `TicketService.java:240-243` 方法：

```java
public Page<TicketResponse> findAllTickets(Pageable pageable) {
    Page<Ticket> page = ticketRepository.findAll(pageable);
    return page.map(this::toResponse);
}
```

**注意：** `findAll()` 返回所有票据类型（年票、月卡、次票），如有需要可过滤 `TicketType.SINGLE_USE` 类型。

- [ ] **Step 3: 提交后端变更**

```bash
git add backend/src/main/java/com/scenic/ticket/controller/TicketController.java
git commit -m "feat: add pagination endpoint for single-use tickets

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 2: 提交前端年/月卡有效期字段

**Files:**
- Modify: `frontend/src/views/PassList.vue:83-84,107,132-140`

- [ ] **Step 1: 验证模板中新增的日期字段**

检查 `PassList.vue:83-84` 创建对话框中的表单字段：

```html
<div class="form-field"><label>有效起始日</label><input v-model="createForm.validFrom" type="date" /></div>
<div class="form-field"><label>有效截止日</label><input v-model="createForm.validTo" type="date" /></div>
```

- [ ] **Step 2: 验证 createForm 初始化**

检查 `PassList.vue:107` 表单数据结构：

```javascript
const createForm = reactive({ visitorName: '', phone: '', visitorId: '', type: 'YEAR_PASS', validFrom: '', validTo: '' })
```

- [ ] **Step 3: 验证 handleCreate 方法的表单处理**

检查 `PassList.vue:132-140`：

```javascript
const handleCreate = async () => {
  if (!createForm.visitorName || !createForm.visitorId || !createForm.validFrom || !createForm.validTo) {
    ElMessage.error('请填写完整信息')
    return
  }
  const payload = { ...createForm, validFrom: createForm.validFrom + 'T00:00:00', validTo: createForm.validTo + 'T23:59:59' }
  submitting.value = true
  try { await ticketStore.createPass(payload); ElMessage.success('创建成功'); showCreateDialog.value = false; fetchPasses() }
  catch (e) { ElMessage.error(e.response?.data?.message || e.response?.data?.error || '创建失败') } finally { submitting.value = false }
}
```

- [ ] **Step 4: 提交前端变更**

```bash
git add frontend/src/views/PassList.vue
git commit -m "feat: add validFrom/validTo fields to pass creation form

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## 自检清单

**1. Spec 覆盖检查：**
- [x] 后端分页查询接口 - Task 1
- [x] 前端创建表单有效期字段 - Task 2
- [ ] 建议：考虑在 `findAllTickets` 中过滤 `TicketType.SINGLE_USE` 而非返回所有类型票据（待确认需求）

**2. 占位符扫描：** 无 TBD/TODO/placeholder

**3. 类型一致性检查：**
- `TicketResponse` 在 Controller 返回类型正确
- `PageRequest.of(page, size)` 参数类型正确
- `createForm.validFrom/validTo` 与后端 `LocalDateTime` 格式转换正确

---

## 执行选项

**1. Subagent-Driven (recommended)** - 我dispatch fresh subagent per task，任务间review，快速迭代

**2. Inline Execution** - 在当前session执行，使用 executing-plans，批量执行带检查点

**Which approach?**