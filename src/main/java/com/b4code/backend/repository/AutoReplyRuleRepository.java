package com.b4code.backend.repository;

import com.b4code.backend.models.messaging.AutoReplyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoReplyRuleRepository extends JpaRepository<AutoReplyRule, Long> {
    List<AutoReplyRule> findByPropertyId(Long propertyId);
    List<AutoReplyRule> findByPropertyIdAndIsActiveTrue(Long propertyId);
}
