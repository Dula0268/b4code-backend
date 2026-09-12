package com.b4code.backend.dto.owner;

import lombok.Data;

@Data
public class IcalSyncRequest {
    private Long propertyId;
    private Long roomTypeId;
    private String channelName;
    private String feedUrl;
    private Boolean isActive;
}
