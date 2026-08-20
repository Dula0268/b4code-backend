package com.b4code.backend.service;

import com.b4code.backend.dto.PayHereRequest;
import com.b4code.backend.dto.PaymentRequest;
import com.b4code.backend.dto.PaymentResponse;
import com.b4code.backend.models.Payment;
import com.b4code.backend.dao.PaymentRepository;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.b4code.backend.dao.TransactionRepository;
import com.b4code.backend.models.Transaction;
import com.b4code.backend.models.enums.TransactionType;
import com.b4code.backend.models.enums.UserRole;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final OrderSseService orderSseService;
    private final EmailService emailService;
    private final TransactionRepository transactionRepository;

    @Value("${payhere.merchant-id}")
    private String merchantId;

    @Value("${payhere.merchant-secret}")
    private String merchantSecret;

    @Value("${payhere.checkout-url}")
    private String checkoutUrl;

    @Value("${payhere.return-url:http://localhost:3000/guest/booking}")
    private String returnUrl;

    @Value("${payhere.cancel-url:http://localhost:3000/payment}")
    private String cancelUrl;

    public PaymentResponse initiatePayment(PaymentRequest request, Long userId) {
        // Create payment record with PENDING status
        Payment payment = new Payment();
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            payment.setUser(user);
        }
        if (request.getBookingId() != null) {
            Booking booking = new Booking();
            booking.setId(request.getBookingId());
            payment.setBooking(booking);
        }
        if (request.getFoodOrderId() != null) {
            payment.setFoodOrderId(request.getFoodOrderId());
        }
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        payment.setPaymentMethod(request.getPaymentMethod());

        // Initial status logic
        if ("card".equalsIgnoreCase(request.getPaymentMethod())) {
            // For card payments, we start as PENDING and wait for notify_url callback
            payment.setStatus(Payment.PaymentStatus.PENDING);
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
                // payment.setTransactionId(notify.getPayment_id());
                if (notify.getCard_holder_name() != null)
                    payment.setCardHolderName(notify.getCard_holder_name());
                if (notify.getCard_no() != null)
                    payment.setCardLastFour(notify.getCard_no());

                // Update linked booking status when payment succeeds
                if (payment.getBooking() != null && payment.getBooking().getId() != null) {
                    bookingRepository.findById(payment.getBooking().getId()).ifPresent(booking -> {
                        booking.setStatus(Booking.BookingStatus.CONFIRMED);
                        bookingRepository.save(booking);
                    });
                }
                // Update linked food order status from PAYMENT_PENDING -> PLACED
                if (payment.getFoodOrderId() != null) {
                    orderRepository.findById(payment.getFoodOrderId()).ifPresent(order -> {
                        if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                            order.setStatus(OrderStatus.PLACED);
                            Order savedOrder = orderRepository.save(order);
                            // Broadcast SSE event so staff dashboard sees the new order
                            orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "new-order", savedOrder);
                        }
                    });
                }
                recordTransaction(payment);
            }
            case "-1", "-2", "-3" -> {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(notify.getStatus_message());
            }
        }

        paymentRepository.save(payment);
    }

    public PaymentResponse verifyLocalPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for order: " + orderId));

        payment.setStatus(Payment.PaymentStatus.SUCCESS);

        if (payment.getBooking() != null && payment.getBooking().getId() != null) {
            bookingRepository.findById(payment.getBooking().getId()).ifPresent(booking -> {
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            });
        }

        if (payment.getFoodOrderId() != null) {
            orderRepository.findById(payment.getFoodOrderId()).ifPresent(order -> {
                if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                    order.setStatus(OrderStatus.PLACED);
                    Order savedOrder = orderRepository.save(order);
                    orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "new-order", savedOrder);
                }
            });
        }

        recordTransaction(payment);
        paymentRepository.save(payment);
        return PaymentResponse.fromEntity(payment);
    }

    public void recordTransaction(Payment payment) {
        if (payment == null || payment.getOrderId() == null) return;

        try {
            if (transactionRepository.findByReferenceNumber(payment.getOrderId()).isPresent()) {
                return;
            }

            com.b4code.backend.models.Property property = null;
            if (payment.getBooking() != null && payment.getBooking().getId() != null) {
                Booking b = bookingRepository.findById(payment.getBooking().getId()).orElse(null);
                if (b != null) property = b.getProperty();
            }

            Transaction tx = new Transaction();
            tx.setReferenceNumber(payment.getOrderId());
            tx.setAmount(BigDecimal.valueOf(payment.getAmount()));
            tx.setCurrency(payment.getCurrency() != null ? payment.getCurrency() : "LKR");
            tx.setType(TransactionType.BOOKING_PAYMENT);
            tx.setProperty(property);
            tx.setUser(payment.getUser());
            tx.setDescription("Booking Payment #" + (payment.getBooking() != null ? payment.getBooking().getConfirmationCode() : payment.getOrderId()));
            transactionRepository.save(tx);

            // Record 20% platform commission entry
            Transaction commTx = new Transaction();
            commTx.setReferenceNumber("COMM-" + payment.getOrderId());
            commTx.setAmount(BigDecimal.valueOf(payment.getAmount()).multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP));
            commTx.setCurrency(payment.getCurrency() != null ? payment.getCurrency() : "LKR");
            commTx.setType(TransactionType.COMMISSION);
            commTx.setProperty(property);
            commTx.setUser(payment.getUser());
            commTx.setDescription("Platform Commission (20%) for Order #" + payment.getOrderId());
            transactionRepository.save(commTx);

            log.info("[TRANSACTION] Recorded finance ledger transactions for order {}", payment.getOrderId());
        } catch (Exception e) {
            log.error("[TRANSACTION] Failed to record transaction for order {}", payment.getOrderId(), e);
        }
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getUserPayments(User user) {
        if (user == null) {
            return List.of();
        }
        if (user.getRole() == UserRole.OWNER) {
            return paymentRepository.findByPropertyOwnerId(user.getId())
                    .stream()
                    .map(PaymentResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        if (user.getRole() == UserRole.ADMIN) {
            return getAllPayments();
        }
        return getUserPayments(user.getId());
    }

    public List<PaymentResponse> getUserPayments(Long userId) {
        if (userId == null) return List.of();
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
            String secretHash = md5(merchantSecret).toUpperCase();
            String hashStr = merchantId + orderId + String.format(java.util.Locale.US, "%.2f", amount) + currency
                    + secretHash;
            return md5(hashStr).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private String generateNotifyHash(String merchantId, String orderId,
            String amount, String currency, String statusCode) {
        try {
            String secretHash = md5(merchantSecret).toUpperCase();
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
        // Use different base return URLs for food orders vs booking payments
        String baseReturnUrl = (payment.getFoodOrderId() != null)
                ? returnUrl.replace("/guest/booking", "/guest/order/confirmation")
                : returnUrl;
        String finalReturnUrl = baseReturnUrl;
        if (request.getReturnParams() != null && !request.getReturnParams().isBlank()) {
            finalReturnUrl = baseReturnUrl + (baseReturnUrl.contains("?") ? "&" : "?") + request.getReturnParams();
        }
        try {
            return "merchant_id=" + merchantId +
                    "&return_url=" + java.net.URLEncoder.encode(finalReturnUrl, "UTF-8") +
                    "&cancel_url=" + java.net.URLEncoder.encode(cancelUrl, "UTF-8") +
                    "&notify_url=" + java.net.URLEncoder.encode("http://localhost:8080/api/payments/notify", "UTF-8") +
                    "&order_id=" + payment.getOrderId() +
                    "&items=" + (payment.getFoodOrderId() != null ? "Food+Order" : "PrimeStay+Booking") +
                    "&currency=" + payment.getCurrency() +
                    "&amount=" + String.format(java.util.Locale.US, "%.2f", payment.getAmount()) +
                    "&first_name=" + (request.getFirstName() != null ? request.getFirstName() : "Guest") +
                    "&last_name=" + (request.getLastName() != null ? request.getLastName() : "User") +
                    "&email=" + (request.getEmail() != null ? request.getEmail() : "") +
                    "&phone=" + (request.getPhone() != null ? request.getPhone() : "") +
                    "&address=Colombo" +
                    "&city=Colombo" +
                    "&country=Sri+Lanka" +
                    "&hash=" + hash;
        } catch (Exception e) {
            throw new RuntimeException("Error encoding URLs", e);
        }
    }
}
