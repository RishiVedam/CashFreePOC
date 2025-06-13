package com.example.Payment.dto;

import lombok.Data;

@Data
public class SaveCustomerRequest {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Double orderAmount;
}

