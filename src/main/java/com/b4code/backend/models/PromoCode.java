package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promo_codes", schema = "guest")
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

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validTo;

    private Integer maxUses;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentUses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private Long propertyId;

    public boolean isValid() {
        LocalDate today = LocalDate.now();
        if (!active) return false;
        if (today.isBefore(validFrom) || today.isAfter(validTo)) return false;
        if (maxUses != null && currentUses >= maxUses) return false;
        return true;
    }
}
