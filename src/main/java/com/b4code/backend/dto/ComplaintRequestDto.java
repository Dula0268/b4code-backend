package com.b4code.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComplaintRequestDto {
    private Long bookingId;
    private Long propertyId;
    private String category;
    private String severity;
    private String description;
    private String relatedOrderRef;
    private List<String> photoUrls;
}
