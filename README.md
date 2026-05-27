# 景区票务中台系统

景区票务中台系统，支持年票/月票管理、次票QR码核销、OTA分销对接。

## 技术栈

- Spring Boot 3.2 + Java 17
- MySQL 8.0
- Redis

## 快速开始

```bash
mvn clean install
mvn spring-boot:run
```

## 核心功能

- **年票/月票管理**: 创建、激活、暂停、取消、有效期校验
- **次票核销**: QR码扫描 + Redis分布式锁防重复验票
- **入场记录**: 记录每次入园的时间戳、渠道、验票员

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/tickets/passes | 创建年票/月票 |
| POST | /api/tickets/passes/{id}/activate | 激活 |
| POST | /api/tickets/passes/{id}/suspend | 暂停 |
| POST | /api/tickets/passes/{id}/cancel | 取消 |
| GET | /api/tickets/passes/visitor/{visitorId} | 查询有效票 |
| POST | /api/tickets/verify | 验票核销 |