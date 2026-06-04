package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stores admin-configurable platform settings as key→value pairs.
 * Stored in the admin schema.
 *
 * Keys:
 *   COMMISSION_RATE  — the percentage commission on hotel bookings (e.g. "20.0")
 */
@Entity
@Table(name = "platform_config", schema = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(nullable = false, length = 500)
    private String configValue;

    @Column(length = 500)
    private String description;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
