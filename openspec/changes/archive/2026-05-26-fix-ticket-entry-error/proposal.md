## Why

票务系统"票据入录"功能报错，无法正常创建入场记录。当前 `EntryLogRepository` 继承了标准 JPA Repository，但调用 `findById` 时可能存在 ID 类型不匹配或 Optional 未正确处理的问题，导致编译错误或运行时异常。

## What Changes

1. 修复 `EntryLogRepository` 的 ID 类型声明，确保与 `EntryLog` 实体的 `@Id` 字段类型（`Long`）一致
2. 修复 `TicketService.logEntry()` 方法中 `entryLogRepository.findById()` 返回值（`Optional`）未正确处理的 bug
3. 修复 `TicketService` 中 `logEntry()` 调用缺少 `ticketId` 参数的问题——当前实现传入了 `ticketCode`，但实际 `EntryLog.ticketId` 需要的是 `Long` 类型的票 ID

## Capabilities

### New Capabilities
- `ticket-entry-log`: 入场记录管理能力，含入场日志创建与查询