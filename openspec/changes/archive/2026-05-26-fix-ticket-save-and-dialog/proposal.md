## Why

次票（QR码票）和年/月票在创建时保存失败，用户无法成功购票或创建票；同时操作结果的错误/成功弹窗显示不明显，用户无法清楚感知操作结果，降低了系统可用性和信任度。

## What Changes

1. **修复次票保存失败** — 排查并修复次票（SingleTicket）创建时的后端保存逻辑问题
2. **修复年票保存失败** — 排查并修复年/月票（Pass）创建时的后端保存逻辑问题
3. **优化弹窗显示** — 使用 Element Plus 的 `ElMessage` 或 `ElNotification` 替代当前不明显的提示方式，确保成功/错误信息清晰可见

## Capabilities

### New Capabilities
- `ticket-creation-fix`: 修复次票和年/月票在后端Service层的保存逻辑，确保JPA事务正确提交

### Modified Capabilities
<!-- 无现有spec需修改，当前为Bug修复 -->

## Impact

- **后端**: `TicketService.createSingleTicket()`、`PassService` 或相关Repository
- **前端**: 票创建页面（`Ticket.vue`、`Pass.vue`）的错误处理和用户反馈机制
- **数据库**: 确认相关表结构是否正确（JPA自动建表）
