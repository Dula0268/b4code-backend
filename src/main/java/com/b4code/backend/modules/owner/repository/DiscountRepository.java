package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    List<Discount> findByPropertyIdAndActive(Long propertyId, Boolean active);
}
