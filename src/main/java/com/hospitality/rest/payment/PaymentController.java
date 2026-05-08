package com.hospitality.rest.payment;

import com.hospitality.dto.payment.PayHereRequest;
import com.hospitality.dto.payment.PaymentRequest;
import com.hospitality.dto.payment.PaymentResponse;
import com.hospitality.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Initiate payment - returns PayHere checkout params
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long userId = 1L; // Will be properly wired when connecting frontend
        return ResponseEntity.ok(paymentService.initiatePayment(request, userId));
    }

    // PayHere notification - called by PayHere after payment
    @PostMapping("/notify")
    public ResponseEntity<String> handleNotification(
            @ModelAttribute PayHereRequest notify) {
        paymentService.handleNotification(notify);
        return ResponseEntity.ok("OK");
    }

    // Get all payments - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // Get current user's payments
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        Long userId = 1L;
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // Refund - Admin only
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }
}