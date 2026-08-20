package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.TransactionRepository;
import com.b4code.backend.dto.*;
import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.service.DashboardService;
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
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final PropertyRepository propertyRepository;

    // ── KPI cards
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "'kpis'")
    public DashboardKpiDto getDashboardKpis() {
        log.debug("Computing dashboard KPIs (cache miss)");

        BigDecimal totalRevenue = transactionRepository.sumTotalRevenue();
        long activeBookings    = transactionRepository.count();

        String revenueFormatted = "LKR " + String.format("%,.0f", totalRevenue);
        String bookingsFormatted = String.format("%,d", activeBookings);

        // Calculate dynamic occupancy (similar to platform analytics)
        long propertyCount = propertyRepository.count();
        double occupancyRate = propertyCount > 0 ? Math.min(95.0, (double)activeBookings / (propertyCount * 10) * 100) : 0.0;
        String occupancyFormatted = String.format("%.0f%%", occupancyRate);

        return DashboardKpiDto.builder()
                .totalRevenue(KpiValueDto.builder()
                        .value(revenueFormatted)
                        .change("0%") // No history yet
                        .positive(true)
                        .build())
                .occupancyRate(KpiValueDto.builder()
                        .value(occupancyFormatted)
                        .change("0%")
                        .positive(true)
                        .build())
                .activeBookings(KpiValueDto.builder()
                        .value(bookingsFormatted)
                        .change("0%")
                        .positive(true)
                        .build())
                .build();
    }

    // ── Revenue trend chart 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "'revenue-trend'")
    public List<RevenueTrendPointDto> getDashboardRevenueTrend() {
        log.debug("Computing dashboard revenue trend (cache miss)");
        return transactionRepository.getMonthlyRevenueTrend()
                .stream()
                .map(row -> RevenueTrendPointDto.builder()
                        .month((String) row[0])
                        .revenue(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();
    }

    // ── Recent verifications table 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "'recent-verifications'")
    public List<RecentVerificationDto> getRecentVerifications() {
        log.debug("Fetching recent verifications (cache miss)");
        return propertyRepository
                .findTop5ByOrderByIdDesc()
                .stream()
                .map(RecentVerificationDto::fromEntity)
                .toList();
    }
}
