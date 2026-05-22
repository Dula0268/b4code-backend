package com.b4code.backend.dto;

import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.models.Property;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {

    private Long id;
    private String name;
    private String pvId;
    private String imageUrl;
    private Long ownerId;
    private String ownerName;
    private String ownerRole;           
    private String ownerInitial;       
    private String ownerColor;          
    private PropertyStatus status;
    private String rejectionReason;
    private String submittedDate;       
    private String submittedTime;       

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private static final String[] AVATAR_COLORS = {
        "#f4a261", "#2f80ed", "#e84393", "#27ae60", "#9b59b6", "#e67e22"
    };

    public static PropertyDto fromEntity(Property p) {
        String initial = (p.getOwnerName() != null && !p.getOwnerName().isEmpty())
                ? String.valueOf(p.getOwnerName().charAt(0)).toUpperCase() : "?";
        String color = AVATAR_COLORS[(int) (p.getOwnerId() % AVATAR_COLORS.length)];

        return PropertyDto.builder()
                .id(p.getId())
                .name(p.getName())
                .pvId(p.getPvId())
                .imageUrl(p.getImageUrl())
                .ownerId(p.getOwnerId())
                .ownerName(p.getOwnerName())
                .ownerRole("Owner")
                .ownerInitial(initial)
                .ownerColor(color)
                .status(p.getStatus())
                .rejectionReason(p.getRejectionReason())
                .submittedDate(p.getSubmittedAt() != null ? p.getSubmittedAt().format(DATE_FMT) : "")
                .submittedTime(p.getSubmittedAt() != null ? p.getSubmittedAt().format(TIME_FMT) : "")
                .build();
    }

    @JsonIgnore
    public Property toEntity() {
        Property property = new Property();
        property.setName(this.name);
        property.setPvId(this.pvId);
        property.setImageUrl(this.imageUrl);
        property.setOwnerId(this.ownerId);
        property.setOwnerName(this.ownerName);
        property.setStatus(this.status != null ? this.status : PropertyStatus.PENDING);
        return property;
    }
}

