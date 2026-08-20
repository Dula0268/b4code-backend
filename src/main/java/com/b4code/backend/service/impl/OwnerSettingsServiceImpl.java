package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BankAccountRepository;
import com.b4code.backend.dao.NotificationPrefRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.PropertySettingRepository;
import com.b4code.backend.dao.ReservationRestrictionRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.BankAccountDto;
import com.b4code.backend.dto.owner.BankAccountRequest;
import com.b4code.backend.dto.owner.NotificationPrefDto;
import com.b4code.backend.dto.owner.PropertySettingDto;
import com.b4code.backend.dto.owner.ReservationRestrictionDto;
import com.b4code.backend.dto.owner.RestrictionRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.BankAccount;
import com.b4code.backend.models.NotificationPref;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.PropertySetting;
import com.b4code.backend.models.ReservationRestriction;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerSettingsService;
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
    private final NotificationPrefRepository notificationPrefRepository;
    private final PropertySettingRepository propertySettingRepository;
    private final ReservationRestrictionRepository restrictionRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final com.b4code.backend.dao.PayoutRepository payoutRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> getBankAccounts(String ownerEmail) {
        User owner = resolveOwner(ownerEmail);
        return getBankAccountsByOwnerId(owner.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> getBankAccountsByOwnerId(Long ownerId) {
        return bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(ownerId)
                .stream().map(BankAccountDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public BankAccountDto addBankAccount(String ownerEmail, BankAccountRequest request) {
        User owner = resolveOwner(ownerEmail);
        return addBankAccountByOwnerId(owner.getId(), request);
    }

    @Override
    @Transactional
    public BankAccountDto addBankAccountByOwnerId(Long ownerId, BankAccountRequest request) {
        List<BankAccount> existing = bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(ownerId);
        boolean makePrimary = Boolean.TRUE.equals(request.getIsPrimary()) || existing.isEmpty();

        if (makePrimary) {
            existing.forEach(a -> { a.setIsPrimary(false); bankAccountRepository.save(a); });
        }
        BankAccount account = BankAccount.builder()
                .ownerId(ownerId)
                .bankName(request.getBankName())
                .accountHolder(request.getAccountHolder())
                .accountNumber(request.getAccountNumber())
                .branchCode(request.getBranchCode())
                .isPrimary(makePrimary)
                .build();
        return BankAccountDto.fromEntity(bankAccountRepository.save(account));
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

    @Override
    @Transactional
    public com.b4code.backend.dto.PayoutDto requestPayout(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);

        java.time.LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<com.b4code.backend.models.Payout> activePayouts = payoutRepository
                .findRecentActiveByOwnerIdOrderByRequestedAtDesc(owner.getId(), List.of(
                        com.b4code.backend.models.enums.PayoutStatus.PENDING,
                        com.b4code.backend.models.enums.PayoutStatus.PROCESSED
                ), cutoff);
        if (!activePayouts.isEmpty()) {
            boolean hasPending = activePayouts.stream()
                    .anyMatch(p -> p.getStatus() == com.b4code.backend.models.enums.PayoutStatus.PENDING);

            if (hasPending) {
                throw new CustomException(
                        "A payout request is already pending. Please wait for admin approval before requesting another payout.",
                        HttpStatus.BAD_REQUEST
                );
            }

            com.b4code.backend.models.Payout latestProcessed = null;
            for (com.b4code.backend.models.Payout payoutEntry : activePayouts) {
                if (payoutEntry.getStatus() != com.b4code.backend.models.enums.PayoutStatus.PROCESSED) {
                    continue;
                }

                if (latestProcessed == null) {
                    latestProcessed = payoutEntry;
                    continue;
                }

                java.time.LocalDateTime currentDate = payoutEntry.getProcessedAt() != null
                        ? payoutEntry.getProcessedAt()
                        : payoutEntry.getRequestedAt();
                java.time.LocalDateTime latestDate = latestProcessed.getProcessedAt() != null
                        ? latestProcessed.getProcessedAt()
                        : latestProcessed.getRequestedAt();

                if (currentDate != null && (latestDate == null || currentDate.isAfter(latestDate))) {
                    latestProcessed = payoutEntry;
                }
            }

            String message = "A payout was already processed in the last 30 days.";
            if (latestProcessed != null) {
                java.time.LocalDateTime latestActionDate = latestProcessed.getProcessedAt() != null
                        ? latestProcessed.getProcessedAt()
                        : latestProcessed.getRequestedAt();

                if (latestActionDate != null) {
                    java.time.LocalDate nextEligibleDate = latestActionDate.plusDays(30).toLocalDate();
                    message += " You can request another payout from " + nextEligibleDate + ".";
                } else {
                    message += " You can request another payout after 30 days from the approved date.";
                }
            } else {
                message += " You can request another payout after 30 days from the approved date.";
            }

            throw new CustomException(message, HttpStatus.BAD_REQUEST);
        }
        
        List<BankAccount> bankAccounts = bankAccountRepository.findByOwnerIdOrderByIsPrimaryDescCreatedAtDesc(owner.getId());
        if (bankAccounts == null || bankAccounts.isEmpty()) {
            throw new CustomException("Please add a primary bank account before requesting a payout.", HttpStatus.BAD_REQUEST);
        }
        
        Property property = null;
        if (propertyId != null) {
            property = propertyRepository.findById(propertyId).orElse(null);
        }
        if (property == null) {
            property = propertyRepository.findAll().stream()
                    .filter(p -> owner.getId().equals(p.getOwnerId()))
                    .findFirst()
                    .orElse(null);
        }

        String propName = property != null ? property.getName() : "Owner Property";
        Long propId = property != null ? property.getId() : null;

        java.math.BigDecimal defaultHotelAmt = new java.math.BigDecimal("50000.00");
        java.math.BigDecimal defaultFoodAmt = new java.math.BigDecimal("10000.00");
        java.math.BigDecimal commissionRate = new java.math.BigDecimal("20.00");
        java.math.BigDecimal commissionAmt = defaultHotelAmt.multiply(commissionRate)
                .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal netAmount = defaultHotelAmt.subtract(commissionAmt).add(defaultFoodAmt);

        com.b4code.backend.models.Payout payout = com.b4code.backend.models.Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(propId)
                .propertyName(propName)
                .hotelAmount(defaultHotelAmt)
                .foodAmount(defaultFoodAmt)
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmt)
                .amount(netAmount)
                .currency("LKR")
                .status(com.b4code.backend.models.enums.PayoutStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        com.b4code.backend.models.Payout saved = payoutRepository.save(payout);

        com.b4code.backend.dto.PayoutDto dto = com.b4code.backend.dto.PayoutDto.fromEntity(saved);
        BankAccount primaryBank = bankAccounts.get(0);
        dto.setBankName(primaryBank.getBankName());
        dto.setAccountHolder(primaryBank.getAccountHolder());
        dto.setAccountNumber(primaryBank.getAccountNumber());
        dto.setBranchCode(primaryBank.getBranchCode());
        dto.setBankDetails(primaryBank.getBankName() + " — Acc: " + primaryBank.getAccountNumber() + " (Holder: " + primaryBank.getAccountHolder() + ")");
        return dto;
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
}
