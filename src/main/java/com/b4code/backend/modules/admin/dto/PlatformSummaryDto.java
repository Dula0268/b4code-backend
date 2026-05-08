package com.b4code.backend.modules.admin.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformSummaryDto {
    private double avgLeadTimeDays;
    private double avgLeadTimeChange;
    private double cancellationRate;
    private long totalBookings;
    private long activeBookings;
    private long newListingsThisWeek;
    private long registeredUsers;
    private double registeredUsersGrowthPct;
    private BigDecimal platformCommission;
    private String currency;
}
