package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 窗口售票记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "window_sales", indexes = {
    @Index(name = "idx_window_id", columnList = "windowId"),
    @Index(name = "idx_sold_at", columnList = "soldAt")
})
public class WindowSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 票种类型（仅 WALK_IN） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    /** 售出数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 售价金额 */
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /** 窗口编号 */
    @Column(length = 20)
    private String windowId;

    /** 售票员工ID（冗余存储） */
    @Column(length = 20)
    private String sellerId;

    /** 售出时间 */
    @Column(nullable = false)
    private LocalDateTime soldAt;

    @PrePersist
    protected void onCreate() {
        if (soldAt == null) soldAt = LocalDateTime.now();
    }
}