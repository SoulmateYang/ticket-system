package com.scenic.ticket.dto;

import com.scenic.ticket.model.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 年票/月票创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {

    @NotNull(message = "票种类型不能为空")
    private TicketType type;

    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    @NotBlank(message = "身份证号不能为空")
    private String visitorId;

    private String phone;

    @NotNull(message = "有效期起始不能为空")
    private LocalDateTime validFrom;

    @NotNull(message = "有效期截止不能为空")
    private LocalDateTime validTo;

    private Integer maxEntries;

    private String maxEntriesExceededAction;
}