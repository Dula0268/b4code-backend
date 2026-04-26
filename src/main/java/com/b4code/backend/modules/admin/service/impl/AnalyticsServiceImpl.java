package com.b4code.backend.modules.admin.service.impl;

import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.admin.dao.TransactionRepository;
import com.b4code.backend.modules.admin.dto.*;
import com.b4code.backend.modules.admin.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final PropertyRepository    propertyRepository;
    private final AdminUserRepository   userRepository;

    private static final int COMMISSION_RATE = 20;  

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'platform'")
    public PlatformAnalyticsDto getPlatformAnalytics() {
        log.debug("Computing platform analytics (cache miss)");

        BigDecimal grossBookingValue = transactionRepository.sumTotalRevenue();
        BigDecimal netRevenue = grossBookingValue
                .multiply(BigDecimal.valueOf(COMMISSION_RATE))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal avgDailyRate  = BigDecimal.valueOf(420);   
        BigDecimal avgDailyGoal  = BigDecimal.valueOf(450);
        double     occupancyRate = 78.0;
        BigDecimal revpar = avgDailyRate.multiply(BigDecimal.valueOf(occupancyRate / 100))
                .setScale(2, RoundingMode.HALF_UP);

        return PlatformAnalyticsDto.builder()
                .grossBookingValue(grossBookingValue)
                .grossBookingValueChangePct(12.4)
                .netRevenue(netRevenue)
                .commissionRate(COMMISSION_RATE)
                .occupancyRate(occupancyRate)
                .avgDailyRate(avgDailyRate)
                .avgDailyRateGoal(avgDailyGoal)
                .revpar(revpar)
                .currency("LKR")
                .build();
    }

    // ── Stat cards 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'summary'")
    public PlatformSummaryDto getPlatformSummary() {
        log.debug("Computing platform summary (cache miss)");

        long registeredUsers = userRepository.count();
        long newListings     = propertyRepository.count();     
        BigDecimal commission = transactionRepository.sumPlatformCommission();

        return PlatformSummaryDto.builder()
                .avgLeadTimeDays(18.5)
                .avgLeadTimeChange(-2.0)
                .cancellationRate(4.2)
                .totalBookings(3248L)
                .activeBookings(842L)
                .newListingsThisWeek(newListings)
                .registeredUsers(registeredUsers)
                .registeredUsersGrowthPct(5.0)
                .platformCommission(commission)
                .currency("LKR")
                .build();
    }

    // ── RevPAR per-property breakdown 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'revpar'")
    public List<RevParDto> getRevParBreakdown() {
        log.debug("Computing RevPAR breakdown (cache miss)");
        return List.of(
                RevParDto.builder().propertyId(1L).propertyName("Sunset Villa")
                        .avgDailyRate(BigDecimal.valueOf(450)).occupancyRate(82)
                        .revpar(BigDecimal.valueOf(369)).currency("LKR").build()
        );
    }

    // ── Monthly bookings chart 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'bookings-chart'")
    public List<BookingChartPointDto> getBookingsChart() {
        log.debug("Computing bookings chart (cache miss)");
        return transactionRepository.getMonthlyRevenueTrend()
                .stream()
                .map(row -> BookingChartPointDto.builder()
                        .month((String) row[0])
                        .value(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();
    }
}
