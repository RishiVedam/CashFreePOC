package com.example.Payment.service;

import com.example.Payment.client.CashfreeClient;
import com.example.Payment.config.CashfreeConfig;
import com.example.Payment.dto.CreateOrderRequest;
import com.example.Payment.dto.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashfreeService {

    private final CashfreeClient cashfreeClient;
    private final CashfreeConfig config;

    public Object createOrder(CreateOrderRequest request) {
        String orderId = "ORDER_" + UUID.randomUUID();

        Map<String, Object> body = new HashMap<>();
        body.put("order_id", orderId);
        body.put("order_amount", request.getOrderAmount());
        body.put("order_currency", "INR");

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", UUID.randomUUID().toString());
        customerDetails.put("customer_name", request.getCustomerName());
        customerDetails.put("customer_email", request.getCustomerEmail());
        customerDetails.put("customer_phone", request.getCustomerPhone());
        body.put("customer_details", customerDetails);

        return cashfreeClient.createOrder(
                body,
                config.getClientId(),
                config.getClientSecret(),
                "2023-08-01"
        );
    }
}


//package com.example.Payment.service;
//
//import com.example.Payment.client.CashfreeClient;
//import com.example.Payment.config.CashfreeConfig;
//import com.example.Payment.dto.CreateOrderRequest;
//import com.example.Payment.dto.CreateOrderResponse;
//import com.example.Payment.entity.OrderEntity;
//import com.example.Payment.repository.OrderRepository;
//import lombok.Builder;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@Builder
//public class CashfreeService {
//
//    @Autowired
//    CashfreeClient cashfreeClient;
//
//    @Autowired
//    CashfreeConfig config;
//
//    @Autowired
//    OrderRepository orderRepository;
//
//    public CreateOrderResponse createOrder(CreateOrderRequest request) {
//        String orderId = "ORDER_" + UUID.randomUUID();
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("order_amount", request.getOrderAmount());
//        requestBody.put("order_currency", "INR");
//        requestBody.put("customer_details", Map.of(
//                "customer_email", request.getCustomerEmail(),
//                "customer_phone", request.getCustomerPhone(),
//                "customer_name", request.getCustomerName()
//        ));
//
//        Map<String, Object> response = cashfreeClient.createOrder(requestBody, config.getClientId(), config.getClientSecret());
//
//        String sessionId = (String) ((Map<?, ?>) response.get("data")).get("payment_session_id");
//
//        OrderEntity order = OrderEntity.builder()
//                .orderAmount(request.getOrderAmount())
//                .currency("INR")
//                .customerName(request.getCustomerName())
//                .customerEmail(request.getCustomerEmail())
//                .customerPhone(request.getCustomerPhone())
//                .sessionId(sessionId)
//                .status("CREATED")
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        orderRepository.save(order);
//
//        return new CreateOrderResponse(sessionId);
//    }
//}

