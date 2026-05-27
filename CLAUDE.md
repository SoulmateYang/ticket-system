# Scenic Ticket Platform — 景区票务中台系统

## 项目概述

景区票务中台系统，支持年票/月票管理、次票QR码核销、OTA分销对接。

## 技术栈

- **后端**: Spring Boot 3.2 + Java 17
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: Spring Data JPA

## 分层架构

```
controller/    # REST API控制器
service/       # 业务逻辑
repository/    # 数据访问层
model/         # 实体类
dto/           # 数据传输对象
exception/     # 异常处理
```

## 核心功能

### Phase 1
1. **年票/月票管理** — 创建、激活、暂停、取消、有效期校验
2. **次票QR码核销** — Redis分布式锁防重复验票
3. **入场记录** — 记录每次入园的时间戳、渠道

### Phase 2
4. OTA订单自动同步
5. 人脸识别验票（需硬件供应商）

## 运行

```bash
# 安装依赖
mvn clean install

# 启动
mvn spring-boot:run

# 测试
mvn test
```

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/tickets/passes | 创建年票/月票 |
| POST | /api/tickets/passes/{id}/activate | 激活 |
| POST | /api/tickets/passes/{id}/suspend | 暂停 |
| POST | /api/tickets/passes/{id}/cancel | 取消 |
| GET | /api/tickets/passes/visitor/{visitorId} | 查询有效票 |
| POST | /api/tickets/verify | 验票核销 |

## 配置

数据库和Redis配置在 `src/main/resources/application.yml`。