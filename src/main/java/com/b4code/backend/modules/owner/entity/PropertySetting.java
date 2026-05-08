package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // General Defaults
    private String defaultCurrency; // LKR, USD, EUR, GBP
    private String timezone;
    private String defaultLanguage;

    // Check-in / Check-out
    private String defaultCheckInTime;
    private String defaultCheckOutTime;

    // Tax & Fees
    private String vatId;
    private String defaultTaxRate;
    private Boolean autoApplyTax;

    // Inventory
    private Boolean allowOverbooking;
    private Integer overbookingLimit;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
