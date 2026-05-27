package com.scenic.ticket.model;

/**
 * 员工角色枚举
 */
public enum StaffRole {
    ADMIN,      // 管理员：所有接口
    TICKETER,   // 验票员：验票相关接口
    FINANCE,    // 财务：财务数据只读
    OTA         // OTA运营：OTA订单管理
}