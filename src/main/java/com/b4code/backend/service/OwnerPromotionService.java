package com.b4code.backend.service;

import com.b4code.backend.dto.owner.CreatePromoRequest;
import com.b4code.backend.dto.owner.PromoResponseDto;
import java.util.List;

public interface OwnerPromotionService {

    List<PromoResponseDto> getPromotions(String ownerEmail, Long propertyId);

    PromoResponseDto createPromotion(String ownerEmail, CreatePromoRequest request);

    PromoResponseDto togglePromotion(String ownerEmail, Long promoId);

    void deletePromotion(String ownerEmail, Long promoId);
}
