package com.scenic.ticket.dto;

import com.scenic.ticket.model.TicketType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 票务响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private TicketType type;
    private String ticketCode;
    private String visitorId;
    private String visitorName;
    private String phone;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer maxEntries;
    private Integer usedEntries;
    private String maxEntriesExceededAction;
    private String status;
    private String channel;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}