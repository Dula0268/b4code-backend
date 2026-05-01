package com.b4code.backend.modules.staff.qr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uniqueQrId; // UUID

    @Column(nullable = false)
    private String name; // Table 01, Room 101, etc.

    @Column(length = 255)
    private String location; // Main Hall, 1st Floor, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QRType type; // Dining Table, Room, Outdoor, Bar

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QRStatus status; // ACTIVE, INACTIVE, DELETED

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "instruction_text")
    private String instructionText; // "Scan to Order Food"

    @Column(name = "show_room_number")
    private Boolean showRoomNumber = false;

    @Column(name = "show_logo")
    private Boolean showLogo = true;

    @Column(nullable = false)
    private Long propertyId; // Foreign key to Property

    @Column(name = "created_by")
    private Long createdBy; // Foreign key to User

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(columnDefinition = "BYTEA")
    private byte[] qrImage; // QR code PNG image stored as blob

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer scans = 0; // Number of times QR was scanned

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    public enum QRType {
        DINING_TABLE("Dining Table"),
        ROOM("Room"),
        OUTDOOR("Outdoor"),
        BAR("Bar");

        private final String displayName;

        QRType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum QRStatus {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = QRStatus.ACTIVE;
        }
        if (this.scans == null) {
            this.scans = 0;
        }
    }
}
