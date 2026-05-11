package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 500)
    private String description;

    /**
     * Discount percentage (e.g., 10 means 10% off)
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validTo;

    /**
     * Maximum number of times this code can be used (null = unlimited)
     */
    private Integer maxUses;

    /**
     * How many times this code has been used so far
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer currentUses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * If set, the promo code can only be applied to this specific property
     */
    private Long propertyId;

    /**
     * Check if the promo code is currently valid
     */
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        if (!active) return false;
        if (today.isBefore(validFrom) || today.isAfter(validTo)) return false;
        if (maxUses != null && currentUses >= maxUses) return false;
        return true;
    }
}
