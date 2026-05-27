package com.scenic.ticket.dto;

import com.scenic.ticket.model.OtaChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * OTA订单同步请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtaOrderSyncRequest {

    @NotNull(message = "OTA渠道不能为空")
    private OtaChannel channel;

    @NotBlank(message = "外部订单号不能为空")
    private String externalOrderId;

    private String performanceName;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private Double amount;

    private String buyerName;

    private String buyerPhone;

    private String buyerIdCard;
}