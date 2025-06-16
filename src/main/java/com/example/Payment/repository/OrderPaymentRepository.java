package com.example.Payment.repository;

import com.example.Payment.entity.OrderPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentRepository extends JpaRepository<OrderPaymentEntity,Long> {

    boolean existsByPaymentIdAndPaymentStatus(String paymentId, String paymentStatus);
}
