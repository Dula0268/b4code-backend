package com.b4code.backend.modules.staff.qr.repository;

import com.b4code.backend.modules.staff.qr.entity.QRCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    
    Optional<QRCode> findByUniqueQrId(String uniqueQrId);
    
    Page<QRCode> findByPropertyId(Long propertyId, Pageable pageable);
    
    Page<QRCode> findByPropertyIdAndStatus(Long propertyId, QRCode.QRStatus status, Pageable pageable);
    
    List<QRCode> findByPropertyIdAndStatus(Long propertyId, QRCode.QRStatus status);
    
    Optional<QRCode> findByPropertyIdAndId(Long propertyId, Long id);
    
    long countByPropertyId(Long propertyId);
}
