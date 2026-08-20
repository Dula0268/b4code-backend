package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerRoomTypeDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeListDto;
import com.b4code.backend.dto.owner.OwnerRoomTypeRequest;

public interface OwnerRoomTypeService {
    OwnerRoomTypeListDto listRoomTypes(String ownerEmail, String status, String search);
    OwnerRoomTypeDto getRoomType(String ownerEmail, Long roomId);
    OwnerRoomTypeDto createRoom(String ownerEmail, OwnerRoomTypeRequest request);
    OwnerRoomTypeDto updateRoom(String ownerEmail, Long roomId, OwnerRoomTypeRequest request);
    void deleteRoom(String ownerEmail, Long roomId);
    OwnerRoomTypeDto updateStatus(String ownerEmail, Long roomId, String status);
    OwnerRoomTypeDto toggleAvailability(String ownerEmail, Long roomId);
}

