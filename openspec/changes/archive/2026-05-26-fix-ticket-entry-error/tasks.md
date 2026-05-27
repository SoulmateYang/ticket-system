## 1. Bug Fixes

- [x] 1.1 修复 TicketService.verify() 中 findByTicketCode 返回的 Optional 安全处理，使用 orElseThrow 替代 .get()
- [x] 1.2 修复 TicketService.logEntry() 方法签名，增加 ticketId (Long) 参数传递
- [x] 1.3 更新 TicketService.verify() 中所有 logEntry() 调用，传入 ticket.getId()

## 2. Verification

- [ ] 2.1 编译后端代码验证修复有效
- [ ] 2.2 启动后端服务验证验票流程无异常