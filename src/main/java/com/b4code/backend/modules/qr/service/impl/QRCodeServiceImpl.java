package com.b4code.backend.modules.qr.service.impl;

import com.b4code.backend.modules.qr.dto.QRCodeGenerateRequest;
import com.b4code.backend.modules.qr.dto.QRCodeResponse;
import com.b4code.backend.models.QRCode;
import com.b4code.backend.modules.qr.repository.QRCodeRepository;
import com.b4code.backend.modules.qr.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${app.qr.base-url:http://localhost:3000}")
    private String baseUrl;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String STATUS_SCANNED = "SCANNED";

    @Override
    @Transactional
    public QRCodeResponse generateQRCode(QRCodeGenerateRequest request) {
        QRCode qrCode = new QRCode();
        qrCode.setUniqueQrId(UUID.randomUUID().toString());
        qrCode.setOrderId(request.getOrderId());
        qrCode.setPropertyId(request.getPropertyId());
        qrCode.setStatus(STATUS_ACTIVE);
        qrCode.setName(request.getName());
        qrCode.setLocation(request.getLocation());
        qrCode.setType(request.getType());
        qrCode.setDescription(request.getDescription());
        qrCode.setInstructionText(request.getInstructionText());
        qrCode.setShowRoomNumber(Boolean.TRUE.equals(request.getShowRoomNumber()));
        qrCode.setShowLogo(request.getShowLogo() == null || request.getShowLogo());
        qrCode.setTableId(request.getTableId());
        qrCode.setRoomNumber(request.getRoomNumber());
        qrCode.setCreatedAt(LocalDateTime.now());
        qrCode.setUpdatedAt(LocalDateTime.now());

        // Construct target URL
        String targetUrl = String.format("%s/guest/order/menu?propertyId=%d", baseUrl, qrCode.getPropertyId());
        if (qrCode.getTableId() != null) {
            targetUrl += "&tableId=" + qrCode.getTableId();
        } else if (qrCode.getRoomNumber() != null && !qrCode.getRoomNumber().isEmpty()) {
            targetUrl += "&roomNumber=" + qrCode.getRoomNumber();
        }

        qrCode.setQrCodeValue(targetUrl);
        qrCode.setQrImageData(generateQRImageBase64(targetUrl));

        QRCode saved = qrCodeRepository.save(qrCode);
        return mapToResponse(saved);
    }

    @Override
    public QRCodeResponse generateQRCode(Long orderId, Long propertyId, String description) {
        // FIXED: Using Builder to avoid the 11-argument constructor mismatch error
        QRCodeGenerateRequest request = QRCodeGenerateRequest.builder()
                .orderId(orderId)
                .propertyId(propertyId)
                .description(description)
                .build();
        
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
        if (start >= allQRs.size()) return List.of();
        
        int end = Math.min(start + size, allQRs.size());
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
    @Transactional
    public QRCodeResponse updateQRCodeStatus(Long id, String status) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));

        qrCode.setStatus(status);
        qrCode.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(qrCodeRepository.save(qrCode));
    }

    @Override
    @Transactional
    public QRCodeResponse toggleQRCodeStatus(Long id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));

        String newStatus = STATUS_ACTIVE.equals(qrCode.getStatus()) ? STATUS_INACTIVE : STATUS_ACTIVE;
        qrCode.setStatus(newStatus);
        qrCode.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(qrCodeRepository.save(qrCode));
    }

    @Override
    @Transactional
    public QRCodeResponse updateQRCode(Long id, Map<String, Object> updates) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));

        if (updates.containsKey("description")) qrCode.setDescription((String) updates.get("description"));
        if (updates.containsKey("name")) qrCode.setName((String) updates.get("name"));
        if (updates.containsKey("location")) qrCode.setLocation((String) updates.get("location"));
        if (updates.containsKey("type")) qrCode.setType((String) updates.get("type"));
        if (updates.containsKey("status")) qrCode.setStatus((String) updates.get("status"));
        if (updates.containsKey("qrImageData")) qrCode.setQrImageData((String) updates.get("qrImageData"));
        if (updates.containsKey("propertyId")) qrCode.setPropertyId(toLong(updates.get("propertyId")));
        if (updates.containsKey("orderId")) qrCode.setOrderId(toLong(updates.get("orderId")));
        if (updates.containsKey("instructionText")) qrCode.setInstructionText((String) updates.get("instructionText"));
        if (updates.containsKey("showRoomNumber")) qrCode.setShowRoomNumber(toBoolean(updates.get("showRoomNumber")));
        if (updates.containsKey("showLogo")) qrCode.setShowLogo(toBoolean(updates.get("showLogo")));

        qrCode.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(qrCodeRepository.save(qrCode));
    }

    @Override
    @Transactional
    public QRCodeResponse markQRCodeAsScanned(Long id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found with id: " + id));

        qrCode.setScannedAt(LocalDateTime.now());
        qrCode.setStatus(STATUS_SCANNED);
        qrCode.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(qrCodeRepository.save(qrCode));
    }

    @Override
    @Transactional
    public void deleteQRCode(Long id) {
        qrCodeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteQRCodesByOrder(Long orderId) {
        List<QRCode> qrCodes = qrCodeRepository.findByOrderId(orderId);
        qrCodeRepository.deleteAll(qrCodes);
    }

    private QRCodeResponse mapToResponse(QRCode qrCode) {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .qrCodeValue(qrCode.getQrCodeValue())
                .uniqueQrId(qrCode.getUniqueQrId())
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
                .tableId(qrCode.getTableId())
                .roomNumber(qrCode.getRoomNumber())
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool;
        return Boolean.valueOf(value.toString());
    }

    private String generateQRImageBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }
}
