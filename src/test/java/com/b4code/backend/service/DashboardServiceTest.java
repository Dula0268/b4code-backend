package com.b4code.backend.service;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.TransactionRepository;
import com.b4code.backend.dto.DashboardKpiDto;
import com.b4code.backend.dto.RecentVerificationDto;
import com.b4code.backend.dto.RevenueTrendPointDto;
import com.b4code.backend.models.Property;
import com.b4code.backend.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void testGetDashboardKpis() {
        when(transactionRepository.sumTotalRevenue()).thenReturn(new BigDecimal("50000.00"));
        when(transactionRepository.count()).thenReturn(100L);
        when(propertyRepository.count()).thenReturn(20L);

        DashboardKpiDto result = dashboardService.getDashboardKpis();

        assertNotNull(result);
        assertNotNull(result.getTotalRevenue());
        assertEquals("LKR 50,000", result.getTotalRevenue().getValue());
        assertNotNull(result.getActiveBookings());
        assertEquals("100", result.getActiveBookings().getValue());
        assertNotNull(result.getOccupancyRate());
        assertEquals("50%", result.getOccupancyRate().getValue());
    }

    @Test
    void testGetDashboardRevenueTrend() {
        Object[] row = new Object[]{"Jan", new BigDecimal("1000.00")};
        when(transactionRepository.getMonthlyRevenueTrend()).thenReturn(Collections.singletonList(row));

        List<RevenueTrendPointDto> result = dashboardService.getDashboardRevenueTrend();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jan", result.get(0).getMonth());
        assertEquals(new BigDecimal("1000.00"), result.get(0).getRevenue());
    }

    @Test
    void testGetRecentVerifications() {
        Property property = new Property();
        property.setId(1L);
        property.setName("Test Hotel");
        when(propertyRepository.findTop5ByOrderByIdDesc()).thenReturn(Collections.singletonList(property));

        List<RecentVerificationDto> result = dashboardService.getRecentVerifications();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("Test Hotel", result.get(0).getName());
    }
}
