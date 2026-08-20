package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomTypeRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerRoomTypeDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeListDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.BedType;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.RoomType;
import com.b4code.backend.models.RoomCategory;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.RoomStatus;
import com.b4code.backend.service.OwnerRoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerRoomTypeServiceImpl implements OwnerRoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public OwnerRoomTypeListDto listRoomTypes(String ownerEmail, String statusParam, String search) {
        User owner = resolveOwner(ownerEmail);

        RoomStatus statusFilter = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try { statusFilter = RoomStatus.valueOf(statusParam.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        List<RoomType> roomTypes = roomTypeRepository.findByOwnerWithFilters(owner.getId(), statusFilter, searchTerm);

        long total = roomTypeRepository.countByOwner(owner.getId());
        long occupied = roomTypeRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.OCCUPIED);
        long maintenance = roomTypeRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.MAINTENANCE);
        long vacant = roomTypeRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.AVAILABLE);

        List<OwnerRoomTypeDto> dtos = roomTypes.stream().map(OwnerRoomTypeDto::fromEntity).toList();

        return OwnerRoomTypeListDto.builder()
                .roomTypes(dtos)
                .totalRoomTypes(total)
                .occupied(occupied)
                .maintenance(maintenance)
                .vacant(vacant)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerRoomTypeDto getRoomType(String ownerEmail, Long roomId) {
        return OwnerRoomTypeDto.fromEntity(resolveOwnedRoom(ownerEmail, roomId));
    }

    @Override
    @Transactional
    public OwnerRoomTypeDto createRoom(String ownerEmail, OwnerRoomTypeRequest request) {
        User owner = resolveOwner(ownerEmail);

        if (request.getPropertyId() == null) {
            throw new CustomException("propertyId is required.", HttpStatus.BAD_REQUEST);
        }
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found: " + request.getPropertyId(), HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Property does not belong to this owner.", HttpStatus.FORBIDDEN);
        }

        RoomType roomType = RoomType.builder()
                .property(property)
                .name(request.getName())
                .description(request.getDescription())
                .roomCategory(parseRoomCategory(request.getRoomCategory()))
                .maxOccupancy(request.getMaxOccupancy() != null ? request.getMaxOccupancy() : 2)
                .maxChildren(request.getMaxChildren() != null ? request.getMaxChildren() : 0)
                .bedType(parseBedType(request.getBedType()))
                .pricePerNight(request.getPricePerNight() != null ? request.getPricePerNight() : BigDecimal.ZERO)
                .inventory(request.getInventory() != null ? request.getInventory() : 1)
                .status(parseRoomStatus(request.getStatus()))
                .build();

        RoomType saved = roomTypeRepository.save(roomType);
        log.info("Owner {} created roomType id={} for property id={}", ownerEmail, saved.getId(), property.getId());
        return OwnerRoomTypeDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerRoomTypeDto updateRoom(String ownerEmail, Long roomId, OwnerRoomTypeRequest request) {
        RoomType roomType = resolveOwnedRoom(ownerEmail, roomId);

        if (request.getName() != null)          roomType.setName(request.getName());
        if (request.getDescription() != null)   roomType.setDescription(request.getDescription());
        if (request.getRoomCategory() != null)      roomType.setRoomCategory(parseRoomCategory(request.getRoomCategory()));
        if (request.getMaxOccupancy() != null)  roomType.setMaxOccupancy(request.getMaxOccupancy());
        if (request.getMaxChildren() != null)   roomType.setMaxChildren(request.getMaxChildren());
        if (request.getBedType() != null)       roomType.setBedType(parseBedType(request.getBedType()));
        if (request.getPricePerNight() != null) roomType.setPricePerNight(request.getPricePerNight());
        if (request.getInventory() != null)     roomType.setInventory(request.getInventory());
        if (request.getStatus() != null)        roomType.setStatus(parseRoomStatus(request.getStatus()));

        RoomType saved = roomTypeRepository.save(roomType);
        log.info("Owner {} updated roomType id={}", ownerEmail, roomId);
        return OwnerRoomTypeDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteRoom(String ownerEmail, Long roomId) {
        RoomType roomType = resolveOwnedRoom(ownerEmail, roomId);
        roomTypeRepository.delete(roomType);
        log.info("Owner {} deleted roomType id={}", ownerEmail, roomId);
    }

    @Override
    @Transactional
    public OwnerRoomTypeDto updateStatus(String ownerEmail, Long roomId, String statusParam) {
        RoomType roomType = resolveOwnedRoom(ownerEmail, roomId);
        roomType.setStatus(parseRoomStatus(statusParam));
        RoomType saved = roomTypeRepository.save(roomType);
        log.info("Owner {} set roomType id={} status → {}", ownerEmail, roomId, statusParam);
        return OwnerRoomTypeDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerRoomTypeDto toggleAvailability(String ownerEmail, Long roomId) {
        RoomType roomType = resolveOwnedRoom(ownerEmail, roomId);
        boolean next = roomType.getStatus() != RoomStatus.AVAILABLE;
        roomType.setStatus(next ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE);
        RoomType saved = roomTypeRepository.save(roomType);
        log.info("Owner {} toggled roomType id={} availability → {}", ownerEmail, roomId, next);
        return OwnerRoomTypeDto.fromEntity(saved);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found: " + email, HttpStatus.NOT_FOUND));
    }

    private RoomType resolveOwnedRoom(String ownerEmail, Long roomId) {
        User owner = resolveOwner(ownerEmail);
        return roomTypeRepository.findByIdAndPropertyOwnerId(roomId, owner.getId())
                .orElseThrow(() -> new CustomException(
                        "Room not found or does not belong to this owner.", HttpStatus.NOT_FOUND));
    }

    private RoomCategory parseRoomCategory(String value) {
        if (value == null) return RoomCategory.STANDARD_ROOM;
        String normalized = value.toUpperCase().replace(" ", "_");
        try { return RoomCategory.valueOf(normalized); }
        catch (IllegalArgumentException e) { return RoomCategory.STANDARD_ROOM; }
    }

    private BedType parseBedType(String value) {
        if (value == null) return null;
        try { return BedType.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private RoomStatus parseRoomStatus(String value) {
        if (value == null) return RoomStatus.AVAILABLE;
        String v = value.toUpperCase();
        if (v.equals("ACTIVE") || v.equals("AVAILABLE")) return RoomStatus.AVAILABLE;
        if (v.equals("OCCUPIED")) return RoomStatus.OCCUPIED;
        if (v.equals("MAINTENANCE")) return RoomStatus.MAINTENANCE;
        try { return RoomStatus.valueOf(v); }
        catch (IllegalArgumentException e) { return RoomStatus.AVAILABLE; }
    }
}

