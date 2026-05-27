package com.scenic.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 次票创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleTicketCreateRequest {

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotBlank(message = "演出名称不能为空")
    private String performanceName;

    private String channel;
}