package com.example.Payment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequest {

    private Double orderAmount;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerId;

}
