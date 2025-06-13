package com.example.Payment.service;

import com.example.Payment.client.CashfreeClient;
import com.example.Payment.config.CashfreeConfig;
import com.example.Payment.dto.CreateOrderRequest;
import com.example.Payment.dto.CreateOrderResponse;
import com.example.Payment.dto.CustomerResponse;
import com.example.Payment.dto.SaveCustomerRequest;
import com.example.Payment.entity.OrderEntity;
import com.example.Payment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashfreeService {

    @Autowired
    CashfreeClient cashfreeClient;

    @Autowired
    CashfreeConfig config;

    @Autowired
    OrderRepository orderRepository;

    public void updatePendingPayments() {
        List<OrderEntity> pendingOrders = orderRepository.findByStatus("PENDING");

        for (OrderEntity order : pendingOrders) {
            updatePaymentStatus(order.getOrderId());
        }
    }


    public void saveCustomer(SaveCustomerRequest request) {
        String orderId = "ORDER_" + UUID.randomUUID();

        OrderEntity order = OrderEntity.builder()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .orderAmount(request.getOrderAmount())
                .currency("INR")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        log.info("{}", Map.of("message", "Saved user with orderId:" +orderId + " to DB", "level", "INFO"));
    }

    public CustomerResponse getCustomer(String customerId) {
        OrderEntity order = orderRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        log.info("{}", Map.of("message", "Found customer with :" +customerId, "level", "INFO"));

        return new CustomerResponse(
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getOrderAmount()
        );
    }

//    public Object createOrder(CreateOrderRequest request) {
//
//        String orderId = "ORDER_" + UUID.randomUUID();
//
//        OrderEntity customerRecord = orderRepository.findByCustomerId(request.getCustomerId())
//                .orElseThrow(() -> new RuntimeException("Customer not found for ID: " + request.getCustomerId()));
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("order_id", orderId);
//        body.put("order_amount", customerRecord.getOrderAmount());
//        body.put("order_currency", "INR");
//
//        Map<String, Object> customerDetails = new HashMap<>();
//        customerDetails.put("customer_id", customerRecord.getCustomerId());
//        customerDetails.put("customer_name", customerRecord.getCustomerName());
//        customerDetails.put("customer_email", customerRecord.getCustomerEmail());
//        customerDetails.put("customer_phone", customerRecord.getCustomerPhone());
//        body.put("customer_details", customerDetails);
//
//        CreateOrderResponse response = cashfreeClient.createOrder(
//                body,
//                config.getClientId(),
//                config.getClientSecret(),
//                "2023-08-01"
//        );
//
//
//        if (response != null && response.getSessionId() != null) {
//            // Try to find the existing order with same customerId and sessionId null
//            Optional<OrderEntity> existingOrderOpt = orderRepository.findTopByCustomerIdAndSessionIdIsNullOrderByCreatedAtDesc(request.getCustomerId());
//
//            if (existingOrderOpt.isPresent()) {
//                OrderEntity existingOrder = existingOrderOpt.get();
//                existingOrder.setSessionId(response.getSessionId());
//                existingOrder.setCfPaymentId(response.getCfPaymentId());
//                existingOrder.setStatus("PENDING");
//                orderRepository.save(existingOrder);
//            } else {
//                // fallback: create a new order record
//                OrderEntity newOrder = OrderEntity.builder()
//                        .orderId(orderId)
//                        .orderAmount(customerRecord.getOrderAmount())
//                        .currency("INR")
//                        .customerId(customerRecord.getCustomerId())
//                        .customerName(customerRecord.getCustomerName())
//                        .customerEmail(customerRecord.getCustomerEmail())
//                        .customerPhone(customerRecord.getCustomerPhone())
//                        .sessionId(response.getSessionId())
//                        .status("PENDING")
//                        .createdAt(LocalDateTime.now())
//                        .cfPaymentId(response.getCfPaymentId())
//                        .build();
//
//                orderRepository.save(newOrder);
//            }
//        } else {
//            throw new RuntimeException("Failed to create Cashfree order: sessionId is null");
//        }
//
//
//        return response;
//    }

    public Object createOrder(CreateOrderRequest request) {

        log.info("{}", Map.of("message", "Fetching customer record with customerId"+request.getCustomerId(), "level", "INFO"));
        OrderEntity customerRecord = orderRepository.findTopByCustomerIdAndSessionIdIsNullOrderByCreatedAtDesc(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found or order already processed for ID: " + request.getCustomerId()));

//        OrderEntity customerRecord = orderRepository.findByCustomerId(request.getCustomerId())
//                .orElseThrow(() -> new RuntimeException("Customer not found or order already processed for ID: " + request.getCustomerId()));

        String orderId = customerRecord.getOrderId();

        Map<String, Object> body = new HashMap<>();
        body.put("order_id", orderId);
        body.put("order_amount", customerRecord.getOrderAmount());
        body.put("order_currency", "INR");

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", customerRecord.getCustomerId());
        customerDetails.put("customer_name", customerRecord.getCustomerName());
        customerDetails.put("customer_email", customerRecord.getCustomerEmail());
        customerDetails.put("customer_phone", customerRecord.getCustomerPhone());
        body.put("customer_details", customerDetails);

        CreateOrderResponse response = cashfreeClient.createOrder(
                body,
                config.getClientId(),
                config.getClientSecret(),
                "2023-08-01"
        );

        if (response != null && response.getSessionId() != null) {
            log.info("{}", Map.of("message", "Setting sessionId :"+response.getSessionId()+"to record with customerId"+request.getCustomerId(), "level", "INFO"));
            customerRecord.setSessionId(response.getSessionId());
//            customerRecord.setCfPaymentId(response.getCfPaymentId());
            log.error("{}", Map.of("message", "No sessionId found for  customerId", "level", "ERROR"));
            customerRecord.setStatus("PENDING");
            orderRepository.save(customerRecord);
        } else {
            log.warn("{}", Map.of("message", "No sessionId found for  customerId"+request.getCustomerId(), "level", "WARN"));
            throw new RuntimeException("Failed to create Cashfree order: sessionId is null");
        }

        return response;
    }


//    public void updatePaymentStatus(String orderId) {
//        log.info("inside updatePaymentStatus function");
//        OrderEntity order = orderRepository.findByOrderId(orderId);
//        if (order == null) {
//            System.err.println("Order not found with orderId: " + orderId);
//            return;
//        }
//
//        Map<String, Object> transactionDetails;
//
////        if (order.getCfPaymentId() != null) {
////            transactionDetails = cashfreeClient.getTransactionDetails(
////                    order.getCfPaymentId(),
////                    config.getClientId(),
////                    config.getClientSecret(),
////                    "2023-08-01"
////            );
////        } else {
//            transactionDetails = cashfreeClient.getPaymentsByOrderId(
//                    order.getOrderId(),
//                    config.getClientId(),
//                    config.getClientSecret(),
//                    "2023-08-01"
//            ).getLast()==null?null:cashfreeClient.getPaymentsByOrderId(
//                    order.getOrderId(),
//                    config.getClientId(),
//                    config.getClientSecret(),
//                    "2023-08-01"
//            ).getLast();
//
//            if (transactionDetails != null && transactionDetails.get("cf_payment_id") != null) {
//                order.setCfPaymentId((String) transactionDetails.get("cf_payment_id"));
//            }
////        }
//
//        if (transactionDetails == null) {
//            System.err.println("No transaction details received for orderId: " + orderId);
//            return;
//        }
//
//        String paymentStatus = (String) transactionDetails.get("payment_status");
//        if (paymentStatus != null) {
//            orderRepository.save(order);
//            System.out.println("Updated order for orderId " + orderId + ": status=" + paymentStatus + ", cf_payment_id=" + order.getCfPaymentId());
//        } else {
//            System.err.println("Payment status not found for orderId: " + orderId);
//        }
//    }

public void updatePaymentStatus(String orderId) {

    log.info("{}", Map.of("message", "updating PaymentStatus for orderId:"+orderId, "level", "INFO"));

    OrderEntity order = orderRepository.findByOrderId(orderId);
    if (order == null) {
        log.warn("{}", Map.of("message", "Order not found with orderId:"+orderId, "level", "WARN"));
        return;
    }

    List<Map<String, Object>> payments = cashfreeClient.getPaymentsByOrderId(
            order.getOrderId(),
            config.getClientId(),
            config.getClientSecret(),
            "2023-08-01"
    );

    if (payments == null || payments.isEmpty()) {
        log.info("{}", Map.of("message", "No payments found for orderId:"+orderId, "level", "INFO"));
        return;
    }

    Map<String, Object> transactionDetails = payments.get(payments.size() - 1); // Safely get the last

    if (transactionDetails != null && transactionDetails.get("cf_payment_id") != null) {
        order.setCfPaymentId((String) transactionDetails.get("cf_payment_id"));
    }

    String paymentStatus = (String) transactionDetails.get("payment_status");

    if (paymentStatus != null) {
        order.setStatus(paymentStatus);
        log.info("", orderId, paymentStatus, order.getCfPaymentId());
        log.info("{}", Map.of("message", "Updating order with status:"+ paymentStatus +"and cf_payment_id:"+ order.getCfPaymentId(), "level", "INFO"));
        orderRepository.save(order);
    } else {
        log.warn("Payment status not found for orderId: {}", orderId);
    }
}

}



