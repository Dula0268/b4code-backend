package com.b4code.backend.modules.qr.service;

import com.b4code.backend.modules.qr.dto.QRCodeResponse;
import com.b4code.backend.modules.qr.entity.QRCode;
import java.util.List;
import java.util.Optional;

public interface QRCodeService {
    
    QRCodeResponse generateQRCode(Long orderId, Long propertyId, String description);
    
    QRCodeResponse getQRCodeById(Long id);
    
    Optional<QRCodeResponse> getQRCodeByValue(String qrCodeValue);
    
    List<QRCodeResponse> getQRCodesByProperty(Long propertyId);
    
    List<QRCodeResponse> getQRCodesByOrder(Long orderId);
    
    List<QRCodeResponse> getQRCodesByPropertyAndStatus(Long propertyId, String status);
    
    QRCodeResponse updateQRCodeStatus(Long id, String status);
    
    QRCodeResponse markQRCodeAsScanned(Long id);
    
    void deleteQRCode(Long id);
    
    void deleteQRCodesByOrder(Long orderId);
}
