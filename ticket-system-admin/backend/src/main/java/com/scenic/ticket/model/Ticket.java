package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 票务实体
 * 支持年票/月票/次票/OTA票四种类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_code", columnList = "ticketCode", unique = true),
    @Index(name = "idx_visitor_id", columnList = "visitorId"),
    @Index(name = "idx_status", columnList = "status")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 票种类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType type;

    /**
     * 票号（QR码内容，UUID）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String ticketCode;

    /**
     * 访客ID（身份证号，年票/月票必需，次票/OTA票可空）
     */
    @Column(length = 18)
    private String visitorId;

    /**
     * 访客姓名
     */
    @Column(length = 50)
    private String visitorName;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String phone;

    /**
     * 有效期起始
     */
    @Column
    private LocalDateTime validFrom;

    /**
     * 有效期截止
     */
    @Column
    private LocalDateTime validTo;

    /**
     * 最大入园次数（年票/月票）
     */
    @Column
    private Integer maxEntries;

    /**
     * 已入园次数
     */
    @Column
    private Integer usedEntries;

    /**
     * 超次处理策略
     */
    @Column(length = 10)
    @Builder.Default
    private String maxEntriesExceededAction = "REJECT";

    /**
     * 票状态：AVAILABLE/USED/REFUNDED/EXPIRED/CANCELLED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "AVAILABLE";

    /**
     * 渠道来源
     */
    @Column(length = 20)
    @Builder.Default
    private String channel = "WINDOW";

    /**
     * 入园时间
     */
    @Column
    private LocalDateTime usedAt;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column
    private LocalDateTime updatedAt;

    /**
     * 人脸特征模板（Phase 2实现，schema预留字段）
     */
    @Column(columnDefinition = "TEXT")
    private String faceTemplate;

    /**
     * 关联窗口售出记录（窗口售票必填，其他票种可空）
     */
    @Column
    private Long windowSaleId;

    /**
     * 关联OTA订单（OTA票必填，其他票种可空）
     */
    @Column
    private Long otaOrderId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usedEntries == null) {
            usedEntries = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}