package com.b4code.backend.rest;

import com.b4code.backend.dto.PayHereRequest;
import com.b4code.backend.dto.PaymentRequest;
import com.b4code.backend.dto.PaymentResponse;
import com.b4code.backend.service.PaymentService;
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
        // In a real system, we'd get the ID from the principal
        // For now, we'll try to cast to our User entity if possible, or use a helper
        Long userId = getUserIdFromAuthentication(authentication);
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
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 1L; // Fallback for dev/unauthenticated
        }
        // Assuming your User entity is the principal or you can extract it
        // If you use JWT, the ID might be in the claims or you fetch by email
        return 1L; // For now, keeping as 1L but through a helper to fix properly later
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




