package com.b4code.backend.controller;

import com.b4code.backend.models.Promotion;
import com.b4code.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Note: Should be configured via global CORS policy in prod
public class PromotionController {

    private final PromotionRepository promotionRepository;

    @GetMapping("/validate")
    public ResponseEntity<?> validatePromotion(@RequestParam String code) {
        Optional<Promotion> promotionOpt = promotionRepository.findByCodeAndIsActiveTrue(code);

        if (promotionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or inactive promotion code"));
        }

        Promotion promo = promotionOpt.get();
        LocalDate now = LocalDate.now();

        if (now.isBefore(promo.getValidFrom()) || now.isAfter(promo.getValidUntil())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Promotion code has expired or is not yet valid"));
        }

        return ResponseEntity.ok(Map.of(
                "code", promo.getCode(),
                "discountPercentage", promo.getDiscountPercentage()
        ));
    }
}
