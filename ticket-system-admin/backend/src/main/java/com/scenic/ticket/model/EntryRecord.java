package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 入场事实记录
 * 区别于 EntryLog（验票事件），EntryRecord 记录实际入场行为
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "entry_records", indexes = {
    @Index(name = "idx_er_ticket_id", columnList = "ticketId"),
    @Index(name = "idx_er_entry_time", columnList = "entryTime"),
    @Index(name = "idx_er_visitor_id", columnList = "visitorId")
})
public class EntryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联票ID */
    @Column(nullable = false)
    private Long ticketId;

    /** 访客身份证（冗余存储，便于查询） */
    @Column(length = 18)
    private String visitorId;

    /** 入场时间 */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /** 入口/闸机编号 */
    @Column(length = 20)
    private String gate;

    /** 渠道 */
    @Column(length = 20)
    private String channel;

    /** 核销员工ID（冗余存储） */
    @Column(length = 20)
    private String verifiedBy;

    @PrePersist
    protected void onCreate() {
        if (entryTime == null) entryTime = LocalDateTime.now();
    }
}