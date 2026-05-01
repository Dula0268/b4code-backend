package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    List<StaffMember> findByPropertyIdOrderByLastNameAsc(Long propertyId);

    List<StaffMember> findByPropertyIdAndRole(Long propertyId, String role);

    List<StaffMember> findByPropertyIdAndStatus(Long propertyId, String status);
}
