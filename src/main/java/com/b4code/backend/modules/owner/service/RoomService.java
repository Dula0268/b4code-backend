package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.RoomDto.*;
import com.b4code.backend.modules.owner.entity.Room;
import com.b4code.backend.modules.owner.repository.OwnerPropertyRepository;
import com.b4code.backend.modules.owner.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final OwnerPropertyRepository propertyRepository;

    public RoomKpiResponse getRoomOverview(Long ownerId, String statusFilter, String search) {
        List<Long> propertyIds = propertyRepository.findByOwnerId(ownerId).stream()
                .map(p -> p.getId()).collect(Collectors.toList());

        List<Room> allRooms = propertyIds.stream()
                .flatMap(pid -> roomRepository.findByPropertyId(pid).stream())
                .collect(Collectors.toList());

        int total = allRooms.size();
        int occupied = (int) allRooms.stream().filter(r -> "OCCUPIED".equalsIgnoreCase(r.getStatus())).count();
        int maintenance = (int) allRooms.stream().filter(r -> "MAINTENANCE".equalsIgnoreCase(r.getStatus())).count();
        int vacant = (int) allRooms.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();

        List<Room> filtered = allRooms.stream()
                .filter(r -> statusFilter == null || "All Rooms".equals(statusFilter) || r.getStatus().equalsIgnoreCase(statusFilter))
                .filter(r -> search == null || search.isEmpty()
                        || r.getName().toLowerCase().contains(search.toLowerCase())
                        || r.getRoomType().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        List<RoomResponse> roomResponses = filtered.stream().map(this::toRoomResponse).collect(Collectors.toList());

        RoomKpiResponse response = new RoomKpiResponse();
        response.setTotalRooms(total);
        response.setOccupied(occupied);
        response.setMaintenance(maintenance);
        response.setVacant(vacant);
        response.setRooms(roomResponses);
        response.setTotalItems(filtered.size());
        return response;
    }

    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return toRoomResponse(room);
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setRoomType(request.getRoomType());
        room.setPropertyId(request.getPropertyId());
        room.setMaxAdults(request.getMaxAdults());
        room.setMaxChildren(request.getMaxChildren());
        room.setMaxOccupancy((request.getMaxAdults() != null ? request.getMaxAdults() : 0) + (request.getMaxChildren() != null ? request.getMaxChildren() : 0));
        room.setBaseRate(request.getNightlyRate());
        room.setCurrency(request.getCurrency());
        room.setDescription(request.getDescription());
        room.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");

        Room saved = roomRepository.save(room);
        log.info("Room created: id={}, name={}", saved.getId(), saved.getName());
        return toRoomResponse(saved);
    }

    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (request.getName() != null) room.setName(request.getName());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getMaxAdults() != null) room.setMaxAdults(request.getMaxAdults());
        if (request.getMaxChildren() != null) room.setMaxChildren(request.getMaxChildren());
        if (request.getNightlyRate() != null) room.setBaseRate(request.getNightlyRate());
        if (request.getCurrency() != null) room.setCurrency(request.getCurrency());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getStatus() != null) room.setStatus(request.getStatus());

        roomRepository.save(room);
        return toRoomResponse(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
        log.info("Room deleted: id={}", roomId);
    }

    @Transactional
    public RoomResponse updateRoomStatus(Long roomId, String status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        room.setStatus(status);
        roomRepository.save(room);
        return toRoomResponse(room);
    }

    private RoomResponse toRoomResponse(Room r) {
        RoomResponse resp = new RoomResponse();
        resp.setId(r.getId());
        resp.setName(r.getName());
        resp.setRoomType(r.getRoomType());
        resp.setPropertyId(r.getPropertyId());
        resp.setStatus(r.getStatus());
        resp.setBaseRate(r.getBaseRate());
        resp.setCurrency(r.getCurrency());
        resp.setMaxOccupancy(r.getMaxOccupancy());
        resp.setMaxAdults(r.getMaxAdults());
        resp.setMaxChildren(r.getMaxChildren());
        resp.setDescription(r.getDescription());
        return resp;
    }
}
