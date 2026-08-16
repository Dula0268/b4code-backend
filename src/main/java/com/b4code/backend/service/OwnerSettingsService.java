package com.b4code.backend.service;

import com.b4code.backend.dto.owner.BankAccountDto;
import com.b4code.backend.dto.owner.BankAccountRequest;
import com.b4code.backend.dto.owner.NotificationPrefDto;
import com.b4code.backend.dto.owner.OwnerBankDetailsDto;
import com.b4code.backend.dto.owner.PropertySettingDto;
import com.b4code.backend.dto.owner.ReservationRestrictionDto;
import com.b4code.backend.dto.owner.RestrictionRequest;
import com.b4code.backend.dto.PayoutDto;
import com.b4code.backend.dto.owner.PayoutRequestDto;

import java.util.List;

public interface OwnerSettingsService {
    // owner.bank_accounts (multi-account list)
    List<BankAccountDto> getBankAccounts(String ownerEmail);
    BankAccountDto addBankAccount(String ownerEmail, BankAccountRequest request);

    // owner.owner_bank_details (single primary payout record per owner)
    OwnerBankDetailsDto getOwnerBankDetails(String ownerEmail);
    OwnerBankDetailsDto saveOwnerBankDetails(String ownerEmail, OwnerBankDetailsDto dto);
    NotificationPrefDto getNotificationPrefs(String ownerEmail);
    NotificationPrefDto updateNotificationPrefs(String ownerEmail, NotificationPrefDto dto);
    PropertySettingDto getPropertySettings(String ownerEmail, Long propertyId);
    PropertySettingDto updatePropertySettings(String ownerEmail, Long propertyId, PropertySettingDto dto);
    List<ReservationRestrictionDto> getRestrictions(String ownerEmail, Long propertyId);
    ReservationRestrictionDto createRestriction(String ownerEmail, RestrictionRequest request);
    ReservationRestrictionDto updateRestriction(String ownerEmail, Long id, RestrictionRequest request);
    void deleteRestriction(String ownerEmail, Long id);

    PayoutDto requestPayout(String ownerEmail, PayoutRequestDto request);
}
