package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ownerRoomRepository")
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByPropertyId(Long propertyId);

    long countByPropertyIdIn(List<Long> propertyIds);

    long countByPropertyIdInAndStatus(List<Long> propertyIds, String status);

    int countByPropertyId(Long propertyId);
}
