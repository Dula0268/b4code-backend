package com.b4code.backend.service;

import com.b4code.backend.dto.owner.IcalSyncDto;
import com.b4code.backend.dto.owner.IcalSyncRequest;

import java.util.List;

public interface IcalService {
    String generateIcsFeed(Long propertyId, Long roomTypeId);
    List<IcalSyncDto> getSyncFeeds(String ownerEmail, Long propertyId);
    IcalSyncDto addSyncFeed(String ownerEmail, IcalSyncRequest request);
    IcalSyncDto syncFeedNow(String ownerEmail, Long syncId);
    void deleteSyncFeed(String ownerEmail, Long syncId);
}
