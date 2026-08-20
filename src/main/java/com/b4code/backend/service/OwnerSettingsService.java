package com.b4code.backend.service;

import com.b4code.backend.dto.owner.BankAccountDto;
import com.b4code.backend.dto.owner.BankAccountRequest;
import com.b4code.backend.dto.owner.NotificationPrefDto;
import com.b4code.backend.dto.owner.PropertySettingDto;
import com.b4code.backend.dto.owner.ReservationRestrictionDto;
import com.b4code.backend.dto.owner.RestrictionRequest;

import java.util.List;

public interface OwnerSettingsService {
    List<BankAccountDto> getBankAccounts(String ownerEmail);
    List<BankAccountDto> getBankAccountsByOwnerId(Long ownerId);
    BankAccountDto addBankAccount(String ownerEmail, BankAccountRequest request);
    BankAccountDto addBankAccountByOwnerId(Long ownerId, BankAccountRequest request);
    NotificationPrefDto getNotificationPrefs(String ownerEmail);
    NotificationPrefDto updateNotificationPrefs(String ownerEmail, NotificationPrefDto dto);
    PropertySettingDto getPropertySettings(String ownerEmail, Long propertyId);
    PropertySettingDto updatePropertySettings(String ownerEmail, Long propertyId, PropertySettingDto dto);
    List<ReservationRestrictionDto> getRestrictions(String ownerEmail, Long propertyId);
    ReservationRestrictionDto createRestriction(String ownerEmail, RestrictionRequest request);
    ReservationRestrictionDto updateRestriction(String ownerEmail, Long id, RestrictionRequest request);
    void deleteRestriction(String ownerEmail, Long id);
    com.b4code.backend.dto.PayoutDto requestPayout(String ownerEmail, Long propertyId);
}
