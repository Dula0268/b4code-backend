package com.b4code.backend.modules.qr.repository;

import com.b4code.backend.models.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    
    Optional<QRCode> findByQrCodeValue(String qrCodeValue);
    
    List<QRCode> findByPropertyId(Long propertyId);
    
    List<QRCode> findByOrderId(Long orderId);
    
    List<QRCode> findByPropertyIdAndStatus(Long propertyId, String status);
    
    Optional<QRCode> findByOrderIdAndPropertyId(Long orderId, Long propertyId);
}
