package com.b4code.backend.dao;

import com.b4code.backend.models.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCodeIgnoreCase(String code);

    List<PromoCode> findByPropertyIdIn(List<Long> propertyIds);

    Optional<PromoCode> findByIdAndPropertyIdIn(Long id, List<Long> propertyIds);
}
