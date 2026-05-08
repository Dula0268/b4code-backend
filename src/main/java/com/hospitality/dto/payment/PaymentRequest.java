package com.hospitality.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String cardHolderName;
    private String cardNumber;
    private String cardExpiry;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String description;
}