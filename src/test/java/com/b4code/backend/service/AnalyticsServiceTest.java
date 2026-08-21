package com.b4code.backend.service;

import com.b4code.backend.dao.*;
import com.b4code.backend.dto.PlatformAnalyticsDto;
import com.b4code.backend.dto.PlatformSummaryDto;
import com.b4code.backend.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void testGetPlatformAnalytics() {
        when(bookingRepository.sumPlatformGrossRevenue()).thenReturn(new BigDecimal("10000.00"));
        when(bookingRepository.count()).thenReturn(100L);
        when(propertyRepository.count()).thenReturn(10L);

        PlatformAnalyticsDto result = analyticsService.getPlatformAnalytics();

        assertNotNull(result);
        assertEquals(new BigDecimal("10000.00"), result.getGrossBookingValue());
        assertEquals(new BigDecimal("2000.00"), result.getNetRevenue()); // 20% commission
        assertEquals(new BigDecimal("100.00"), result.getAvgDailyRate()); // 10000 / 100
        assertEquals("LKR", result.getCurrency());
    }

    @Test
    void testGetPlatformSummary() {
        when(userRepository.count()).thenReturn(50L);
        when(propertyRepository.count()).thenReturn(20L);
        when(propertyRepository.countByCreatedAtAfter(any())).thenReturn(5L);
        when(transactionRepository.sumPlatformCommission()).thenReturn(new BigDecimal("1500.00"));
        when(bookingRepository.count()).thenReturn(200L);
        when(bookingRepository.countActiveBookings()).thenReturn(50L);
        when(bookingRepository.countCancelledBookings()).thenReturn(10L);
        when(bookingRepository.getAverageLeadTime()).thenReturn(14.5);

        PlatformSummaryDto result = analyticsService.getPlatformSummary();

        assertNotNull(result);
        assertEquals(50L, result.getRegisteredUsers());
        assertEquals(20L, result.getTotalProperties());
        assertEquals(5L, result.getNewListingsThisWeek());
        assertEquals(new BigDecimal("1500.00"), result.getPlatformCommission());
        assertEquals(200L, result.getTotalBookings());
        assertEquals(50L, result.getActiveBookings());
        assertEquals(14.5, result.getAvgLeadTimeDays());
        assertEquals(5.0, result.getCancellationRate()); // (10 / 200) * 100 = 5.0
    }
}
