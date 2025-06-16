package com.example.Payment.service;

import com.example.Payment.client.CashfreeClient;
import com.example.Payment.config.CashfreeConfig;
import com.example.Payment.dto.CreateOrderRequest;
import com.example.Payment.dto.CreateOrderResponse;
import com.example.Payment.dto.CustomerResponse;
import com.example.Payment.dto.SaveCustomerRequest;
import com.example.Payment.entity.OrderEntity;
import com.example.Payment.entity.OrderPaymentEntity;
import com.example.Payment.repository.OrderPaymentRepository;
import com.example.Payment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    @Autowired
    OrderPaymentRepository orderPaymentRepository;

    public void updatePendingPayments() {
        List<OrderEntity> pendingOrders = orderRepository.findByStatus("PENDING");

        for (OrderEntity order : pendingOrders) {
            updatePaymentStatus(order.getOrderId());
        }
    }

    public void saveCustomer(SaveCustomerRequest request) {
//        String orderId = "ORDER_" + UUID.randomUUID();

        Optional<OrderEntity> orderStatus= orderRepository.findByCustomerId(request.getCustomerId());
//
        if(orderStatus.isEmpty())
        {
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
    else {
          log.info("{}", Map.of("message", "user already existed with customerId:" +request.getCustomerId(), "level", "INFO"));
    }
    }

    public Object createOrder(CreateOrderRequest request) {

        log.info("{}", Map.of("message", "Fetching customer record with customerId"+request.getCustomerId(), "level", "INFO"));
        OrderEntity customerRecord = orderRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found or order already processed for ID: " + request.getCustomerId()));

        if(customerRecord.getSessionId()!=null)
        {
            return new CreateOrderResponse(customerRecord.getSessionId(),customerRecord.getOrderId());
        }

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
            log.info("{}", Map.of("message", "Setting sessionId :"+response.getSessionId()+"to record with customerId:"+request.getCustomerId(), "level", "INFO"));
            customerRecord.setSessionId(response.getSessionId());
            log.error("{}", Map.of("message", "No sessionId found for  customerId", "level", "ERROR"));
            customerRecord.setStatus("PENDING");
            orderRepository.save(customerRecord);
        } else {
            log.warn("{}", Map.of("message", "No sessionId found for  customerId"+request.getCustomerId(), "level", "WARN"));
            throw new RuntimeException("Failed to create Cashfree order: sessionId is null");
        }

        return response;
    }

//public void updatePaymentStatus(String orderId) {
//
//    log.info("{}", Map.of("message", "updating PaymentStatus for orderId:"+orderId, "level", "INFO"));
//
//    OrderEntity order = orderRepository.findByOrderId(orderId);
//    if (order == null) {
//        log.warn("{}", Map.of("message", "Order not found with orderId:"+orderId, "level", "WARN"));
//        return;
//    }
//
//    List<Map<String, Object>> payments = cashfreeClient.getPaymentsByOrderId(
//            order.getOrderId(),
//            config.getClientId(),
//            config.getClientSecret(),
//            "2023-08-01"
//    );
//
//    if (payments == null || payments.isEmpty()) {
//        log.info("{}", Map.of("message", "No payments found for orderId:"+orderId, "level", "INFO"));
//        return;
//    }
//
//    Map<String, Object> transactionDetails = payments.get(payments.size() - 1); // Safely get the last
//
//    if (transactionDetails != null && transactionDetails.get("cf_payment_id") != null) {
//        order.setCfPaymentId((String) transactionDetails.get("cf_payment_id"));
//    }
//
//    String paymentStatus = (String) transactionDetails.get("payment_status");
//
//    if (paymentStatus != null) {
//        order.setStatus(paymentStatus);
//        log.info("{}", Map.of("message", "Updating order with status:"+ paymentStatus +"and cf_payment_id:"+ order.getCfPaymentId(), "level", "INFO"));
//        orderRepository.save(order);
//    } else {
//        log.warn("Payment status not found for orderId: {}", orderId);
//    }
//}

    // ---------------------- updated

