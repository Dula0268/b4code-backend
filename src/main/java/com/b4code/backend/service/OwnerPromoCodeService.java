package com.b4code.backend.service;

import com.b4code.backend.dto.PromoCodeRequest;
import com.b4code.backend.dto.PromoCodeResponse;
import java.util.List;

public interface OwnerPromoCodeService {
    List<PromoCodeResponse> getAll(String ownerEmail);
    PromoCodeResponse getById(String ownerEmail, Long id);
    PromoCodeResponse create(String ownerEmail, PromoCodeRequest request);
    PromoCodeResponse update(String ownerEmail, Long id, PromoCodeRequest request);
    void delete(String ownerEmail, Long id);
}
