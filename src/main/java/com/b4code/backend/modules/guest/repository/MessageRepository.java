package com.b4code.backend.modules.guest.repository;

import com.b4code.backend.modules.guest.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);
    List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);
    List<Message> findByPropertyIdOrderBySentAtDesc(Long propertyId);
}