//    public void updatePaymentStatus(String orderId) {
//        log.info("{}", Map.of("message", "Updating PaymentStatus for orderId: " + orderId, "level", "INFO"));
//
//        OrderEntity order = orderRepository.findByOrderId(orderId);
//        if (order == null) {
//            log.warn("{}", Map.of("message", "Order not found with orderId: " + orderId, "level", "WARN"));
//            return;
//        }
//
//        List<Map<String, Object>> payments = cashfreeClient.getPaymentsByOrderId(
//                order.getOrderId(),
//                config.getClientId(),
//                config.getClientSecret(),
//                "2023-08-01"
//        );
//
//        if (payments == null || payments.isEmpty()) {
//            log.info("{}", Map.of("message", "No payments found for orderId: " + orderId, "level", "INFO"));
//            return;
//        }
//
//        List<OrderPaymentEntity> paymentEntities = payments.stream()
//                .map(p -> mapToPaymentEntity(orderId, p))
//                .toList();
//        orderPaymentRepository.saveAll(paymentEntities);
//
//        for (Map<String, Object> payment : payments) {
//            String status = (String) payment.get("payment_status");
//            if ("SUCCESS".equalsIgnoreCase(status)) {
//                order.setStatus("SUCCESS");
//                orderRepository.save(order);
//                log.info("Order {} updated with SUCCESS status", orderId);
//                return;
//            }
//        }
//
//        payments.sort((a, b) -> {
//            String timeA = (String) a.get("payment_completion_time");
//            String timeB = (String) b.get("payment_completion_time");
//            return Instant.parse(timeB).compareTo(Instant.parse(timeA)); // Descending
//        });
//
//        Map<String, Object> latest = payments.get(0);
//        String latestStatus = (String) latest.get("payment_status");
//        order.setStatus(latestStatus);
//        orderRepository.save(order);
//        log.info("Order {} updated with latest status: {}", orderId, latestStatus);
//    }
//
//    private OrderPaymentEntity mapToPaymentEntity(String orderId, Map<String, Object> payment) {
//        OrderEntity order = orderRepository.findByOrderId(orderId);
//
//        return OrderPaymentEntity.builder()
//                .order(order)
//                .paymentId((String) payment.get("cf_payment_id"))
//                .paymentStatus((String) payment.get("payment_status"))
//                .paymentMethod((String) payment.get("payment_method"))
//                .bankReference((String) payment.get("bank_reference"))
//                .paymentTime((String) payment.get("payment_completion_time"))
//                .build();
//    }



    // ---------------------- updated

