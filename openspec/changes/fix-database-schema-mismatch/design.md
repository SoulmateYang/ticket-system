## Context

Spring Boot 3.2 + Hibernate 6.3 项目使用 `spring.jpa.hibernate.ddl-auto=validate` 模式运行。

**当前问题：**
- `visitors.status` 数据库定义为 `varchar(20)`，Entity 使用 `@Enumerated(EnumType.STRING)` 映射到 `ENUM('active','suspended','cancelled')`（小写）
- `window_sales.ticket_type` 数据库定义为 `varchar(20)`，Entity 使用 `@Enumerated(EnumType.STRING)` 映射到 `ENUM('year_pass','month_pass','single_use','walk_in','ota_ticket')`（小写）
- Hibernate 6.x MySQL 方言将 `EnumType.STRING` 映射为 MySQL ENUM 类型，但期望的是小写枚举值

**约束：**
- 生产环境使用 Flyway 管理 schema
- 开发环境需与生产保持一致
- 已有数据需要兼容

## Goals / Non-Goals

**Goals:**
- 修复 Hibernate schema validation 失败问题
- 保持代码与数据库 enum 值一致（大写）
- 生成可重复执行的 Flyway 迁移脚本

**Non-Goals:**
- 不修改已有数据的 enum 值格式
- 不重构其他表结构

## Decisions

**1. 直接 ALTER TABLE 修改列类型**

```sql
ALTER TABLE visitors MODIFY status ENUM('ACTIVE','SUSPENDED','CANCELLED') NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE window_sales MODIFY ticket_type ENUM('YEAR_PASS','MONTH_PASS','SINGLE_USE','WALK_IN','OTA_TICKET') NOT NULL;
```

**替代方案考虑：**
- 方案 B: 修改 Entity 使用 `ORDINAL` 映射 → 数值顺序敏感，易出错
- 方案 C: 临时设置 `ddl-auto=none` → 生产环境风险高

**选择理由：** 最直接修复，不改变现有数据逻辑

**2. 创建 Flyway 迁移脚本 V005**

确保其他环境（测试、生产）可复现修复

## Risks / Trade-offs

[风险] 枚举值大小写不一致
→ **缓解**：JPA Entity 和 MySQL ENUM 都使用大写值，保持一致

[风险] 生产环境执行 ALTER TABLE 锁表
→ **缓解**：MySQL 8.0 的 ALTER TABLE 支持 INPLACE 算法，大表不影响性能

## Open Questions

- 是否有其他表存在类似 enum/varchar 不匹配问题？→ 已检查，暂未发现