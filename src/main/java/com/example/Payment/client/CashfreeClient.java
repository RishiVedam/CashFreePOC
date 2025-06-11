package com.example.Payment.client;

import com.example.Payment.dto.CreateOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "cashfreeClient", url = "https://sandbox.cashfree.com/pg")
public interface CashfreeClient {

    @PostMapping(value = "/orders", consumes = "application/json")
    Object createOrder(
            @RequestBody Map<String, Object> body,
            @RequestHeader("x-client-id") String clientId,
            @RequestHeader("x-client-secret") String clientSecret,
            @RequestHeader("x-api-version") String apiVersion
    );
}

