package com.b4code.backend.dto.owner;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePromoRequest {

    @NotBlank(message = "Promo code is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,20}$", message = "Promo code must be 3-20 alphanumeric characters")
    private String code;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "1.00", message = "Discount must be at least 1%")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercent;

    @NotNull(message = "Valid from date is required")
    private LocalDate validFrom;

    @NotNull(message = "Valid to date is required")
    private LocalDate validTo;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    private Long propertyId;
}
