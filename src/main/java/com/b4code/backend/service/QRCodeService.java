package com.b4code.backend.service;

import com.b4code.backend.dto.QRCodeGenerateRequest;
import com.b4code.backend.dto.QRCodeResponse;
import com.b4code.backend.models.QRCode;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QRCodeService {
    
    QRCodeResponse generateQRCode(QRCodeGenerateRequest request);


    QRCodeResponse getQRCodeById(Long id);
    
    Optional<QRCodeResponse> getQRCodeByValue(String qrCodeValue);
    
    List<QRCodeResponse> getQRCodesByProperty(Long propertyId);
    
    List<QRCodeResponse> getQRCodesByPropertyPaginated(Long propertyId, int page, int size);
    
    Optional<QRCodeResponse> getQRCodeByUniqueId(String uniqueQrId);
    
    List<QRCodeResponse> getQRCodesByPropertyAndStatus(Long propertyId, String status);
    
    QRCodeResponse updateQRCodeStatus(Long id, String status);
    
    QRCodeResponse toggleQRCodeStatus(Long id);
    
    QRCodeResponse updateQRCode(Long id, Map<String, Object> updates);
    
    QRCodeResponse markQRCodeAsScanned(Long id);
    
    void deleteQRCode(Long id);
}
