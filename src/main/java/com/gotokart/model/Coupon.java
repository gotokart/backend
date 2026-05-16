package com.gotokart.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    /** Discount percentage 1-100 (integer). Future work: support flat-amount discounts too. */
    @Column(nullable = false)
    private Integer discountPercent;

    /** When the coupon stops being redeemable. Null = no expiry. */
    private LocalDateTime validUntil;

    /** Maximum number of total redemptions. Null = unlimited. */
    private Integer usageLimit;

    /** Running count of how many times this coupon has been redeemed. */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0 NOT NULL")
    private Integer usedCount = 0;

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1 NOT NULL")
    private Boolean active = Boolean.TRUE;

    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (usedCount == null) usedCount = 0;
        if (active == null) active = Boolean.TRUE;
        if (code != null) code = code.trim().toUpperCase();
    }

    @PreUpdate
    private void onUpdate() {
        if (code != null) code = code.trim().toUpperCase();
    }
}
