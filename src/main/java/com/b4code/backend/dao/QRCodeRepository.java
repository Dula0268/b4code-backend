package com.b4code.backend.dao;

import com.b4code.backend.models.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    
    Optional<QRCode> findByQrCodeValue(String qrCodeValue);
    
    Optional<QRCode> findByUniqueQrId(String uniqueQrId);
    
    List<QRCode> findByPropertyId(Long propertyId);
    
    List<QRCode> findByPropertyIdAndStatus(Long propertyId, String status);
}
