## Why

后端服务启动时 Hibernate schema validation 失败，因为数据库表结构与 JPA Entity 定义的枚举类型不匹配。当前数据库使用 `varchar` 存储枚举值，但 Hibernate 6.x 期望 `enum` 类型，导致 `Schema-validation: wrong column type` 错误。

## What Changes

- 修改 `visitors` 表 `status` 列从 `varchar(20)` 改为 `ENUM('ACTIVE','SUSPENDED','CANCELLED')`
- 修改 `window_sales` 表 `ticket_type` 列从 `varchar(20)` 改为 `ENUM('YEAR_PASS','MONTH_PASS','SINGLE_USE','WALK_IN','OTA_TICKET')`
- 创建 Flyway 迁移脚本确保环境一致
- 更新开发文档记录数据库约束

## Capabilities

### New Capabilities
- `database-enum-migration`: 数据库枚举类型规范化迁移

### Modified Capabilities
<!-- 无现有 spec 变更 -->

## Impact

- **数据库**: `visitors.status`, `window_sales.ticket_type` 列类型变更
- **配置**: `spring.jpa.hibernate.ddl-auto=validate` 要求 schema 必须与 Entity 完全匹配
- **文档**: 更新 README 说明数据库类型约束