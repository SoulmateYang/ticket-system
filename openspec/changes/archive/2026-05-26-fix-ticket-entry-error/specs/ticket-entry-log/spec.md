## ADDED Requirements

### Requirement: 入场日志记录完整性
验票成功时系统 SHALL 记录完整的入场日志，包含正确的 ticketId（Long 类型）、ticketCode、visitorId、entryType、channel、entryTime、result、remark、verifiedBy、deviceId。

#### Scenario: 验票成功记录入场日志
- **WHEN** 用户凭有效票通过验票
- **THEN** 系统在 entry_logs 表中创建一条记录，ticketId 为票的 Long 类型主键，entryTime 为入园时间戳，result 为 "SUCCESS"

#### Scenario: 验票拒绝记录入场日志
- **WHEN** 用户凭无效票或超次被拒绝验票
- **THEN** 系统在 entry_logs 表中创建一条记录，result 为 "REJECT"，remark 包含拒绝原因

### Requirement: 票不存在时返回明确错误
系统 SHALL 在票号不存在时返回明确的错误码和消息，而非抛出未处理异常。

#### Scenario: 票号不存在时返回 NOT_FOUND
- **WHEN** 验票时传入的 ticketCode 在数据库中不存在
- **THEN** 系统返回 {success: false, code: "TICKET_NOT_FOUND", message: "票不存在"}，HTTP 状态码 404

#### Scenario: 票已过期时返回 EXPIRED
- **WHEN** 验票时票的有效期已过
- **THEN** 系统返回 {success: false, code: "TICKET_EXPIRED", message: "票已过期"}

### Requirement: Optional 安全处理
verify 方法 SHALL 安全处理 Optional 返回值，在票不存在时抛出明确异常而非调用 .get() 导致 NPE。

#### Scenario: findByTicketCode 返回 Optional.empty 时正确处理
- **WHEN** 数据库查询 findByTicketCode 返回空 Optional
- **THEN** 系统抛出 TicketNotFoundException，最终返回 TICKET_NOT_FOUND 错误码