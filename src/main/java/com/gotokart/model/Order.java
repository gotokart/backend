package com.gotokart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer"})
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("order")
    private List<OrderItem> items;

    /** Sum of (price × qty) across all items, before any discount is applied. */
    private Double subtotal;

    /** Rupee amount taken off by the redeemed coupon. Null/0 if no coupon. */
    private Double discountAmount;

    /** The coupon code that was redeemed at checkout. Null if none. Denormalised
     *  so that even if the coupon is later edited or deleted, the historical
     *  record on this order stays accurate. */
    @Column(length = 32)
    private String couponCode;

    /** Final amount actually charged: subtotal − discountAmount. */
    private Double totalAmount;

    private String status;
    private LocalDateTime createdAt;
}