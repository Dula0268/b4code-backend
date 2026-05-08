package com.hospitality.dto.admin;

import com.hospitality.enums.PropertyStatus;
import com.hospitality.models.Property;
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
        String status = switch (p.getStatus()) {
            case PENDING      -> "Pending";
            case UNDER_REVIEW -> "Pending";  
            case APPROVED     -> "Verified";
            case REJECTED     -> "Rejected";
        };

        String action = (p.getStatus() == PropertyStatus.APPROVED ||
                         p.getStatus() == PropertyStatus.REJECTED) ? "View" : "Review";

        return RecentVerificationDto.builder()
                .id(String.valueOf(p.getId()))
                .name(p.getName())
                .entityId("#" + p.getPvId())
                .type("Property Verification")
                .dateSubmitted(p.getSubmittedAt() != null
                        ? p.getSubmittedAt().format(DISPLAY_FORMAT)
                        : "")
                .status(status)
                .action(action)
                .icon("property")
                .build();
    }
}
