package com.b4code.backend.rest;

import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByProperty(@PathVariable Long propertyId) {
        List<Room> rooms = roomRepository.findByPropertyId(propertyId);
        List<RoomDTO> dtos = rooms.stream().map(r -> new RoomDTO(r.getId(), r.getRoomType().name())).collect(Collectors.toList());
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
}
