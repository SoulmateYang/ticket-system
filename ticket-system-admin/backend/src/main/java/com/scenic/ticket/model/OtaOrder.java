package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * OTA分销订单实体
 * 存储来自美团/抖音/携程的订单同步数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ota_orders", indexes = {
    @Index(name = "idx_external_order_id", columnList = "externalOrderId"),
    @Index(name = "idx_ota_channel", columnList = "otaChannel"),
    @Index(name = "idx_synced_status", columnList = "syncStatus")
})
public class OtaOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OTA渠道
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtaChannel otaChannel;

    /**
     * 外部订单号（OTA平台生成）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String externalOrderId;

    /**
     * 票种类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    /**
     * 演出/景区名称
     */
    @Column(length = 100)
    private String performanceName;

    /**
     * 数量
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 订单金额
     */
    @Column
    private Double amount;

    /**
     * 买家姓名
     */
    @Column(length = 50)
    private String buyerName;

    /**
     * 买家电话
     */
    @Column(length = 20)
    private String buyerPhone;

    /**
     * 买家身份证
     */
    @Column(length = 18)
    private String buyerIdCard;

    /**
     * 订单状态
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * 同步状态：PENDING/SYNCED/FAILED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String syncStatus = "PENDING";

    /**
     * 同步时间
     */
    @Column
    private LocalDateTime syncTime;

    /**
     * 同步备注
     */
    @Column(length = 200)
    private String syncRemark;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}