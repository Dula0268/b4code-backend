package com.b4code.backend.modules.qr.service.impl;

import com.b4code.backend.modules.qr.dto.QRCodeResponse;
import com.b4code.backend.modules.qr.entity.QRCode;
import com.b4code.backend.modules.qr.repository.QRCodeRepository;
import com.b4code.backend.modules.qr.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRCodeServiceImpl implements QRCodeService {
    
    private final QRCodeRepository qrCodeRepository;
    
    @Override
    public QRCodeResponse generateQRCode(Long orderId, Long propertyId, String description) {
        String qrValue = UUID.randomUUID().toString();
        
        QRCode qrCode = new QRCode();
        qrCode.setQrCodeValue(qrValue);
        qrCode.setOrderId(orderId);
        qrCode.setPropertyId(propertyId);
        qrCode.setStatus("ACTIVE");
        qrCode.setDescription(description);
        qrCode.setCreatedAt(LocalDateTime.now());
        qrCode.setUpdatedAt(LocalDateTime.now());
        
        QRCode saved = qrCodeRepository.save(qrCode);
        return mapToResponse(saved);
    }
    
    @Override
    public QRCodeResponse getQRCodeById(Long id) {
        return qrCodeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));
    }
    
    @Override
    public Optional<QRCodeResponse> getQRCodeByValue(String qrCodeValue) {
        return qrCodeRepository.findByQrCodeValue(qrCodeValue)
                .map(this::mapToResponse);
    }
    
    @Override
    public List<QRCodeResponse> getQRCodesByProperty(Long propertyId) {
        return qrCodeRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QRCodeResponse> getQRCodesByOrder(Long orderId) {
        return qrCodeRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QRCodeResponse> getQRCodesByPropertyAndStatus(Long propertyId, String status) {
        return qrCodeRepository.findByPropertyIdAndStatus(propertyId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public QRCodeResponse updateQRCodeStatus(Long id, String status) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));
        
        qrCode.setStatus(status);
        qrCode.setUpdatedAt(LocalDateTime.now());
        
        QRCode updated = qrCodeRepository.save(qrCode);
        return mapToResponse(updated);
    }
    
    @Override
    public QRCodeResponse markQRCodeAsScanned(Long id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));
        
        qrCode.setScannedAt(LocalDateTime.now());
        qrCode.setStatus("SCANNED");
        qrCode.setUpdatedAt(LocalDateTime.now());
        
        QRCode updated = qrCodeRepository.save(qrCode);
        return mapToResponse(updated);
    }
    
    @Override
    public void deleteQRCode(Long id) {
        qrCodeRepository.deleteById(id);
    }
    
    @Override
    public void deleteQRCodesByOrder(Long orderId) {
        List<QRCode> qrCodes = qrCodeRepository.findByOrderId(orderId);
        qrCodeRepository.deleteAll(qrCodes);
    }
    
    private QRCodeResponse mapToResponse(QRCode qrCode) {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .qrCodeValue(qrCode.getQrCodeValue())
                .orderId(qrCode.getOrderId())
                .propertyId(qrCode.getPropertyId())
                .status(qrCode.getStatus())
                .qrImageData(qrCode.getQrImageData())
                .createdAt(qrCode.getCreatedAt())
                .updatedAt(qrCode.getUpdatedAt())
                .scannedAt(qrCode.getScannedAt())
                .description(qrCode.getDescription())
                .build();
    }
}
