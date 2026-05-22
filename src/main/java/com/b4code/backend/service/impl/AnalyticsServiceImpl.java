package com.b4code.backend.service.impl;

import com.b4code.backend.dao.AdminUserRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.TransactionRepository;
import com.b4code.backend.dto.*;
import com.b4code.backend.service.AnalyticsService;
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
        BigDecimal netRevenue = transactionRepository.sumPlatformCommission();
        
        // Calculate real Total Bookings
        long totalBookings = transactionRepository.count(); // Basic count for now
        
        // Calculate ADR (Gross / Bookings) or default to 0
        BigDecimal avgDailyRate = totalBookings > 0 
            ? grossBookingValue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
            
        BigDecimal avgDailyGoal  = BigDecimal.valueOf(500); // Standard platform goal
        
        // Calculate occupancy based on property count vs bookings (mock logic if no actual stay dates)
        long propertyCount = propertyRepository.count();
        double occupancyRate = propertyCount > 0 ? Math.min(95.0, (double)totalBookings / (propertyCount * 10) * 100) : 0.0;
        
        BigDecimal revpar = avgDailyRate.multiply(BigDecimal.valueOf(occupancyRate / 100))
                .setScale(2, RoundingMode.HALF_UP);

        return PlatformAnalyticsDto.builder()
                .grossBookingValue(grossBookingValue)
                .grossBookingValueChangePct(0.0) // No historical data yet
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
        long propertyCount   = propertyRepository.count();     
        BigDecimal commission = transactionRepository.sumPlatformCommission();
        long totalBookings    = transactionRepository.count();

        return PlatformSummaryDto.builder()
                .avgLeadTimeDays(0.0) // Mock 0 until we have actual stay-start dates
                .avgLeadTimeChange(0.0)
                .cancellationRate(0.0)
                .totalBookings(totalBookings)
                .activeBookings(totalBookings) // For now, all bookings are active
                .newListingsThisWeek(propertyCount)
                .registeredUsers(registeredUsers)
                .registeredUsersGrowthPct(0.0)
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
        
        List<com.b4code.backend.models.Property> properties = propertyRepository.findAll();
        
        return properties.stream().map(prop -> {
            long id = prop.getId();
            // Generate deterministic realistic values based on ID
            double occupancy = 60.0 + (id % 35); // 60% to 95%
            BigDecimal adr = BigDecimal.valueOf(300 + (id * 50)); 
            BigDecimal revpar = adr.multiply(BigDecimal.valueOf(occupancy / 100.0)).setScale(2, RoundingMode.HALF_UP);
            
            String[] types = {"Suite", "Villa", "Penthouse", "Eco Cabin"};
            String type = types[(int)(id % types.length)];
            
            String imageUrl = prop.getImageUrl() != null && !prop.getImageUrl().isEmpty() 
                ? prop.getImageUrl() 
                : "/images/placeholder-property.jpg";
                
            return RevParDto.builder()
                    .propertyId(prop.getId())
                    .propertyName(prop.getName())
                    .type(type)
                    .roomNumber("Room " + (100 + id))
                    .adults(2 + (int)(id % 4))
                    .sqm(80 + (int)(id * 10))
                    .image(imageUrl)
                    .avgDailyRate(adr)
                    .occupancyRate(occupancy)
                    .revpar(revpar)
                    .currency("LKR")
                    .build();
        }).toList();
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
