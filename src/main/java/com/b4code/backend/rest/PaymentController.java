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
    private final com.b4code.backend.dao.UserRepository userRepository;

    // Initiate payment - returns PayHere checkout params
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody PaymentRequest request,
            Authentication authentication) {
        com.b4code.backend.models.User user = getAuthenticatedUser(authentication);
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(paymentService.initiatePayment(request, userId));
    }

    // PayHere notification - called by PayHere after payment
    @PostMapping("/notify")
    public ResponseEntity<String> handleNotification(
            @ModelAttribute PayHereRequest notify) {
        paymentService.handleNotification(notify);
        return ResponseEntity.ok("OK");
    }

    // Local / Dev testing verification helper
    @PostMapping("/verify-local/{orderId}")
    public ResponseEntity<PaymentResponse> verifyPaymentLocal(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.verifyLocalPayment(orderId));
    }

    // Get all payments - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // Get current user's payments (filtered by property if owner)
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        com.b4code.backend.models.User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(paymentService.getUserPayments(user));
    }

    private com.b4code.backend.models.User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
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




