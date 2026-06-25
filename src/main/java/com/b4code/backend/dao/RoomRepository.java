package com.b4code.backend.dao;

import com.b4code.backend.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByPropertyId(Long propertyId);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r JOIN FETCH r.property LEFT JOIN FETCH r.image")
    List<Room> findAllWithRelations();
}
