package com.b4code.backend.repository;

import com.b4code.backend.models.messaging.StaffQuickReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffQuickReplyRepository extends JpaRepository<StaffQuickReply, Long> {
    List<StaffQuickReply> findByPropertyId(Long propertyId);
    List<StaffQuickReply> findByPropertyIdAndIsActiveTrue(Long propertyId);
}
