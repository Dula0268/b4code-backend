package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.RoomRepository;
import com.b4code.backend.dao.UserRepository;
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
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    private static final int COMMISSION_RATE = 20;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'platform'")
    public PlatformAnalyticsDto getPlatformAnalytics() {
        log.debug("Computing platform analytics (cache miss)");

        BigDecimal grossBookingValue = bookingRepository.sumPlatformGrossRevenue();
        BigDecimal netRevenue = grossBookingValue.multiply(BigDecimal.valueOf(COMMISSION_RATE))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate real Total Bookings
        long totalBookings = bookingRepository.count(); // Count total bookings in system

        // Calculate ADR (Gross / Bookings) or default to 0
        BigDecimal avgDailyRate = totalBookings > 0
                ? grossBookingValue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgDailyGoal = BigDecimal.valueOf(500); // Standard platform goal

        // Calculate occupancy based on property count vs bookings (mock logic if no
        // actual stay dates)
        long propertyCount = propertyRepository.count();
        double rawOccupancyRate = propertyCount > 0
                ? Math.min(95.0, (double) totalBookings / (propertyCount * 10) * 100)
                : 0.0;
        double occupancyRate = Math.round(rawOccupancyRate * 10.0) / 10.0;

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
        long propertyCount = propertyRepository.count();
        long newPropertiesThisWeek = propertyRepository
                .countByCreatedAtAfter(java.time.LocalDateTime.now().minusDays(7));
        BigDecimal commission = transactionRepository.sumPlatformCommission();
        long totalBookings = bookingRepository.count();
        long activeBookings = bookingRepository.countActiveBookings();
        long cancelledBookings = bookingRepository.countCancelledBookings();
        Double avgLeadTime = bookingRepository.getAverageLeadTime();

        double cancellationRate = totalBookings > 0
                ? ((double) cancelledBookings / totalBookings) * 100.0
                : 0.0;

        return PlatformSummaryDto.builder()
                .avgLeadTimeDays(avgLeadTime != null ? Math.round(avgLeadTime * 10.0) / 10.0 : 0.0)
                .avgLeadTimeChange(0.0) // Requires historical lead time data
                .cancellationRate(Math.round(cancellationRate * 10.0) / 10.0)
                .totalBookings(totalBookings)
                .activeBookings(activeBookings)
                .newListingsThisWeek(newPropertiesThisWeek)
                .totalProperties(propertyCount)
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

        List<com.b4code.backend.models.Room> rooms = roomRepository.findAllWithRelations();
        List<Object[]> aggregatedMetrics = bookingRepository.getRoomAggregatedMetrics();

        // Build a map for O(1) lookup
        java.util.Map<Long, Object[]> metricsMap = new java.util.HashMap<>();
        for (Object[] row : aggregatedMetrics) {
            Number roomId = (Number) row[0];
            metricsMap.put(roomId.longValue(), row);
        }

        return rooms.stream().map(room -> {
            long roomId = room.getId();

            long soldNights = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            Object[] metrics = metricsMap.get(roomId);
            if (metrics != null) {
                totalRevenue = metrics[1] != null ? new BigDecimal(metrics[1].toString()) : BigDecimal.ZERO;
                soldNights = metrics[2] != null ? ((Number) metrics[2]).longValue() : 0;
            }

            // Calculate ADR (Average Daily Rate)
            BigDecimal adr = soldNights > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(soldNights), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Approximate Occupancy (assuming 30 days timeframe for MVP all-time
            // calculation)
            double occupancy = Math.min(100.0, (soldNights / 30.0) * 100.0);

            BigDecimal revpar = adr.multiply(BigDecimal.valueOf(occupancy / 100.0)).setScale(2, RoundingMode.HALF_UP);

            String imageUrl = room.getImage() != null && room.getImage().getUrl() != null
                    ? room.getImage().getUrl()
                    : "/images/placeholder-property.jpg";

            String propertyName = room.getProperty() != null ? room.getProperty().getName() : "Unknown Property";

            return RevParDto.builder()
                    .propertyId(room.getProperty() != null ? room.getProperty().getId() : 0L)
                    .propertyName(propertyName)
                    .type(room.getRoomType() != null ? room.getRoomType().name() : "N/A")
                    .roomNumber("Room " + roomId)
                    .adults(room.getMaxOccupancy() != null ? room.getMaxOccupancy() : 2)
                    .sqm(0) // Default if no SQM
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

        // Initialize 12 months with 0
        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        java.util.Map<String, BigDecimal> monthMap = new java.util.LinkedHashMap<>();
        for (String m : months) {
            monthMap.put(m, BigDecimal.ZERO);
        }

        // Override with actual data
        List<Object[]> dbData = bookingRepository.getMonthlyBookingRevenueTrend();
        for (Object[] row : dbData) {
            String month = (String) row[0];
            BigDecimal val = (BigDecimal) row[1];
            if (monthMap.containsKey(month)) {
                monthMap.put(month, val);
            }
        }

        return monthMap.entrySet().stream()
                .map(e -> {
                    BigDecimal gross = e.getValue().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal net = gross.multiply(BigDecimal.valueOf(COMMISSION_RATE)).divide(BigDecimal.valueOf(100),
                            2, RoundingMode.HALF_UP);
                    return BookingChartPointDto.builder()
                            .month(e.getKey())
                            .value(gross)
                            .netRevenue(net)
                            .build();
                })
                .toList();
    }
}
