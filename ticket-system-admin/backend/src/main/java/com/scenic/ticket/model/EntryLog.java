package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 入场记录实体
 * 记录每次入园的时间戳、渠道、核销人员
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "entry_logs", indexes = {
    @Index(name = "idx_ticket_id", columnList = "ticketId"),
    @Index(name = "idx_entry_time", columnList = "entryTime"),
    @Index(name = "idx_channel", columnList = "channel")
})
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 票ID
     */
    @Column(nullable = false)
    private Long ticketId;

    /**
     * 票号（冗余存储，便于查询）
     */
    @Column(nullable = false, length = 64)
    private String ticketCode;

    /**
     * 访客ID
     */
    @Column(length = 18)
    private String visitorId;

    /**
     * 入园时间
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /**
     * 入园类型
     */
    @Column(length = 20)
    private String entryType;

    /**
     * 渠道来源
     */
    @Column(length = 20)
    private String channel;

    /**
     * 核销员工ID
     */
    @Column(length = 20)
    private String verifiedBy;

    /**
     * 核销设备ID
     */
    @Column(length = 50)
    private String deviceId;

    /**
     * 核销结果
     */
    @Column(length = 20)
    private String result;

    /**
     * 备注
     */
    @Column(length = 200)
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (entryTime == null) {
            entryTime = LocalDateTime.now();
        }
    }
}