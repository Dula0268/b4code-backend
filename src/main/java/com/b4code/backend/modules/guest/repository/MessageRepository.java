package com.b4code.backend.modules.guest.repository;

import com.b4code.backend.modules.guest.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByPropertyId(Long propertyId);
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Message> findByReceiverId(Long receiverId);
    List<Message> findBySenderId(Long senderId);
}
