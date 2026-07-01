package com.b4code.backend.service.impl;

import com.b4code.backend.dao.ImageRepository;
import com.b4code.backend.dao.PhysicalRoomRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerRoomDto;
import com.b4code.backend.dto.owner.OwnerRoomListDto;
import com.b4code.backend.dto.owner.OwnerRoomRequest;
import com.b4code.backend.dto.owner.PhysicalRoomDto;
import com.b4code.backend.models.PhysicalRoom;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.BedType;
import com.b4code.backend.models.Image;
import com.b4code.backend.models.ImageType;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.RoomType;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.RoomStatus;
import com.b4code.backend.service.OwnerRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerRoomServiceImpl implements OwnerRoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final PhysicalRoomRepository physicalRoomRepository;

    @Override
    @Transactional(readOnly = true)
    public OwnerRoomListDto listRooms(String ownerEmail, String statusParam, String search, int page, int size) {
        RoomStatus statusFilter = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try { statusFilter = RoomStatus.valueOf(statusParam.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        int zeroPage = Math.max(0, page - 1);
        PageRequest pageable = PageRequest.of(zeroPage, Math.max(1, size));

        // Use no-owner-filter queries — all properties are in the shared table
        Page<Room> pageResult = (statusFilter == null)
                ? roomRepository.findAllForOwner(searchTerm, pageable)
                : roomRepository.findAllForOwnerWithStatus(statusFilter, searchTerm, pageable);

        long total      = roomRepository.count();
        long occupied   = roomRepository.countAllByStatus(RoomStatus.OCCUPIED);
        long maintenance = roomRepository.countAllByStatus(RoomStatus.MAINTENANCE);
        long vacant     = roomRepository.countAllByStatus(RoomStatus.AVAILABLE);

        List<OwnerRoomDto> dtos = pageResult.getContent().stream().map(OwnerRoomDto::fromEntity).toList();

        return OwnerRoomListDto.builder()
                .rooms(dtos)
                .totalRooms(total)
                .occupied(occupied)
                .maintenance(maintenance)
                .vacant(vacant)
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerRoomDto getRoom(String ownerEmail, Long roomId) {
        return OwnerRoomDto.fromEntity(resolveOwnedRoom(ownerEmail, roomId));
    }

    @Override
    @Transactional
    public OwnerRoomDto createRoom(String ownerEmail, OwnerRoomRequest request) {
        User owner = resolveOwner(ownerEmail);

        if (request.getPropertyId() == null) {
            throw new CustomException("propertyId is required.", HttpStatus.BAD_REQUEST);
        }
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found: " + request.getPropertyId(), HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Property does not belong to this owner.", HttpStatus.FORBIDDEN);
        }

        Image image = null;
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            image = imageRepository.save(Image.builder()
                    .url(request.getImageUrl())
                    .type(ImageType.ROOM)
                    .build());
        }

        Room room = Room.builder()
                .property(property)
                .name(request.getName())
                .description(request.getDescription())
                .roomType(parseRoomType(request.getRoomType()))
                .maxOccupancy(request.getMaxOccupancy() != null ? request.getMaxOccupancy() : 2)
                .maxChildren(request.getMaxChildren() != null ? request.getMaxChildren() : 0)
                .bedType(parseBedType(request.getBedType()))
                .pricePerNight(request.getPricePerNight() != null ? request.getPricePerNight() : BigDecimal.ZERO)
                .inventory(request.getInventory() != null ? request.getInventory() : 1)
                .status(parseRoomStatus(request.getStatus()))
                .isAvailable(true)
                .image(image)
                .build();

        Room saved = roomRepository.save(room);
        log.info("Owner {} created room id={} for property id={}", ownerEmail, saved.getId(), property.getId());
        return OwnerRoomDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerRoomDto updateRoom(String ownerEmail, Long roomId, OwnerRoomRequest request) {
        Room room = resolveOwnedRoom(ownerEmail, roomId);

        if (request.getName() != null)          room.setName(request.getName());
        if (request.getDescription() != null)   room.setDescription(request.getDescription());
        if (request.getRoomType() != null)      room.setRoomType(parseRoomType(request.getRoomType()));
        if (request.getMaxOccupancy() != null)  room.setMaxOccupancy(request.getMaxOccupancy());
        if (request.getMaxChildren() != null)   room.setMaxChildren(request.getMaxChildren());
        if (request.getBedType() != null)       room.setBedType(parseBedType(request.getBedType()));
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getInventory() != null)     room.setInventory(request.getInventory());
        if (request.getStatus() != null)        room.setStatus(parseRoomStatus(request.getStatus()));

        Room saved = roomRepository.save(room);
        log.info("Owner {} updated room id={}", ownerEmail, roomId);
        return OwnerRoomDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteRoom(String ownerEmail, Long roomId) {
        Room room = resolveOwnedRoom(ownerEmail, roomId);
        roomRepository.delete(room);
        log.info("Owner {} deleted room id={}", ownerEmail, roomId);
    }

    @Override
    @Transactional
    public OwnerRoomDto updateStatus(String ownerEmail, Long roomId, String statusParam) {
        Room room = resolveOwnedRoom(ownerEmail, roomId);
        room.setStatus(parseRoomStatus(statusParam));
        Room saved = roomRepository.save(room);
        log.info("Owner {} set room id={} status → {}", ownerEmail, roomId, statusParam);
        return OwnerRoomDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerRoomDto toggleAvailability(String ownerEmail, Long roomId) {
        Room room = resolveOwnedRoom(ownerEmail, roomId);
        boolean next = !(Boolean.TRUE.equals(room.getIsAvailable()));
        room.setIsAvailable(next);
        Room saved = roomRepository.save(room);
        log.info("Owner {} toggled room id={} availability → {}", ownerEmail, roomId, next);
        return OwnerRoomDto.fromEntity(saved);
    }

    // ── Physical room units ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalRoomDto> listPhysicalRooms(String ownerEmail, Long roomId) {
        resolveOwnedRoom(ownerEmail, roomId);
        return physicalRoomRepository.findByRoomIdOrderByDoorNumber(roomId)
                .stream().map(PhysicalRoomDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalRoomDto> listPhysicalRoomsByProperty(String ownerEmail, Long propertyId) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        return physicalRoomRepository.findByPropertyId(propertyId)
                .stream().map(PhysicalRoomDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public PhysicalRoomDto updatePhysicalRoomStatus(String ownerEmail, Long roomId, Long unitId, String status) {
        resolveOwnedRoom(ownerEmail, roomId);
        PhysicalRoom unit = physicalRoomRepository.findByIdAndRoomId(unitId, roomId)
                .orElseThrow(() -> new CustomException("Physical room unit not found", HttpStatus.NOT_FOUND));
        unit.setStatus(status != null ? status.toUpperCase() : "CLEAN");
        PhysicalRoom saved = physicalRoomRepository.save(unit);
        log.info("Owner {} updated physical room unit id={} status→{}", ownerEmail, unitId, status);
        return PhysicalRoomDto.fromEntity(saved);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found: " + email, HttpStatus.NOT_FOUND));
    }

    private Room resolveOwnedRoom(String ownerEmail, Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException("Room not found.", HttpStatus.NOT_FOUND));
    }

    private RoomType parseRoomType(String value) {
        if (value == null) return RoomType.STANDARD_ROOM;
        String normalized = value.toUpperCase().replace(" ", "_");
        try { return RoomType.valueOf(normalized); }
        catch (IllegalArgumentException e) { return RoomType.STANDARD_ROOM; }
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
