package com.scenic.ticket.model;

/**
 * OTA渠道枚举
 */
public enum OtaChannel {
    MEITUAN("美团"),
    DOUYIN("抖音"),
    CTRIP("携程"),
    WINDOW("窗口"),
    MINIAPP("小程序");

    private final String description;

    OtaChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}