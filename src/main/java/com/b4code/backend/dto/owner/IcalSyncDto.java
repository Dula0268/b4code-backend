package com.b4code.backend.dto.owner;

import com.b4code.backend.models.PropertyIcalSync;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IcalSyncDto {
    private Long id;
    private Long propertyId;
    private Long roomTypeId;
    private String roomTypeName;
    private String channelName;
    private String feedUrl;
    private String lastSyncAt;
    private String syncStatus;
    private String syncError;
    private Integer eventsImported;
    private Boolean isActive;
    private String exportFeedUrl;

    public static IcalSyncDto fromEntity(PropertyIcalSync s, String basePublicUrl) {
        String exportUrl = basePublicUrl + "/api/v1/public/ical/properties/" + s.getProperty().getId();
        if (s.getRoomType() != null) {
            exportUrl += "/rooms/" + s.getRoomType().getId();
        }
        exportUrl += "/calendar.ics";

        return IcalSyncDto.builder()
                .id(s.getId())
                .propertyId(s.getProperty() != null ? s.getProperty().getId() : null)
                .roomTypeId(s.getRoomType() != null ? s.getRoomType().getId() : null)
                .roomTypeName(s.getRoomType() != null ? s.getRoomType().getName() : "All Rooms")
                .channelName(s.getChannelName())
                .feedUrl(s.getFeedUrl())
                .lastSyncAt(s.getLastSyncAt() != null ? s.getLastSyncAt().toString() : null)
                .syncStatus(s.getSyncStatus())
                .syncError(s.getSyncError())
                .eventsImported(s.getEventsImported())
                .isActive(s.getIsActive())
                .exportFeedUrl(exportUrl)
                .build();
    }
}
