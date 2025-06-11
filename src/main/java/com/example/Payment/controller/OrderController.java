package com.example.Payment.controller;

import com.example.Payment.dto.CreateOrderRequest;
import com.example.Payment.dto.CreateOrderResponse;
import com.example.Payment.service.CashfreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    CashfreeService cashfreeService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(cashfreeService.createOrder(request));
    }
}
