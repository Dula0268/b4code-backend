package com.b4code.backend.modules.guest.dao;

import com.b4code.backend.modules.guest.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}