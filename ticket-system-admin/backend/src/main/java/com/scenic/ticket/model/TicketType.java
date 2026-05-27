package com.scenic.ticket.model;

/**
 * 票种类型枚举
 * YEAR_PASS: 年票
 * MONTH_PASS: 月票
 * SINGLE_USE: 次票（单次入场）
 * WALK_IN: 窗口票（窗口即时售票）
 * OTA_TICKET: OTA分销票
 */
public enum TicketType {
    YEAR_PASS("年票"),
    MONTH_PASS("月票"),
    SINGLE_USE("次票"),
    WALK_IN("窗口票"),    // 新增：窗口即时售票
    OTA_TICKET("OTA票");

    private final String description;

    TicketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}