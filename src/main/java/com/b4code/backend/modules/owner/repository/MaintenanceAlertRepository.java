package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.MaintenanceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceAlertRepository extends JpaRepository<MaintenanceAlert, Long> {

    long countByPropertyIdInAndStatusInAndPriority(List<Long> propertyIds, List<String> statuses, String priority);

    long countByPropertyIdInAndStatusIn(List<Long> propertyIds, List<String> statuses);
}
