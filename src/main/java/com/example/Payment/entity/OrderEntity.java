package com.example.Payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

//@Entity
//@Table(name = "orders")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class OrderEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name="customer_id")
//    private String customerId;
//
//    @Column(name = "order_id")
//    private String orderId;
//
//    @Column(name = "order_amount")
//    private Double orderAmount;
//
//    @Column(name = "currency")
//    private String currency;
//
//    @Column(name = "customer_email")
//    private String customerEmail;
//
//    @Column(name = "customer_phone")
//    private String customerPhone;
//
//    @Column(name = "session_id")
//    private String sessionId;
//
//    @Column(name = "customer_name")
//    private String customerName;
//
//    private String status;
//
//    private LocalDateTime createdAt;
//
//    @Column(name = "cf_payment_id")
//    private String cfPaymentId;
//
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//    private List<OrderPaymentEntity> payments;
//
//
//}



@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id                                       // ← becomes PK
    @Column(name = "order_id", length = 64)
    private String orderId;

    private String customerId;
    private Double  orderAmount;
    private String  currency;
    private String  customerEmail;
    private String  customerPhone;
    private String  customerName;
    private String  sessionId;
    private String  status;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderPaymentEntity> payments;
}
