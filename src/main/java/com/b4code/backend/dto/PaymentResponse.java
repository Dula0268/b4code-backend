package com.b4code.backend.dto;

import com.b4code.backend.models.Payment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {
    private Long id;
    private Long userId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String orderId;
    private String transactionId;
    private String cardHolderName;
    private String cardLastFour;
    private String failureReason;
    private LocalDateTime createdAt;

    // PayHere checkout URL - returned when initiating payment
    private String checkoutUrl;
    private String payHereParams;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus().name());
        response.setOrderId(payment.getOrderId());
        response.setTransactionId(payment.getTransactionId());
        response.setCardHolderName(payment.getCardHolderName());
        response.setCardLastFour(payment.getCardLastFour());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}

