package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 年票/月票持卡人档案
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visitors", indexes = {
    @Index(name = "idx_id_card", columnList = "idCard", unique = true),
    @Index(name = "idx_phone", columnList = "phone")
})
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 身份证号（唯一） */
    @Column(nullable = false, unique = true, length = 18)
    private String idCard;

    /** 状态：ACTIVE / SUSPENDED / CANCELLED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VisitorStatus status = VisitorStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}