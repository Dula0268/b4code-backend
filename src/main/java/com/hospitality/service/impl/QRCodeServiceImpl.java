package com.hospitality.service.impl;

import com.hospitality.dto.property.QRCodeGenerateRequest;
import com.hospitality.dto.property.QRCodeResponse;
import com.hospitality.models.QRCode;
import com.hospitality.dao.QRCodeRepository;
import com.hospitality.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRCodeServiceImpl implements QRCodeService {
    
    private final QRCodeRepository qrCodeRepository;
    
    @Override
    public QRCodeResponse generateQRCode(QRCodeGenerateRequest request) {
        QRCode qrCode = new QRCode();
        qrCode.setUniqueQrId(UUID.randomUUID().toString());
        qrCode.setQrCodeValue(UUID.randomUUID().toString());
        qrCode.setOrderId(request.getOrderId());
        qrCode.setPropertyId(request.getPropertyId());
        qrCode.setStatus("ACTIVE");
        qrCode.setName(request.getName());
        qrCode.setLocation(request.getLocation());
        qrCode.setType(request.getType());
        qrCode.setDescription(request.getDescription());
        qrCode.setInstructionText(request.getInstructionText());
        qrCode.setShowRoomNumber(Boolean.TRUE.equals(request.getShowRoomNumber()));
        qrCode.setShowLogo(request.getShowLogo() == null || request.getShowLogo());
        qrCode.setCreatedAt(LocalDateTime.now());
        qrCode.setUpdatedAt(LocalDateTime.now());
        qrCode.setQrImageData(generateQRImageBase64(qrCode.getQrCodeValue()));

        QRCode saved = qrCodeRepository.save(qrCode);
        return mapToResponse(saved);
    }

    @Override
    public QRCodeResponse generateQRCode(Long orderId, Long propertyId, String description) {
        QRCodeGenerateRequest request = new QRCodeGenerateRequest(
                orderId,
                propertyId,
                null,
                null,
                null,
                description,
                null,
                null,
                null);
        return generateQRCode(request);
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
    public List<QRCodeResponse> getQRCodesByPropertyPaginated(Long propertyId, int page, int size) {
        List<QRCodeResponse> allQRs = getQRCodesByProperty(propertyId);
        int start = page * size;
        int end = Math.min(start + size, allQRs.size());
        
        if (start >= allQRs.size()) {
            return List.of();
        }
        
        return allQRs.subList(start, end);
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
    public QRCodeResponse toggleQRCodeStatus(Long id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));
        
        String newStatus = "ACTIVE".equals(qrCode.getStatus()) ? "INACTIVE" : "ACTIVE";
        qrCode.setStatus(newStatus);
        qrCode.setUpdatedAt(LocalDateTime.now());
        
        QRCode updated = qrCodeRepository.save(qrCode);
        return mapToResponse(updated);
    }
    
    @Override
    public QRCodeResponse updateQRCode(Long id, Map<String, Object> updates) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));
        
        if (updates.containsKey("description")) {
            qrCode.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("name")) {
            qrCode.setName((String) updates.get("name"));
        }
        if (updates.containsKey("location")) {
            qrCode.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("type")) {
            qrCode.setType((String) updates.get("type"));
        }
        if (updates.containsKey("status")) {
            qrCode.setStatus((String) updates.get("status"));
        }
        if (updates.containsKey("qrImageData")) {
            qrCode.setQrImageData((String) updates.get("qrImageData"));
        }
        if (updates.containsKey("propertyId")) {
            qrCode.setPropertyId(toLong(updates.get("propertyId")));
        }
        if (updates.containsKey("orderId")) {
            qrCode.setOrderId(toLong(updates.get("orderId")));
        }
        if (updates.containsKey("instructionText")) {
            qrCode.setInstructionText((String) updates.get("instructionText"));
        }
        if (updates.containsKey("showRoomNumber")) {
            qrCode.setShowRoomNumber(toBoolean(updates.get("showRoomNumber")));
        }
        if (updates.containsKey("showLogo")) {
            qrCode.setShowLogo(toBoolean(updates.get("showLogo")));
        }
        
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
                .uniqueQrId(qrCode.getQrCodeValue())
                .orderId(qrCode.getOrderId())
                .propertyId(qrCode.getPropertyId())
                .status(qrCode.getStatus())
                .name(qrCode.getName())
                .location(qrCode.getLocation())
                .type(qrCode.getType())
                .qrImageData(qrCode.getQrImageData())
                .qrImageUrl(qrCode.getQrImageData())
                .createdAt(qrCode.getCreatedAt())
                .updatedAt(qrCode.getUpdatedAt())
                .scannedAt(qrCode.getScannedAt())
                .description(qrCode.getDescription())
                .instructionText(qrCode.getInstructionText())
                .showRoomNumber(qrCode.getShowRoomNumber())
                .showLogo(qrCode.getShowLogo())
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(value.toString());
    }

    private String generateQRImageBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }
}
