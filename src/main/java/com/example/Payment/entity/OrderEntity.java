package com.example.Payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "order_amount", nullable = false)
    private Double orderAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "customer_name")
    private String customerName;

    private String status;

    private LocalDateTime createdAt;


}
