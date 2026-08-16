package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BankAccountRepository;
import com.b4code.backend.dao.NotificationPrefRepository;
import com.b4code.backend.dao.OwnerBankDetailsRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.PropertySettingRepository;
import com.b4code.backend.dao.ReservationRestrictionRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.BankAccountDto;
import com.b4code.backend.dto.owner.BankAccountRequest;
import com.b4code.backend.dto.owner.NotificationPrefDto;
import com.b4code.backend.dto.owner.OwnerBankDetailsDto;
import com.b4code.backend.dto.owner.PropertySettingDto;
import com.b4code.backend.dto.owner.ReservationRestrictionDto;
import com.b4code.backend.dto.owner.RestrictionRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.BankAccount;
import com.b4code.backend.models.NotificationPref;
import com.b4code.backend.models.OwnerBankDetails;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.PropertySetting;
import com.b4code.backend.models.ReservationRestriction;
import com.b4code.backend.models.User;
import com.b4code.backend.models.Payout;
import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.service.OwnerSettingsService;
import com.b4code.backend.dao.PayoutRepository;
import com.b4code.backend.dto.PayoutDto;
import com.b4code.backend.dto.owner.PayoutRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerSettingsServiceImpl implements OwnerSettingsService {

    private final BankAccountRepository bankAccountRepository;
    private final OwnerBankDetailsRepository ownerBankDetailsRepository;
    private final NotificationPrefRepository notificationPrefRepository;
    private final PropertySettingRepository propertySettingRepository;
    private final ReservationRestrictionRepository restrictionRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PayoutRepository payoutRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> getBankAccounts(String ownerEmail) {
        User owner = resolveOwner(ownerEmail);
        return bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(owner.getId())
                .stream().map(BankAccountDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public BankAccountDto addBankAccount(String ownerEmail, BankAccountRequest request) {
        User owner = resolveOwner(ownerEmail);
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(owner.getId())
                    .forEach(a -> { a.setIsPrimary(false); bankAccountRepository.save(a); });
        }
        BankAccount account = BankAccount.builder()
                .ownerId(owner.getId())
                .bankName(request.getBankName())
                .accountHolder(request.getAccountHolder())
                .accountNumber(request.getAccountNumber())
                .branchCode(request.getBranchCode())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .build();
        return BankAccountDto.fromEntity(bankAccountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerBankDetailsDto getOwnerBankDetails(String ownerEmail) {
        User owner = resolveOwner(ownerEmail);
        return ownerBankDetailsRepository.findByOwnerId(owner.getId())
                .map(OwnerBankDetailsDto::fromEntity)
                .orElse(OwnerBankDetailsDto.builder().ownerId(owner.getId()).build());
    }

    @Override
    @Transactional
    public OwnerBankDetailsDto saveOwnerBankDetails(String ownerEmail, OwnerBankDetailsDto dto) {
        User owner = resolveOwner(ownerEmail);
        OwnerBankDetails details = ownerBankDetailsRepository.findByOwnerId(owner.getId())
                .orElse(OwnerBankDetails.builder().ownerId(owner.getId()).build());
        if (dto.getAccountHolderName() != null) details.setAccountHolderName(dto.getAccountHolderName());
        if (dto.getAccountNumber() != null)     details.setAccountNumber(dto.getAccountNumber());
        if (dto.getBankName() != null)          details.setBankName(dto.getBankName());
        if (dto.getBranchName() != null)        details.setBranchName(dto.getBranchName());
        if (dto.getAccountType() != null)       details.setAccountType(dto.getAccountType());
        if (dto.getBranchCode() != null)        details.setBranchCode(dto.getBranchCode());
        log.info("Owner {} saved bank details", ownerEmail);
        return OwnerBankDetailsDto.fromEntity(ownerBankDetailsRepository.save(details));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPrefDto getNotificationPrefs(String ownerEmail) {
        User owner = resolveOwner(ownerEmail);
        return notificationPrefRepository.findByOwnerId(owner.getId())
                .map(NotificationPrefDto::fromEntity)
                .orElse(NotificationPrefDto.defaults());
    }

    @Override
    @Transactional
    public NotificationPrefDto updateNotificationPrefs(String ownerEmail, NotificationPrefDto dto) {
        User owner = resolveOwner(ownerEmail);
        NotificationPref pref = notificationPrefRepository.findByOwnerId(owner.getId())
                .orElse(NotificationPref.builder().ownerId(owner.getId()).build());
        if (dto.getEmailBooking() != null)      pref.setEmailBooking(dto.getEmailBooking());
        if (dto.getEmailCancellation() != null) pref.setEmailCancellation(dto.getEmailCancellation());
        if (dto.getEmailReview() != null)       pref.setEmailReview(dto.getEmailReview());
        if (dto.getSmsBooking() != null)        pref.setSmsBooking(dto.getSmsBooking());
        if (dto.getSmsCancellation() != null)   pref.setSmsCancellation(dto.getSmsCancellation());
        pref.setUpdatedAt(LocalDateTime.now());
        return NotificationPrefDto.fromEntity(notificationPrefRepository.save(pref));
    }

    @Override
    @Transactional(readOnly = true)
    public PropertySettingDto getPropertySettings(String ownerEmail, Long propertyId) {
        verifyOwnsProperty(ownerEmail, propertyId);
        return propertySettingRepository.findByPropertyId(propertyId)
                .map(PropertySettingDto::fromEntity)
                .orElse(PropertySettingDto.defaults(propertyId));
    }

    @Override
    @Transactional
    public PropertySettingDto updatePropertySettings(String ownerEmail, Long propertyId, PropertySettingDto dto) {
        verifyOwnsProperty(ownerEmail, propertyId);
        PropertySetting setting = propertySettingRepository.findByPropertyId(propertyId)
                .orElse(PropertySetting.builder().propertyId(propertyId).build());
        if (dto.getMinStay() != null)             setting.setMinStay(dto.getMinStay());
        if (dto.getMaxStay() != null)             setting.setMaxStay(dto.getMaxStay());
        if (dto.getAdvanceBookingDays() != null)  setting.setAdvanceBookingDays(dto.getAdvanceBookingDays());
        if (dto.getInstantBooking() != null)      setting.setInstantBooking(dto.getInstantBooking());
        if (dto.getBufferDays() != null)          setting.setBufferDays(dto.getBufferDays());
        setting.setUpdatedAt(LocalDateTime.now());
        return PropertySettingDto.fromEntity(propertySettingRepository.save(setting));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRestrictionDto> getRestrictions(String ownerEmail, Long propertyId) {
        verifyOwnsProperty(ownerEmail, propertyId);
        return restrictionRepository.findByPropertyIdOrderByStartDateDesc(propertyId)
                .stream().map(ReservationRestrictionDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public ReservationRestrictionDto createRestriction(String ownerEmail, RestrictionRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());
        ReservationRestriction r = ReservationRestriction.builder()
                .property(property)
                .name(request.getName())
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return ReservationRestrictionDto.fromEntity(restrictionRepository.save(r));
    }

    @Override
    @Transactional
    public ReservationRestrictionDto updateRestriction(String ownerEmail, Long id, RestrictionRequest request) {
        User owner = resolveOwner(ownerEmail);
        ReservationRestriction r = restrictionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Restriction not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(r.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (request.getName() != null)      r.setName(request.getName());
        if (request.getType() != null)      r.setType(request.getType());
        if (request.getStartDate() != null) r.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)   r.setEndDate(request.getEndDate());
        if (request.getReason() != null)    r.setReason(request.getReason());
        if (request.getIsActive() != null)  r.setIsActive(request.getIsActive());
        return ReservationRestrictionDto.fromEntity(restrictionRepository.save(r));
    }

    @Override
    @Transactional
    public void deleteRestriction(String ownerEmail, Long id) {
        User owner = resolveOwner(ownerEmail);
        ReservationRestriction r = restrictionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Restriction not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(r.getProperty().getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        restrictionRepository.delete(r);
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private Property resolveOwnedProperty(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        return property;
    }

    private void verifyOwnsProperty(String ownerEmail, Long propertyId) {
        resolveOwnedProperty(ownerEmail, propertyId);
    }

    @Override
    @Transactional
    public PayoutDto requestPayout(String ownerEmail, PayoutRequestDto request) {
        User owner = resolveOwner(ownerEmail);
        Property property = resolveOwnedProperty(ownerEmail, request.getPropertyId());

        Payout payout = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(property.getId())
                .propertyName(property.getName())
                .amount(request.getAmount())
                .hotelAmount(request.getAmount())
                .foodAmount(java.math.BigDecimal.ZERO)
                .commissionAmount(java.math.BigDecimal.ZERO)
                .commissionRate(java.math.BigDecimal.ZERO)
                .currency("LKR")
                .status(PayoutStatus.PENDING)
                .build();

        return PayoutDto.fromEntity(payoutRepository.save(payout));
    }
}
