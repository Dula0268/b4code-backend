package com.b4code.backend.dto.owner;

import com.b4code.backend.models.Room;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerRoomDto {

    private Long id;
    private Long propertyId;
    private String propertyName;
    private String name;
    private String description;
    private String roomType;
    private Integer maxOccupancy;
    private Integer maxChildren;
    private String bedType;
    private String baseRate;
    private String currency;
    private Integer inventory;
    private String status;
    private String imageUrl;

    public static OwnerRoomDto fromEntity(Room r) {
        String imgUrl = (r.getImage() != null) ? r.getImage().getUrl() : null;
        String propName = (r.getProperty() != null) ? r.getProperty().getName() : null;
        Long propId = (r.getProperty() != null) ? r.getProperty().getId() : null;

        return OwnerRoomDto.builder()
                .id(r.getId())
                .propertyId(propId)
                .propertyName(propName)
                .name(r.getName())
                .description(r.getDescription())
                .roomType(r.getRoomType() != null ? r.getRoomType().name() : null)
                .maxOccupancy(r.getMaxOccupancy())
                .maxChildren(r.getMaxChildren() != null ? r.getMaxChildren() : 0)
                .bedType(r.getBedType() != null ? r.getBedType().name() : null)
                .baseRate(r.getPricePerNight() != null ? r.getPricePerNight().toPlainString() : "0")
                .currency("LKR")
                .inventory(r.getInventory())
                .status(r.getStatus() != null ? r.getStatus().name() : "AVAILABLE")
                .imageUrl(imgUrl)
                .build();
    }
}
