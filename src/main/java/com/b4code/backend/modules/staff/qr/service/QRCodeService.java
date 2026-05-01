package com.b4code.backend.modules.staff.qr.service;

import com.b4code.backend.modules.staff.qr.dto.CreateQRRequest;
import com.b4code.backend.modules.staff.qr.dto.QRResponse;
import com.b4code.backend.modules.staff.qr.dto.QRValidationResponse;
import com.b4code.backend.modules.staff.qr.dto.UpdateQRRequest;
import com.b4code.backend.modules.staff.qr.entity.QRCode;
import com.b4code.backend.modules.staff.qr.repository.QRCodeRepository;
import com.b4code.backend.modules.staff.qr.util.QRImageGenerator;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.owner.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final PropertyRepository propertyRepository;
    private final QRImageGenerator qrImageGenerator;

    /**
     * Generate a new QR code
     */
    @Transactional
    public QRResponse generateQRCode(CreateQRRequest request) {
        log.info("Generating QR code for property: {}", request.getPropertyId());

        // Verify property exists
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + request.getPropertyId()));

        // Generate unique QR ID
        String uniqueQrId = UUID.randomUUID().toString();

        // Create QRCode entity
        QRCode qrCode = new QRCode();
        qrCode.setUniqueQrId(uniqueQrId);
        qrCode.setName(request.getName());
        qrCode.setLocation(request.getLocation());
        qrCode.setType(QRCode.QRType.valueOf(request.getType().toUpperCase()));
        qrCode.setStatus(QRCode.QRStatus.ACTIVE);
        qrCode.setDescription(request.getDescription());
        qrCode.setInstructionText(request.getInstructionText());
        qrCode.setShowRoomNumber(request.getShowRoomNumber() != null ? request.getShowRoomNumber() : false);
        qrCode.setShowLogo(request.getShowLogo() != null ? request.getShowLogo() : true);
        qrCode.setPropertyId(request.getPropertyId());
        qrCode.setScans(0);

        // Generate QR image
        byte[] qrImage = qrImageGenerator.generateQRImage(uniqueQrId);
        qrCode.setQrImage(qrImage);

        // Save to database
        QRCode savedQRCode = qrCodeRepository.save(qrCode);
        log.info("QR code generated successfully with ID: {}", savedQRCode.getId());

        return convertToDTO(savedQRCode);
    }

    /**
     * Validate a QR code and return location context
     */
    @Transactional
    public QRValidationResponse validateQRCode(String uniqueQrId) {
        log.info("Validating QR code: {}", uniqueQrId);

        QRCode qrCode = qrCodeRepository.findByUniqueQrId(uniqueQrId)
                .orElseThrow(() -> new RuntimeException("QR code not found: " + uniqueQrId));

        // Check if QR code is active
        if (qrCode.getStatus() != QRCode.QRStatus.ACTIVE) {
            throw new RuntimeException("QR code is not active");
        }

        // Check if QR code has expired
        if (qrCode.getExpiresAt() != null && LocalDateTime.now().isAfter(qrCode.getExpiresAt())) {
            throw new RuntimeException("QR code has expired");
        }

        // Get property details
        Property property = propertyRepository.findById(qrCode.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Increment scan count
        qrCode.setScans(qrCode.getScans() + 1);
        qrCode.setLastScannedAt(LocalDateTime.now());
        qrCodeRepository.save(qrCode);

        // Build validation response
        QRValidationResponse response = new QRValidationResponse();
        response.setQrId(qrCode.getUniqueQrId());
        response.setPropertyName(property.getName());
        response.setLocationLabel(qrCode.getName()); // Table name or Room number
        response.setType(qrCode.getType().name());
        response.setName(qrCode.getName());
        response.setStatus(qrCode.getStatus().name());
        response.setIsValid(true);

        log.info("QR code validated successfully: {}", uniqueQrId);
        return response;
    }

    /**
     * Get QR code by ID
     */
    public QRResponse getQRCodeById(Long id) {
        log.info("Fetching QR code with ID: {}", id);
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with ID: " + id));
        return convertToDTO(qrCode);
    }

    /**
     * Get QR codes by property with pagination
     */
    public Page<QRResponse> getQRCodesByProperty(Long propertyId, Pageable pageable) {
        log.info("Fetching QR codes for property: {}", propertyId);
        Page<QRCode> qrCodes = qrCodeRepository.findByPropertyId(propertyId, pageable);
        return new PageImpl<>(
                qrCodes.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                qrCodes.getTotalElements()
        );
    }

    /**
     * Get active QR codes by property with pagination
     */
    public Page<QRResponse> getActiveQRCodesByProperty(Long propertyId, Pageable pageable) {
        log.info("Fetching active QR codes for property: {}", propertyId);
        Page<QRCode> qrCodes = qrCodeRepository.findByPropertyIdAndStatus(propertyId, QRCode.QRStatus.ACTIVE, pageable);
        return new PageImpl<>(
                qrCodes.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                qrCodes.getTotalElements()
        );
    }

    /**
     * Update QR code
     */
    @Transactional
    public QRResponse updateQRCode(Long id, UpdateQRRequest request) {
        log.info("Updating QR code with ID: {}", id);
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with ID: " + id));

        if (request.getName() != null) {
            qrCode.setName(request.getName());
        }
        if (request.getLocation() != null) {
            qrCode.setLocation(request.getLocation());
        }
        if (request.getType() != null) {
            qrCode.setType(QRCode.QRType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getDescription() != null) {
            qrCode.setDescription(request.getDescription());
        }
        if (request.getInstructionText() != null) {
            qrCode.setInstructionText(request.getInstructionText());
        }
        if (request.getShowRoomNumber() != null) {
            qrCode.setShowRoomNumber(request.getShowRoomNumber());
        }
        if (request.getShowLogo() != null) {
            qrCode.setShowLogo(request.getShowLogo());
        }
        if (request.getStatus() != null) {
            qrCode.setStatus(QRCode.QRStatus.valueOf(request.getStatus().toUpperCase()));
        }

        QRCode updatedQRCode = qrCodeRepository.save(qrCode);
        log.info("QR code updated successfully: {}", id);
        return convertToDTO(updatedQRCode);
    }

    /**
     * Delete QR code (soft delete)
     */
    @Transactional
    public void deleteQRCode(Long id) {
        log.info("Deleting QR code with ID: {}", id);
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with ID: " + id));
        qrCode.setStatus(QRCode.QRStatus.DELETED);
        qrCodeRepository.save(qrCode);
        log.info("QR code deleted (soft delete): {}", id);
    }

    /**
     * Toggle QR code status (ACTIVE <-> INACTIVE)
     */
    @Transactional
    public QRResponse toggleQRStatus(Long id) {
        log.info("Toggling QR code status: {}", id);
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with ID: " + id));

        if (qrCode.getStatus() == QRCode.QRStatus.ACTIVE) {
            qrCode.setStatus(QRCode.QRStatus.INACTIVE);
        } else if (qrCode.getStatus() == QRCode.QRStatus.INACTIVE) {
            qrCode.setStatus(QRCode.QRStatus.ACTIVE);
        }

        QRCode updatedQRCode = qrCodeRepository.save(qrCode);
        log.info("QR code status toggled: {}", id);
        return convertToDTO(updatedQRCode);
    }

    /**
     * Bulk generate QR codes
     */
    @Transactional
    public List<QRResponse> bulkGenerateQRCodes(Long propertyId, List<CreateQRRequest> requests) {
        log.info("Bulk generating {} QR codes for property: {}", requests.size(), propertyId);

        if (requests.size() > 100) {
            throw new RuntimeException("Cannot generate more than 100 QR codes at once");
        }

        return requests.stream()
                .map(request -> {
                    request.setPropertyId(propertyId);
                    return generateQRCode(request);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get QR code image as byte array
     */
    public byte[] getQRImage(Long id) {
        log.info("Fetching QR image for ID: {}", id);
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with ID: " + id));
        
        if (qrCode.getQrImage() == null || qrCode.getQrImage().length == 0) {
            throw new RuntimeException("QR image not found for ID: " + id);
        }
        
        return qrCode.getQrImage();
    }

    /**
     * Convert QRCode entity to QRResponse DTO
     */
    private QRResponse convertToDTO(QRCode qrCode) {
        QRResponse response = new QRResponse();
        response.setId(qrCode.getId());
        response.setUniqueQrId(qrCode.getUniqueQrId());
        response.setName(qrCode.getName());
        response.setLocation(qrCode.getLocation());
        response.setType(qrCode.getType().name());
        response.setStatus(qrCode.getStatus().name());
        response.setDescription(qrCode.getDescription());
        response.setInstructionText(qrCode.getInstructionText());
        response.setShowRoomNumber(qrCode.getShowRoomNumber());
        response.setShowLogo(qrCode.getShowLogo());
        response.setPropertyId(qrCode.getPropertyId());
        response.setCreatedBy(qrCode.getCreatedBy());
        response.setCreatedAt(qrCode.getCreatedAt());
        response.setExpiresAt(qrCode.getExpiresAt());
        response.setScans(qrCode.getScans());
        response.setLastScannedAt(qrCode.getLastScannedAt());
        response.setQrImageUrl("/api/qr/" + qrCode.getId() + "/image");
        return response;
    }
}
