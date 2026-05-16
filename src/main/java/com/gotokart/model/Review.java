package com.gotokart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Product product;

    /** 1 to 5 stars. */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    /** PENDING (default), APPROVED, REJECTED. Storefront only renders APPROVED. */
    @Column(nullable = false, length = 16)
    private String status;

    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "PENDING";
    }
}
