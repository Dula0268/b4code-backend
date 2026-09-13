package com.b4code.backend.service.impl;

import com.b4code.backend.dao.DiscountRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RatePlanRepository;
import com.b4code.backend.dao.RoomTypeRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.DiscountDto;
import com.b4code.backend.dto.owner.DiscountRequest;
import com.b4code.backend.dto.owner.OwnerRoomTypeDto;
import com.b4code.backend.dto.owner.RateOverviewDto;
import com.b4code.backend.dto.owner.RatePlanDto;
import com.b4code.backend.dto.owner.RatePlanRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Discount;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.RatePlan;
import com.b4code.backend.models.RoomType;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerRateServiceImpl implements OwnerRateService {

    private final RatePlanRepository ratePlanRepository;
    private final DiscountRepository discountRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public RateOverviewDto getRateOverview(String ownerEmail, Long propertyId) {
        verifyOwnsProperty(ownerEmail, propertyId);
        List<RatePlanDto> plans = ratePlanRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream().map(RatePlanDto::fromEntity).toList();
        List<DiscountDto> discounts = discountRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream().map(DiscountDto::fromEntity).toList();
        List<OwnerRoomTypeDto> rooms = roomTypeRepository.findByPropertyId(propertyId)
                .stream().map(OwnerRoomTypeDto::fromEntity).toList();
        return RateOverviewDto.builder()
                .propertyId(propertyId)
                .ratePlans(plans)
                .discounts(discounts)
                .rooms(rooms)
                .build();
    }

    @Override
    @Transactional
    public RatePlanDto createRatePlan(String ownerEmail, RatePlanRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());
        RoomType roomType = null;
        if (request.getRoomTypeId() != null) {
            roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
            if (!roomType.getProperty().getId().equals(property.getId())) {
                throw new CustomException("Room does not belong to this property", HttpStatus.BAD_REQUEST);
            }
        }

        RatePlan plan = RatePlan.builder()
                .property(property)
                .roomType(roomType)
                .name(request.getName())
                .type(request.getType())
                .basePrice(request.getBasePrice())
                .minNights(request.getMinNights() != null ? request.getMinNights() : 1)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        if (request.getBasePrice() != null && "STANDARD".equalsIgnoreCase(request.getType())) {
            syncRoomPrices(property.getId(), roomType, request.getBasePrice());
        }

        return RatePlanDto.fromEntity(ratePlanRepository.save(plan));
    }

    @Override
    @Transactional
    public RatePlanDto updateRatePlan(String ownerEmail, Long id, RatePlanRequest request) {
        User owner = resolveOwner(ownerEmail);
        RatePlan plan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rate plan not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(plan.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
            if (!roomType.getProperty().getId().equals(plan.getProperty().getId())) {
                throw new CustomException("Room does not belong to this property", HttpStatus.BAD_REQUEST);
            }
            plan.setRoomType(roomType);
        } else if (request.getPropertyId() != null) {
            plan.setRoomType(null);
        }

        if (request.getName() != null)      plan.setName(request.getName());
        if (request.getType() != null)      plan.setType(request.getType());
        if (request.getBasePrice() != null) plan.setBasePrice(request.getBasePrice());
        if (request.getMinNights() != null) plan.setMinNights(request.getMinNights());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)   plan.setEndDate(request.getEndDate());
        if (request.getIsActive() != null)  plan.setIsActive(request.getIsActive());

        if (plan.getBasePrice() != null && "STANDARD".equalsIgnoreCase(plan.getType())) {
            syncRoomPrices(plan.getProperty().getId(), plan.getRoomType(), plan.getBasePrice());
        }

        return RatePlanDto.fromEntity(ratePlanRepository.save(plan));
    }

    @Override
    @Transactional
    public void deleteRatePlan(String ownerEmail, Long id) {
        User owner = resolveOwner(ownerEmail);
        RatePlan plan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rate plan not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(plan.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        Long propertyId = plan.getProperty().getId();
        RoomType targetRoom = plan.getRoomType();
        ratePlanRepository.delete(plan);

        // Check if another active STANDARD plan remains for this room/property
        List<RatePlan> remaining = ratePlanRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        Optional<RatePlan> nextStandard = remaining.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && "STANDARD".equalsIgnoreCase(p.getType()))
                .filter(p -> targetRoom == null || p.getRoomType() == null || p.getRoomType().getId().equals(targetRoom.getId()))
                .findFirst();

        BigDecimal fallbackPrice = nextStandard.map(RatePlan::getBasePrice).orElse(new BigDecimal("10000.00"));
        syncRoomPrices(propertyId, targetRoom, fallbackPrice);
    }

    @Override
    @Transactional
    public DiscountDto createDiscount(String ownerEmail, DiscountRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());
        RoomType roomType = null;
        if (request.getRoomTypeId() != null) {
            roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
            if (!roomType.getProperty().getId().equals(property.getId())) {
                throw new CustomException("Room does not belong to this property", HttpStatus.BAD_REQUEST);
            }
        }

        Discount d = Discount.builder()
                .property(property)
                .roomType(roomType)
                .name(request.getName())
                .type(request.getType())
                .percentage(request.getPercentage())
                .minNights(request.getMinNights())
                .daysInAdvance(request.getDaysInAdvance())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return DiscountDto.fromEntity(discountRepository.save(d));
    }

    @Override
    @Transactional
    public DiscountDto updateDiscount(String ownerEmail, Long id, DiscountRequest request) {
        User owner = resolveOwner(ownerEmail);
        Discount d = discountRepository.findById(id)
                .orElseThrow(() -> new CustomException("Discount not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(d.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
            if (!roomType.getProperty().getId().equals(d.getProperty().getId())) {
                throw new CustomException("Room does not belong to this property", HttpStatus.BAD_REQUEST);
            }
            d.setRoomType(roomType);
        } else if (request.getPropertyId() != null) {
            d.setRoomType(null);
        }

        if (request.getName() != null)         d.setName(request.getName());
        if (request.getType() != null)         d.setType(request.getType());
        if (request.getPercentage() != null)   d.setPercentage(request.getPercentage());
        if (request.getMinNights() != null)    d.setMinNights(request.getMinNights());
        if (request.getDaysInAdvance() != null) d.setDaysInAdvance(request.getDaysInAdvance());
        if (request.getStartDate() != null)    d.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)      d.setEndDate(request.getEndDate());
        if (request.getIsActive() != null)     d.setIsActive(request.getIsActive());
        return DiscountDto.fromEntity(discountRepository.save(d));
    }

    private void syncRoomPrices(Long propertyId, RoomType roomType, BigDecimal price) {
        if (roomType != null) {
            roomType.setPricePerNight(price);
            roomTypeRepository.save(roomType);
        } else {
            List<RoomType> allRooms = roomTypeRepository.findByPropertyId(propertyId);
            for (RoomType rt : allRooms) {
                rt.setPricePerNight(price);
            }
            roomTypeRepository.saveAll(allRooms);
        }
    }

    @Override
    @Transactional
    public void deleteDiscount(String ownerEmail, Long id) {
        User owner = resolveOwner(ownerEmail);
        Discount d = discountRepository.findById(id)
                .orElseThrow(() -> new CustomException("Discount not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(d.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        discountRepository.delete(d);
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private void verifyOwnsProperty(String ownerEmail, Long propertyId) {
        resolveOwnedProperty(ownerEmail, propertyId);
    }

    private Property resolveOwnedProperty(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        return property;
    }
}
