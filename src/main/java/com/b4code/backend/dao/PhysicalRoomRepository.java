package com.b4code.backend.dao;

import com.b4code.backend.models.PhysicalRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalRoomRepository extends JpaRepository<PhysicalRoom, Long> {
    
    @Query("SELECT pr FROM PhysicalRoom pr JOIN FETCH pr.roomType WHERE pr.roomType.property.id = :propertyId")
    List<PhysicalRoom> findByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT pr FROM PhysicalRoom pr WHERE pr.roomType.id = :roomTypeId AND pr.status = :status")
    List<PhysicalRoom> findByRoomTypeIdAndStatus(@Param("roomTypeId") Long roomTypeId, @Param("status") String status);

    /**
     * "Ready to assign" rooms for a room type. PhysicalRoom.status is a free-text
     * column with no enforced vocabulary — most existing data uses "CLEAN" (a
     * housekeeping convention) rather than the literal "AVAILABLE", so this
     * matches anything NOT explicitly blocked (occupied or under maintenance)
     * instead of requiring one exact status string.
     */
    @Query("SELECT pr FROM PhysicalRoom pr WHERE pr.roomType.id = :roomTypeId " +
           "AND UPPER(pr.status) NOT IN ('OCCUPIED', 'MAINTENANCE', 'DIRTY', 'OUT_OF_SERVICE')")
    List<PhysicalRoom> findAssignableByRoomTypeId(@Param("roomTypeId") Long roomTypeId);
}
