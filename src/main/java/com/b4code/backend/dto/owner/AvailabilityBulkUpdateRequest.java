package com.b4code.backend.dto.owner;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AvailabilityBulkUpdateRequest {
    private Long propertyId;
    private Long roomId;
    private List<String> dates;
    private String newStatus;
    private BigDecimal customPrice;
    private String notes;
}
