package com.example.Payment.controller;

import com.example.Payment.dto.CreateOrderRequest;
import com.example.Payment.dto.CreateOrderResponse;
import com.example.Payment.dto.CustomerResponse;
import com.example.Payment.dto.SaveCustomerRequest;
import com.example.Payment.entity.OrderEntity;
import com.example.Payment.repository.OrderRepository;
import com.example.Payment.service.CashfreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    @Autowired
    CashfreeService cashfreeService;

    @Autowired
    OrderRepository orderRepository;

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveCustomer(@RequestBody SaveCustomerRequest request) {
        log.info("{}", Map.of("message", "Saving user to DB" , "level", "INFO"));

        cashfreeService.saveCustomer(request);
    return ResponseEntity.ok(Map.of("message", "Customer saved successfully"));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String customerId) {
        log.info("{}", Map.of("message", "Fetching customer having customerId:" +customerId, "level", "INFO"));
        return ResponseEntity.ok(cashfreeService.getCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("{}", Map.of("message", "Creating order", "level", "INFO"));
        return ResponseEntity.ok(cashfreeService.createOrder(request));
    }

    @GetMapping("/{orderId}/verify")
    public ResponseEntity<?> getOrderStatus(@PathVariable String orderId) {
        log.info("goining to find order using orderId");
        OrderEntity order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            log.info("not found order with given orderId");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No order found for this orderId");
        }

        cashfreeService.updatePaymentStatus(order.getOrderId());

        OrderEntity updatedOrder = orderRepository.findByOrderId(order.getOrderId());

        if (updatedOrder.getStatus() != null) {
            return ResponseEntity.ok(Map.of("status", updatedOrder.getStatus()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "Could not retrieve payment status."));
        }
    }

}
