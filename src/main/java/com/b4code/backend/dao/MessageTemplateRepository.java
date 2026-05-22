package com.b4code.backend.dao;

import com.b4code.backend.models.MessageTemplate;
import com.b4code.backend.models.MessageTemplate.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    List<MessageTemplate> findByPropertyId(Long propertyId);

    Optional<MessageTemplate> findByPropertyIdAndTemplateType(Long propertyId, TemplateType templateType);

    List<MessageTemplate> findByPropertyIdAndEnabledTrue(Long propertyId);
}
