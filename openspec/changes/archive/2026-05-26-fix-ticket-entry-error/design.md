## Context

`TicketService.verify()` 和 `TicketService.createSingleTickets()` 方法调用 `logEntry()` 记录入场日志。当前 `logEntry()` 方法使用 `ticketCode` 作为标识符，但 `EntryLog` 表同时有 `ticketId`（Long）和 `ticketCode`（String）两个字段，`ticketId` 不可为 null，且 `TicketService.verify()` 返回的是 `Optional<Ticket>`，存在潜在的 NPE 风险。

## Goals / Non-Goals

**Goals:**
- 修复 `logEntry()` 调用时 `ticketId` 为 null 导致的数据库约束违反
- 修复 `TicketService.verify()` 中 Optional 未正确处理导致的潜在 NPE
- 确保验票流程中 `EntryLog` 记录能正确保存 ticketId

**Non-Goals:**
- 不改变 `EntryLog` 表结构
- 不修改 `EntryLogRepository` 的接口定义（当前已正确继承 JpaRepository）

## Decisions

### 1. 修复 `logEntry()` 的 ticketId 传递问题

**问题：** 当前 `logEntry(ticketCode, visitorId, ticketType, ...)` 方法只接收 `ticketCode`（String），无法获取 `ticketId`（Long）。

**决策：** 将 `logEntry()` 方法签名改为接收 `ticket` 对象或显式传入 `ticketId`。在 `verify()` 中，`ticket` 变量是已查询到的 `Ticket` 实体，其 `getId()` 可直接使用。

**替代方案考虑：**
- 直接在 `verify()` 方法内调用 `entryLogRepository.save()` 而不通过 `logEntry()` 封装 → 代码重复，不推荐
- 修改 `EntryLog.ticketId` 改为 nullable → 破坏数据完整性，不推荐

### 2. 修复 Optional 处理

**问题：** `verify()` 中 `ticketRepository.findByTicketCode(ticketCode)` 返回 `Optional<Ticket>`，当前代码直接 `.get()` 可能导致 NPE。

**决策：** 使用 `orElseThrow()` 在找不到票时抛出明确的业务异常，由上层处理并返回相应的错误结果。

## Risks / Trade-offs

- [Risk] `logEntry` 在事务外调用 `entryLogRepository.save()` 可能因事务回滚导致日志与实际状态不一致
  - → [Mitigation] `logEntry` 在 `verify()` 的 finally 块前调用，此时票状态已更新，即使日志保存失败也不影响验票结果的返回