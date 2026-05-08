package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String name; // Airbnb Sync, August Lock, etc.

    private String integrationType; // CHANNEL_MANAGER, SMART_LOCK, PAYMENT_GATEWAY
    private String status; // CONNECTED, NOT_CONNECTED
    private String configJson; // JSON config blob

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
