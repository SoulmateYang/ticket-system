package com.scenic.ticket.dto;

import com.scenic.ticket.model.OtaChannel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * OTA订单响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtaOrderResponse {
    private Long id;
    private OtaChannel otaChannel;
    private String externalOrderId;
    private String performanceName;
    private Integer quantity;
    private Double amount;
    private String buyerName;
    private String buyerPhone;
    private String buyerIdCard;
    private String status;
    private String syncStatus;
    private LocalDateTime syncTime;
    private LocalDateTime createdAt;
}