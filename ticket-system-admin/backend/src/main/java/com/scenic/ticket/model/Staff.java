package com.scenic.ticket.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "staff", indexes = {
    @Index(name = "idx_staff_phone", columnList = "phone", unique = true),
    @Index(name = "idx_staff_employee_no", columnList = "employeeNo", unique = true)
})
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 工号
     */
    @Column(nullable = false, unique = true, length = 32)
    private String employeeNo;

    /**
     * 姓名
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 手机号（登录账号）
     */
    @Column(nullable = false, unique = true, length = 16)
    private String phone;

    /**
     * 密码哈希（BCrypt）
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * 角色
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StaffRole role;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

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