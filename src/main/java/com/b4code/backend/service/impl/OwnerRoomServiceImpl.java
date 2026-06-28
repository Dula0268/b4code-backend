package com.b4code.backend.service.impl;

import com.b4code.backend.dao.ImageRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerRoomDto;
import com.b4code.backend.dto.owner.OwnerRoomListDto;
import com.b4code.backend.dto.owner.OwnerRoomRequest;
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

    @Override
    @Transactional(readOnly = true)
    public OwnerRoomListDto listRooms(String ownerEmail, String statusParam, String search) {
        User owner = resolveOwner(ownerEmail);

        RoomStatus statusFilter = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try { statusFilter = RoomStatus.valueOf(statusParam.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        List<Room> rooms = roomRepository.findByOwnerWithFilters(owner.getId(), statusFilter, searchTerm);

        long total = roomRepository.countByOwner(owner.getId());
        long occupied = roomRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.OCCUPIED);
        long maintenance = roomRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.MAINTENANCE);
        long vacant = roomRepository.countByOwnerAndStatus(owner.getId(), RoomStatus.AVAILABLE);

        List<OwnerRoomDto> dtos = rooms.stream().map(OwnerRoomDto::fromEntity).toList();

        return OwnerRoomListDto.builder()
                .rooms(dtos)
                .totalRooms(total)
                .occupied(occupied)
                .maintenance(maintenance)
                .vacant(vacant)
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
                    .property(property)
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

    // ── helpers ─────────────────────────────────────────────────────────────

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found: " + email, HttpStatus.NOT_FOUND));
    }

    private Room resolveOwnedRoom(String ownerEmail, Long roomId) {
        User owner = resolveOwner(ownerEmail);
        return roomRepository.findByIdAndPropertyOwnerId(roomId, owner.getId())
                .orElseThrow(() -> new CustomException(
                        "Room not found or does not belong to this owner.", HttpStatus.NOT_FOUND));
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
