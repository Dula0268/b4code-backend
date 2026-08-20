package com.b4code.backend.dao;

import com.b4code.backend.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    Optional<Discount> findByIdAndPropertyId(Long id, Long propertyId);
}
