package com.b4code.backend.dao;

import com.b4code.backend.models.PhysicalRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalRoomRepository extends JpaRepository<PhysicalRoom, Long> {

    List<PhysicalRoom> findByRoomIdOrderByDoorNumber(Long roomId);

    @Query("SELECT pr FROM PhysicalRoom pr WHERE pr.room.property.id = :propertyId ORDER BY pr.room.id, pr.doorNumber")
    List<PhysicalRoom> findByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT pr FROM PhysicalRoom pr WHERE pr.room.property.ownerId = :ownerId ORDER BY pr.room.id, pr.doorNumber")
    List<PhysicalRoom> findByOwnerIdOrderByRoom(@Param("ownerId") Long ownerId);

    Optional<PhysicalRoom> findByIdAndRoomId(Long id, Long roomId);

    long countByRoomId(Long roomId);

    @Query("SELECT COUNT(pr) FROM PhysicalRoom pr WHERE pr.room.id = :roomId AND pr.status = :status")
    long countByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") String status);
}
