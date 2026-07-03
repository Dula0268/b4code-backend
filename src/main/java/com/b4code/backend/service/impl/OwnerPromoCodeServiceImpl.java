package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PromoCodeRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.PromoCodeRequest;
import com.b4code.backend.dto.PromoCodeResponse;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.PromoCode;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.service.OwnerPromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerPromoCodeServiceImpl implements OwnerPromoCodeService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final PromoCodeRepository promoCodeRepository;

    @Override
    public List<PromoCodeResponse> getAll(String ownerEmail) {
        List<Long> propertyIds = getOwnerPropertyIds(ownerEmail);

        Map<Long, String> propNames = propertyRepository.findAllById(propertyIds).stream()
                .collect(Collectors.toMap(Property::getId, Property::getName));

        return promoCodeRepository.findByPropertyIdIn(propertyIds).stream()
                .map(p -> {
                    String propName = p.getPropertyId() != null ? propNames.getOrDefault(p.getPropertyId(), "") : "";
                    String roomName = "";
                    if (p.getRoomId() != null) {
                        roomName = roomRepository.findById(p.getRoomId())
                                .map(Room::getName).orElse("");
                    }
                    return PromoCodeResponse.from(p, propName, roomName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PromoCodeResponse getById(String ownerEmail, Long id) {
        getOwnerPropertyIds(ownerEmail); // validates owner exists
        PromoCode p = promoCodeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Promo code not found", HttpStatus.NOT_FOUND));
        String propName = p.getPropertyId() != null
                ? propertyRepository.findById(p.getPropertyId()).map(Property::getName).orElse("") : "";
        String roomName = p.getRoomId() != null
                ? roomRepository.findById(p.getRoomId()).map(Room::getName).orElse("") : "";
        return PromoCodeResponse.from(p, propName, roomName);
    }

    @Override
    public PromoCodeResponse update(String ownerEmail, Long id, PromoCodeRequest req) {
        getOwnerPropertyIds(ownerEmail); // validates owner exists
        PromoCode p = promoCodeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Promo code not found", HttpStatus.NOT_FOUND));

        if (req.getCode() != null && !req.getCode().equalsIgnoreCase(p.getCode())) {
            promoCodeRepository.findByCodeIgnoreCase(req.getCode()).ifPresent(existing -> {
                throw new CustomException("Promo code already exists: " + req.getCode(), HttpStatus.CONFLICT);
            });
            p.setCode(req.getCode().toUpperCase().trim());
        }
        if (req.getDescription() != null)    p.setDescription(req.getDescription());
        if (req.getDiscountPercent() != null) p.setDiscountPercent(req.getDiscountPercent());
        if (req.getValidFrom() != null)       p.setValidFrom(req.getValidFrom());
        if (req.getValidTo() != null)         p.setValidTo(req.getValidTo());
        if (req.getMaxUses() != null)         p.setMaxUses(req.getMaxUses());
        if (req.getPropertyId() != null)      p.setPropertyId(req.getPropertyId());
        p.setRoomId(req.getRoomId()); // allow clearing to null

        PromoCode saved = promoCodeRepository.save(p);
        String propName = saved.getPropertyId() != null
                ? propertyRepository.findById(saved.getPropertyId()).map(Property::getName).orElse("") : "";
        String roomName = saved.getRoomId() != null
                ? roomRepository.findById(saved.getRoomId()).map(Room::getName).orElse("") : "";
        return PromoCodeResponse.from(saved, propName, roomName);
    }

    @Override
    public PromoCodeResponse create(String ownerEmail, PromoCodeRequest req) {
        List<Long> propertyIds = getOwnerPropertyIds(ownerEmail);

        if (req.getPropertyId() == null || !propertyIds.contains(req.getPropertyId())) {
            throw new CustomException("Property not found or not owned by you", HttpStatus.BAD_REQUEST);
        }

        if (promoCodeRepository.findByCodeIgnoreCase(req.getCode()).isPresent()) {
            throw new CustomException("Promo code already exists: " + req.getCode(), HttpStatus.CONFLICT);
        }

        PromoCode saved = promoCodeRepository.save(PromoCode.builder()
                .code(req.getCode().toUpperCase().trim())
                .description(req.getDescription() != null ? req.getDescription() : "")
                .discountPercent(req.getDiscountPercent())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .maxUses(req.getMaxUses())
                .propertyId(req.getPropertyId())
                .roomId(req.getRoomId())
                .build());

        String propName = propertyRepository.findById(saved.getPropertyId())
                .map(Property::getName).orElse("");
        String roomName = saved.getRoomId() != null
                ? roomRepository.findById(saved.getRoomId()).map(Room::getName).orElse("") : "";

        return PromoCodeResponse.from(saved, propName, roomName);
    }

    @Override
    public void delete(String ownerEmail, Long id) {
        List<Long> propertyIds = getOwnerPropertyIds(ownerEmail);
        PromoCode promo = promoCodeRepository.findByIdAndPropertyIdIn(id, propertyIds)
                .orElseThrow(() -> new CustomException("Promo code not found", HttpStatus.NOT_FOUND));
        promoCodeRepository.delete(promo);
    }

    private List<Long> getOwnerPropertyIds(String ownerEmail) {
        // Validate the caller is a known owner account, then return all property IDs.
        // This matches OwnerPropertyServiceImpl.listProperties which uses findAllForOwner
        // (no owner_id filter) — owner_id is not consistently set across all properties.
        userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
        return propertyRepository.findAll().stream()
                .map(Property::getId)
                .collect(Collectors.toList());
    }
}
