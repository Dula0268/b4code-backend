package com.b4code.backend.rest;

import com.b4code.backend.dao.PhysicalRoomRepository;
import com.b4code.backend.models.PhysicalRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomTypeController {

    @Autowired
    private PhysicalRoomRepository physicalRoomRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<RoomDTO>> getRoomTypesByProperty(@PathVariable Long propertyId) {
        List<PhysicalRoom> physicalRooms = physicalRoomRepository.findByPropertyId(propertyId);
        List<RoomDTO> dtos = physicalRooms.stream()
                .map(r -> new RoomDTO(r.getId(), r.getRoomType().getName() + " " + r.getDoorNumber()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/room-type/{roomTypeId}/available")
    public ResponseEntity<List<AvailableRoomDTO>> getAvailableRoomsForRoomType(@PathVariable Long roomTypeId) {
        List<PhysicalRoom> physicalRooms = physicalRoomRepository.findAssignableByRoomTypeId(roomTypeId);
        List<AvailableRoomDTO> dtos = physicalRooms.stream()
                .map(r -> new AvailableRoomDTO(r.getId(), r.getDoorNumber()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public static class RoomDTO {
        public Long id;
        public String roomType;

        public RoomDTO(Long id, String roomType) {
            this.id = id;
            this.roomType = roomType;
        }
    }

    public static class AvailableRoomDTO {
        public Long id;
        public String doorNumber;

        public AvailableRoomDTO(Long id, String doorNumber) {
            this.id = id;
            this.doorNumber = doorNumber;
        }
    }
}
