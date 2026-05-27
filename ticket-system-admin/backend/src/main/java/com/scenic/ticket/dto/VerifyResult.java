package com.scenic.ticket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 验票结果响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResult {
    private boolean success;
    private String code;
    private String message;
    private String ticketCode;
    private String ticketType;
    private String visitorName;
    private String entryType;
};