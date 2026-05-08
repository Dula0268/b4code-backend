package com.hospitality.service;

import com.hospitality.dto.property.QRCodeGenerateRequest;
import com.hospitality.dto.property.QRCodeResponse;
import com.hospitality.models.QRCode;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QRCodeService {
    
    QRCodeResponse generateQRCode(QRCodeGenerateRequest request);

    QRCodeResponse generateQRCode(Long orderId, Long propertyId, String description);
    
    QRCodeResponse getQRCodeById(Long id);
    
    Optional<QRCodeResponse> getQRCodeByValue(String qrCodeValue);
    
    List<QRCodeResponse> getQRCodesByProperty(Long propertyId);
    
    List<QRCodeResponse> getQRCodesByPropertyPaginated(Long propertyId, int page, int size);
    
    List<QRCodeResponse> getQRCodesByOrder(Long orderId);
    
    List<QRCodeResponse> getQRCodesByPropertyAndStatus(Long propertyId, String status);
    
    QRCodeResponse updateQRCodeStatus(Long id, String status);
    
    QRCodeResponse toggleQRCodeStatus(Long id);
    
    QRCodeResponse updateQRCode(Long id, Map<String, Object> updates);
    
    QRCodeResponse markQRCodeAsScanned(Long id);
    
    void deleteQRCode(Long id);
    
    void deleteQRCodesByOrder(Long orderId);
}
