package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.PromoCodeRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.CreatePromoRequest;
import com.b4code.backend.dto.owner.PromoResponseDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.PromoCode;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerPromotionServiceImpl implements OwnerPromotionService {

    private final PromoCodeRepository promoCodeRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromoResponseDto> getPromotions(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        List<Property> ownerProperties = propertyRepository.findByOwnerId(owner.getId());
        Map<Long, String> propertyNameMap = ownerProperties.stream()
                .collect(Collectors.toMap(Property::getId, Property::getName, (a, b) -> a));

        List<PromoCode> list;
        if (propertyId != null) {
            verifyOwnsProperty(owner, propertyId);
            list = promoCodeRepository.findByPropertyIdOrderByValidToDesc(propertyId);
        } else {
            List<Long> propertyIds = ownerProperties.stream().map(Property::getId).toList();
            list = propertyIds.isEmpty()
                    ? new ArrayList<>()
                    : promoCodeRepository.findByPropertyIdInOrderByValidToDesc(propertyIds);
        }

        return list.stream()
                .map(p -> mapToDto(p, propertyNameMap.get(p.getPropertyId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromoResponseDto createPromotion(String ownerEmail, CreatePromoRequest request) {
        User owner = resolveOwner(ownerEmail);

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new CustomException("Valid to date must be on or after valid from date", HttpStatus.BAD_REQUEST);
        }

        String cleanCode = request.getCode().trim().toUpperCase();
        if (promoCodeRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new CustomException("A promo code with this code already exists: " + cleanCode, HttpStatus.CONFLICT);
        }

        String propName = null;
        if (request.getPropertyId() != null) {
            Property prop = verifyOwnsProperty(owner, request.getPropertyId());
            propName = prop.getName();
        }

        PromoCode promo = PromoCode.builder()
                .code(cleanCode)
                .description(request.getDescription().trim())
                .discountPercent(request.getDiscountPercent())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .maxUses(request.getMaxUses())
                .currentUses(0)
                .active(true)
                .propertyId(request.getPropertyId())
                .build();

        PromoCode saved = promoCodeRepository.save(promo);
        return mapToDto(saved, propName);
    }

    @Override
    @Transactional
    public PromoResponseDto togglePromotion(String ownerEmail, Long promoId) {
        User owner = resolveOwner(ownerEmail);
        PromoCode promo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new CustomException("Promo code not found", HttpStatus.NOT_FOUND));

        if (promo.getPropertyId() != null) {
            verifyOwnsProperty(owner, promo.getPropertyId());
        }

        promo.setActive(!Boolean.TRUE.equals(promo.getActive()));
        PromoCode saved = promoCodeRepository.save(promo);

        String propName = promo.getPropertyId() != null
                ? propertyRepository.findById(promo.getPropertyId()).map(Property::getName).orElse(null)
                : null;

        return mapToDto(saved, propName);
    }

    @Override
    @Transactional
    public void deletePromotion(String ownerEmail, Long promoId) {
        User owner = resolveOwner(ownerEmail);
        PromoCode promo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new CustomException("Promo code not found", HttpStatus.NOT_FOUND));

        if (promo.getPropertyId() != null) {
            verifyOwnsProperty(owner, promo.getPropertyId());
        }

        if (promo.getCurrentUses() != null && promo.getCurrentUses() > 0) {
            throw new CustomException("Cannot delete promo code that has already been used by guests. Please deactivate it instead.", HttpStatus.BAD_REQUEST);
        }

        promoCodeRepository.delete(promo);
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private Property verifyOwnsProperty(User owner, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied: You do not own this property", HttpStatus.FORBIDDEN);
        }
        return property;
    }

    private PromoResponseDto mapToDto(PromoCode promo, String propertyName) {
        return PromoResponseDto.builder()
                .id(promo.getId())
                .code(promo.getCode())
                .description(promo.getDescription())
                .discountPercent(promo.getDiscountPercent())
                .validFrom(promo.getValidFrom())
                .validTo(promo.getValidTo())
                .maxUses(promo.getMaxUses())
                .currentUses(promo.getCurrentUses())
                .active(promo.getActive())
                .isValid(promo.isValid())
                .propertyId(promo.getPropertyId())
                .propertyName(propertyName)
                .build();
    }
}