//    public void updatePaymentStatus(String orderId) {
//        log.info("{}", Map.of("message", "Updating PaymentStatus for orderId: " + orderId, "level", "INFO"));
//
//        OrderEntity order = orderRepository.findByOrderId(orderId);
//        if (order == null) {
//            log.warn("{}", Map.of("message", "Order not found with orderId: " + orderId, "level", "WARN"));
//            return;
//        }
//
//        List<Map<String, Object>> payments = cashfreeClient.getPaymentsByOrderId(
//                order.getOrderId(),
//                config.getClientId(),
//                config.getClientSecret(),
//                "2023-08-01"
//        );
//
//        if (payments == null || payments.isEmpty()) {
//            log.info("{}", Map.of("message", "No payments found for orderId: " + orderId, "level", "INFO"));
//            return;
//        }
//
//        List<OrderPaymentEntity> paymentEntities = payments.stream()
//                .map(p -> mapToPaymentEntity(order, p))
//                .filter(Objects::nonNull)
//                .toList();
//
//        orderPaymentRepository.saveAll(paymentEntities);
//
//        for (Map<String, Object> payment : payments) {
//            Object statusObj = payment.get("payment_status");
//            if (statusObj instanceof String status && "SUCCESS".equalsIgnoreCase(status)) {
//                order.setStatus("SUCCESS");
//                orderRepository.save(order);
//                log.info("Order {} updated with SUCCESS status", orderId);
//                return;
//            }
//        }
//
//
//
//        payments.sort((a, b) -> {
//            try {
//                String timeA = (String) a.get("payment_completion_time");
//                String timeB = (String) b.get("payment_completion_time");
//                return Instant.parse(timeB).compareTo(Instant.parse(timeA));
//            } catch (Exception e) {
//                return 0;
//            }
//        });
//
//        Map<String, Object> latest = payments.get(0);
//        Object latestStatusObj = latest.get("payment_status");
//        if (latestStatusObj instanceof String latestStatus) {
//            order.setStatus(latestStatus);
//            orderRepository.save(order);
//            log.info("Order {} updated with latest status: {}", orderId, latestStatus);
//        } else {
//            log.warn("Invalid payment_status format in latest payment object");
//        }
//    }

    // --------- updated

    public void updatePaymentStatus(String orderId) {
        log.info("{}", Map.of("level", "INFO",
                "message", "Updating PaymentStatus for orderId: " + orderId));

        /* ─────────────────── 1.  Fetch the order ─────────────────── */
        OrderEntity order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            log.warn("{}", Map.of("level", "WARN",
                    "message", "Order not found with orderId: " + orderId));
            return;
        }

        /* ─────────────────── 2.  Pull payments from Cashfree ──────── */
        List<Map<String, Object>> payments = cashfreeClient.getPaymentsByOrderId(
                orderId,
                config.getClientId(),
                config.getClientSecret(),
                "2023-08-01");

        if (payments == null || payments.isEmpty()) {
            log.info("{}", Map.of("level", "INFO",
                    "message", "No payments found for orderId: " + orderId));
            return;
        }

        /* ─────────────────── 3.  Persist each *new* payment row ───── */
        payments.stream()
                .map(p -> mapToPaymentEntity(order, p))      // build entity
                .filter(Objects::nonNull)                    // skip malformed
                .filter(pe -> {                              // skip duplicates
                    boolean exists = orderPaymentRepository
                            .existsByPaymentIdAndPaymentStatus(
                                    pe.getPaymentId(), pe.getPaymentStatus());
                    if (exists) {
                        log.debug("{}", Map.of("level", "DEBUG",
                                "message",
                                "Duplicate ignored (paymentId=" +
                                        pe.getPaymentId() + ", status=" +
                                        pe.getPaymentStatus() + ")"));
                    }
                    return !exists;
                })
                .forEach(orderPaymentRepository::save);

        /* ─────────────────── 4.  If any SUCCESS row, mark order SUCCESS ── */
        if (payments.stream()
                .anyMatch(p -> "SUCCESS".equalsIgnoreCase(
                        getAsString(p.get("payment_status"))))) {
            order.setStatus("SUCCESS");
            orderRepository.save(order);
            log.info("Order {} updated with SUCCESS status", orderId);
            return;                          // DONE – no need to search latest
        }

        /* ─────────────────── 5.  Otherwise pick the *latest* status ────── */
        payments.sort((a, b) -> {
            try {
                return Instant.parse(getAsString(b.get("payment_completion_time")))
                        .compareTo(
                                Instant.parse(getAsString(a.get("payment_completion_time"))));
            } catch (Exception ex) {               // null / bad timestamp
                return 0;
            }
        });

        String latestStatus = getAsString(payments.get(0).get("payment_status"));
        order.setStatus(latestStatus);
        orderRepository.save(order);
        log.info("Order {} updated with latest status: {}", orderId, latestStatus);
    }


    private OrderPaymentEntity mapToPaymentEntity(OrderEntity order, Map<String, Object> payment) {
        try {
            String paymentId = getAsString(payment.get("cf_payment_id"));
            String status = getAsString(payment.get("payment_status"));
            String method = getAsString(payment.get("payment_method"));
            String bankRef = getAsString(payment.get("bank_reference"));
            String paymentTime = getAsString(payment.get("payment_completion_time"));

            if (paymentId == null || status == null || "NOT_ATTEMPTED".equalsIgnoreCase(status)) {
                return null;
            }

            return OrderPaymentEntity.builder()
                    .order(order)
                    .paymentId(paymentId)
                    .paymentStatus(status)
                    .paymentMethod(method)
                    .bankReference(bankRef)
                    .paymentTime(paymentTime)
                    .build();

        } catch (Exception e) {
            log.error("Error mapping payment entity for order {}: {}", order.getOrderId(), e.getMessage());
            return null;
        }
    }

    private String getAsString(Object obj) {
        if (obj instanceof String str) return str;
        if (obj instanceof Map<?, ?>) return obj.toString(); // Avoid ClassCastException
        return obj != null ? obj.toString() : null;
    }



}



