package com.hospitality.service;

import com.hospitality.dto.payment.PayHereRequest;

import com.hospitality.dto.payment.PaymentRequest;
import com.hospitality.dto.payment.PaymentResponse;
import com.hospitality.models.Payment;
import com.hospitality.dao.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${payhere.merchant-id}")
    private String merchantId;

    @Value("${payhere.merchant-secret}")
    private String merchantSecret;

    @Value("${payhere.checkout-url}")
    private String checkoutUrl;

    public PaymentResponse initiatePayment(PaymentRequest request, Long userId) {
        // Create payment record with PENDING status
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        payment.setPaymentMethod(request.getPaymentMethod());
        if ("card".equalsIgnoreCase(request.getPaymentMethod())) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(Payment.PaymentStatus.PENDING);
        }
        payment.setOrderId("ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        if (request.getCardHolderName() != null) {
            payment.setCardHolderName(request.getCardHolderName());
        }
        if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
            String cleaned = request.getCardNumber().replaceAll("\\s", "");
            payment.setCardLastFour(cleaned.substring(cleaned.length() - 4));
        }

        paymentRepository.save(payment);

        // Build PayHere response
        PaymentResponse response = PaymentResponse.fromEntity(payment);
        response.setCheckoutUrl(checkoutUrl);

        // Generate hash for PayHere
        String hash = generateHash(payment.getOrderId(), request.getAmount(), payment.getCurrency());
        response.setPayHereParams(buildPayHereParams(payment, request, hash));

        return response;
    }

    public void handleNotification(PayHereRequest notify) {
        Payment payment = paymentRepository.findByOrderId(notify.getOrder_id())
                .orElseThrow(() -> new RuntimeException("Payment not found: " + notify.getOrder_id()));

        // Verify MD5 signature
        String localMd5 = generateNotifyHash(
                notify.getMerchant_id(),
                notify.getOrder_id(),
                notify.getPayhere_amount(),
                notify.getPayhere_currency(),
                notify.getStatus_code());

        if (!localMd5.equalsIgnoreCase(notify.getMd5sig())) {
            throw new RuntimeException("Invalid payment notification signature");
        }

        // Update payment status based on PayHere status code
        // 2 = SUCCESS, 0 = PENDING, -1 = CANCELLED, -2 = FAILED, -3 = CHARGEDBACK
        switch (notify.getStatus_code()) {
            case "2" -> {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setTransactionId(notify.getPayment_id());
                if (notify.getCard_holder_name() != null)
                    payment.setCardHolderName(notify.getCard_holder_name());
                if (notify.getCard_no() != null)
                    payment.setCardLastFour(notify.getCard_no());
            }
            case "-1", "-2", "-3" -> {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(notify.getStatus_message());
            }
        }

        paymentRepository.save(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        return PaymentResponse.fromEntity(payment);
    }

    public PaymentResponse getPaymentById(Long id) {
        return PaymentResponse.fromEntity(
                paymentRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Payment not found: " + id)));
    }

    private String generateHash(String orderId, Double amount, String currency) {
        try {
            String secretHash = md5(merchantSecret.toUpperCase());
            String hashStr = merchantId + orderId + String.format("%.2f", amount) + currency + secretHash;
            return md5(hashStr).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private String generateNotifyHash(String merchantId, String orderId,
            String amount, String currency, String statusCode) {
        try {
            String secretHash = md5(merchantSecret.toUpperCase());
            String hashStr = merchantId + orderId + amount + currency + statusCode + secretHash;
            return md5(hashStr).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error generating notify hash", e);
        }
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashText = no.toString(16);
        while (hashText.length() < 32)
            hashText = "0" + hashText;
        return hashText;
    }

    private String buildPayHereParams(Payment payment, PaymentRequest request, String hash) {
        return "merchant_id=" + merchantId +
                "&return_url=http://localhost:3003/guest/booking/confirmation" +
                "&cancel_url=http://localhost:3003/payment" +
                "&notify_url=http://localhost:8080/api/payments/notify" +
                "&order_id=" + payment.getOrderId() +
                "&items=PrimeStay Booking" +
                "&currency=" + payment.getCurrency() +
                "&amount=" + String.format("%.2f", payment.getAmount()) +
                "&first_name=" + (request.getFirstName() != null ? request.getFirstName() : "Guest") +
                "&last_name=" + (request.getLastName() != null ? request.getLastName() : "User") +
                "&email=" + (request.getEmail() != null ? request.getEmail() : "") +
                "&phone=" + (request.getPhone() != null ? request.getPhone() : "") +
                "&address=Colombo" +
                "&city=Colombo" +
                "&country=Sri Lanka" +
                "&hash=" + hash;
    }
}