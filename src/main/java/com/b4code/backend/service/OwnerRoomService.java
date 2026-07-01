package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerRoomDto;
import com.b4code.backend.dto.owner.OwnerRoomListDto;
import com.b4code.backend.dto.owner.OwnerRoomRequest;
import com.b4code.backend.dto.owner.PhysicalRoomDto;

import java.util.List;

public interface OwnerRoomService {
    OwnerRoomListDto listRooms(String ownerEmail, String status, String search, int page, int size);
    OwnerRoomDto getRoom(String ownerEmail, Long roomId);
    OwnerRoomDto createRoom(String ownerEmail, OwnerRoomRequest request);
    OwnerRoomDto updateRoom(String ownerEmail, Long roomId, OwnerRoomRequest request);
    void deleteRoom(String ownerEmail, Long roomId);
    OwnerRoomDto updateStatus(String ownerEmail, Long roomId, String status);
    OwnerRoomDto toggleAvailability(String ownerEmail, Long roomId);

    // Physical room units (individual room units with door numbers)
    List<PhysicalRoomDto> listPhysicalRooms(String ownerEmail, Long roomId);
    List<PhysicalRoomDto> listPhysicalRoomsByProperty(String ownerEmail, Long propertyId);
    PhysicalRoomDto updatePhysicalRoomStatus(String ownerEmail, Long roomId, Long unitId, String status);
}
