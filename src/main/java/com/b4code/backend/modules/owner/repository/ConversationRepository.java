package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    long countByOwnerIdAndUnread(Long ownerId, Boolean unread);
}
