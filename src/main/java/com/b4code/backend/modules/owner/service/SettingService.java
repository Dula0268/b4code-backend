package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.SettingDto.*;
import com.b4code.backend.modules.owner.entity.*;
import com.b4code.backend.modules.owner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService {

    private final PropertySettingRepository settingRepo;
    private final ReservationRestrictionRepository restrictionRepo;
    private final BankAccountRepository bankRepo;
    private final NotificationPreferenceRepository notifRepo;
    private final IntegrationRepository integrationRepo;

    // ── Property Settings ──
    public PropertySettingResponse getPropertySettings(Long ownerId) {
        PropertySetting s = settingRepo.findByOwnerId(ownerId).orElseGet(() -> { PropertySetting n = new PropertySetting(); n.setOwnerId(ownerId); return settingRepo.save(n); });
        PropertySettingResponse r = new PropertySettingResponse();
        r.setDefaultCurrency(s.getDefaultCurrency()); r.setTimezone(s.getTimezone()); r.setDefaultLanguage(s.getDefaultLanguage());
        r.setDefaultCheckInTime(s.getDefaultCheckInTime()); r.setDefaultCheckOutTime(s.getDefaultCheckOutTime());
        r.setVatId(s.getVatId()); r.setDefaultTaxRate(s.getDefaultTaxRate()); r.setAutoApplyTax(s.getAutoApplyTax());
        r.setAllowOverbooking(s.getAllowOverbooking()); r.setOverbookingLimit(s.getOverbookingLimit());
        return r;
    }

    @Transactional
    public PropertySettingResponse updatePropertySettings(Long ownerId, PropertySettingUpdateRequest req) {
        PropertySetting s = settingRepo.findByOwnerId(ownerId).orElseGet(() -> { PropertySetting n = new PropertySetting(); n.setOwnerId(ownerId); return n; });
        if (req.getDefaultCurrency() != null) s.setDefaultCurrency(req.getDefaultCurrency());
        if (req.getTimezone() != null) s.setTimezone(req.getTimezone());
        if (req.getDefaultLanguage() != null) s.setDefaultLanguage(req.getDefaultLanguage());
        if (req.getDefaultCheckInTime() != null) s.setDefaultCheckInTime(req.getDefaultCheckInTime());
        if (req.getDefaultCheckOutTime() != null) s.setDefaultCheckOutTime(req.getDefaultCheckOutTime());
        if (req.getVatId() != null) s.setVatId(req.getVatId());
        if (req.getDefaultTaxRate() != null) s.setDefaultTaxRate(req.getDefaultTaxRate());
        if (req.getAutoApplyTax() != null) s.setAutoApplyTax(req.getAutoApplyTax());
        if (req.getAllowOverbooking() != null) s.setAllowOverbooking(req.getAllowOverbooking());
        if (req.getOverbookingLimit() != null) s.setOverbookingLimit(req.getOverbookingLimit());
        settingRepo.save(s);
        return getPropertySettings(ownerId);
    }

    // ── Notification Preferences ──
    public NotificationPreferenceResponse getNotificationPreferences(Long ownerId) {
        NotificationPreference n = notifRepo.findByOwnerId(ownerId).orElseGet(() -> { NotificationPreference p = new NotificationPreference(); p.setOwnerId(ownerId); return notifRepo.save(p); });
        NotificationPreferenceResponse r = new NotificationPreferenceResponse();
        r.setEmailNotifications(n.getEmailNotifications()); r.setSmsAlerts(n.getSmsAlerts());
        r.setPushNotifications(n.getPushNotifications()); r.setBookingConfirmations(n.getBookingConfirmations());
        r.setMonthlyReports(n.getMonthlyReports()); r.setMaintenanceAlerts(n.getMaintenanceAlerts());
        return r;
    }

    @Transactional
    public NotificationPreferenceResponse updateNotificationPreferences(Long ownerId, NotificationPreferenceUpdateRequest req) {
        NotificationPreference n = notifRepo.findByOwnerId(ownerId).orElseGet(() -> { NotificationPreference p = new NotificationPreference(); p.setOwnerId(ownerId); return p; });
        if (req.getEmailNotifications() != null) n.setEmailNotifications(req.getEmailNotifications());
        if (req.getSmsAlerts() != null) n.setSmsAlerts(req.getSmsAlerts());
        if (req.getPushNotifications() != null) n.setPushNotifications(req.getPushNotifications());
        if (req.getBookingConfirmations() != null) n.setBookingConfirmations(req.getBookingConfirmations());
        if (req.getMonthlyReports() != null) n.setMonthlyReports(req.getMonthlyReports());
        if (req.getMaintenanceAlerts() != null) n.setMaintenanceAlerts(req.getMaintenanceAlerts());
        notifRepo.save(n);
        return getNotificationPreferences(ownerId);
    }

    // ── Bank Accounts ──
    public List<BankAccountResponse> getBankAccounts(Long ownerId) {
        return bankRepo.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(b -> {
            BankAccountResponse r = new BankAccountResponse();
            r.setId(b.getId()); r.setBankName(b.getBankName()); r.setAccountNumber(b.getAccountNumber());
            r.setAccountHolderName(b.getAccountHolderName()); r.setBranchCode(b.getBranchCode()); r.setIsPrimary(b.getIsPrimary());
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional
    public BankAccountResponse addBankAccount(Long ownerId, BankAccountRequest req) {
        BankAccount b = new BankAccount();
        b.setOwnerId(ownerId); b.setBankName(req.getBankName()); b.setAccountNumber(req.getAccountNumber());
        b.setAccountHolderName(req.getAccountHolderName()); b.setBranchCode(req.getBranchCode());
        b.setSwiftCode(req.getSwiftCode()); b.setIsPrimary(req.getIsPrimary());
        BankAccount saved = bankRepo.save(b);
        BankAccountResponse r = new BankAccountResponse();
        r.setId(saved.getId()); r.setBankName(saved.getBankName()); r.setAccountNumber(saved.getAccountNumber());
        r.setAccountHolderName(saved.getAccountHolderName()); r.setIsPrimary(saved.getIsPrimary());
        return r;
    }

    @Transactional
    public void deleteBankAccount(Long id) { bankRepo.deleteById(id); }

    // ── Integrations ──
    public List<IntegrationResponse> getIntegrations(Long ownerId) {
        return integrationRepo.findByOwnerIdOrderByNameAsc(ownerId).stream().map(i -> {
            IntegrationResponse r = new IntegrationResponse();
            r.setId(i.getId()); r.setName(i.getName()); r.setIntegrationType(i.getIntegrationType()); r.setStatus(i.getStatus());
            return r;
        }).collect(Collectors.toList());
    }

    // ── Reservation Restrictions ──
    public List<RestrictionResponse> getRestrictions(Long propertyId) {
        return restrictionRepo.findByPropertyIdOrderByCreatedAtDesc(propertyId).stream().map(r -> {
            RestrictionResponse resp = new RestrictionResponse();
            resp.setId(r.getId()); resp.setPropertyId(r.getPropertyId()); resp.setRestrictionType(r.getRestrictionType());
            resp.setMinNights(r.getMinNights()); resp.setMaxNights(r.getMaxNights());
            resp.setStartDate(r.getStartDate() != null ? r.getStartDate().toString() : null);
            resp.setEndDate(r.getEndDate() != null ? r.getEndDate().toString() : null);
            resp.setDescription(r.getDescription()); resp.setActive(r.getActive());
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    public RestrictionResponse createRestriction(RestrictionRequest req) {
        ReservationRestriction r = new ReservationRestriction();
        r.setPropertyId(req.getPropertyId()); r.setRestrictionType(req.getRestrictionType());
        r.setMinNights(req.getMinNights()); r.setMaxNights(req.getMaxNights());
        if (req.getStartDate() != null) r.setStartDate(LocalDate.parse(req.getStartDate()));
        if (req.getEndDate() != null) r.setEndDate(LocalDate.parse(req.getEndDate()));
        r.setDescription(req.getDescription()); r.setActive(req.getActive() != null ? req.getActive() : true);
        ReservationRestriction saved = restrictionRepo.save(r);
        RestrictionResponse resp = new RestrictionResponse();
        resp.setId(saved.getId()); resp.setPropertyId(saved.getPropertyId());
        resp.setRestrictionType(saved.getRestrictionType()); resp.setActive(saved.getActive());
        return resp;
    }

    @Transactional
    public void deleteRestriction(Long id) { restrictionRepo.deleteById(id); }
}
