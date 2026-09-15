package com.b4code.backend.dao;

import com.b4code.backend.models.messaging.InternalMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalMessageRepository extends JpaRepository<InternalMessage, Long> {
    List<InternalMessage> findByPropertyIdOrderByCreatedAtAsc(Long propertyId);
}
