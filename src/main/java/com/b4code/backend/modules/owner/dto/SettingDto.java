package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Settings module — account, property, notification, billing, integrations
 */
public class SettingDto {

    // ── Account Settings ──
    @Data
    public static class AccountSettingResponse {
        private String firstName;
        private String lastName;
        private String email;
        private String photoUrl;
    }

    @Data
    public static class AccountUpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }

    // ── Property Settings ──
    @Data
    public static class PropertySettingResponse {
        private String defaultCurrency;
        private String timezone;
        private String defaultLanguage;
        private String defaultCheckInTime;
        private String defaultCheckOutTime;
        private String vatId;
        private String defaultTaxRate;
        private Boolean autoApplyTax;
        private Boolean allowOverbooking;
        private Integer overbookingLimit;
    }

    @Data
    public static class PropertySettingUpdateRequest {
        private String defaultCurrency;
        private String timezone;
        private String defaultLanguage;
        private String defaultCheckInTime;
        private String defaultCheckOutTime;
        private String vatId;
        private String defaultTaxRate;
        private Boolean autoApplyTax;
        private Boolean allowOverbooking;
        private Integer overbookingLimit;
    }

    // ── Notification Preferences ──
    @Data
    public static class NotificationPreferenceResponse {
        private Boolean emailNotifications;
        private Boolean smsAlerts;
        private Boolean pushNotifications;
        private Boolean bookingConfirmations;
        private Boolean monthlyReports;
        private Boolean maintenanceAlerts;
    }

    @Data
    public static class NotificationPreferenceUpdateRequest {
        private Boolean emailNotifications;
        private Boolean smsAlerts;
        private Boolean pushNotifications;
        private Boolean bookingConfirmations;
        private Boolean monthlyReports;
        private Boolean maintenanceAlerts;
    }

    // ── Billing & Payouts ──
    @Data
    public static class BankAccountRequest {
        private String bankName;
        private String accountNumber;
        private String accountHolderName;
        private String branchCode;
        private String swiftCode;
        private Boolean isPrimary;
    }

    @Data
    public static class BankAccountResponse {
        private Long id;
        private String bankName;
        private String accountNumber;
        private String accountHolderName;
        private String branchCode;
        private Boolean isPrimary;
    }

    // ── Integrations ──
    @Data
    public static class IntegrationResponse {
        private Long id;
        private String name;
        private String integrationType;
        private String status;
    }

    @Data
    public static class IntegrationUpdateRequest {
        private Long id;
        private String status;
        private String configJson;
    }

    // ── Reservation Restrictions ──
    @Data
    public static class RestrictionRequest {
        private Long propertyId;
        private String restrictionType;
        private Integer minNights;
        private Integer maxNights;
        private String startDate;
        private String endDate;
        private String description;
        private Boolean active;
    }

    @Data
    public static class RestrictionResponse {
        private Long id;
        private Long propertyId;
        private String restrictionType;
        private Integer minNights;
        private Integer maxNights;
        private String startDate;
        private String endDate;
        private String description;
        private Boolean active;
    }
}
