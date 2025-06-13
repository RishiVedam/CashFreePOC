package com.example.Payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {

    @JsonProperty("payment_session_id")
    private String sessionId;

//    @JsonProperty("cf_payment_id")
//    private Long cfPaymentId;

    @JsonProperty("order_id")
    private String orderId;

}

