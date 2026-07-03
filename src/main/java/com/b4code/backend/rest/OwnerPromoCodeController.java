package com.b4code.backend.rest;

import com.b4code.backend.dto.PromoCodeRequest;
import com.b4code.backend.dto.PromoCodeResponse;
import com.b4code.backend.service.OwnerPromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/owner/promo-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerPromoCodeController {

    private final OwnerPromoCodeService ownerPromoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCodeResponse>> getAll(Principal principal) {
        return ResponseEntity.ok(ownerPromoCodeService.getAll(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> getById(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ownerPromoCodeService.getById(principal.getName(), id));
    }

    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(Principal principal,
                                                     @RequestBody PromoCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerPromoCodeService.create(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> update(Principal principal,
                                                     @PathVariable Long id,
                                                     @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(ownerPromoCodeService.update(principal.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        ownerPromoCodeService.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
