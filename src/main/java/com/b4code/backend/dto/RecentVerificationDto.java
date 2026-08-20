package com.b4code.backend.dto;

import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.models.Property;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecentVerificationDto {

    private String id;
    private String name;
    private String entityId;        
    private String type;            
    private String dateSubmitted;  
    private String status;          
    private String action;          
    private String icon;           

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static RecentVerificationDto fromEntity(Property p) {
        String status = "Verified";

        String action = "View";

        return RecentVerificationDto.builder()
                .id(String.valueOf(p.getId()))
                .name(p.getName())
                .entityId("#PROP-" + p.getId())
                .type("Property Verification")
                .dateSubmitted("N/A")
                .status(status)
                .action(action)
                .icon("property")
                .build();
    }
}

