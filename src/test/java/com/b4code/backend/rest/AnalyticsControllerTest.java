package com.b4code.backend.rest;

import com.b4code.backend.dto.PlatformAnalyticsDto;
import com.b4code.backend.dto.PlatformSummaryDto;
import com.b4code.backend.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.b4code.backend.common.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.b4code.backend.common.security.JwtUtil jwtUtil;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPlatformAnalytics() throws Exception {
        PlatformAnalyticsDto analyticsDto = PlatformAnalyticsDto.builder()
                .grossBookingValue(new BigDecimal("10000.00"))
                .netRevenue(new BigDecimal("2000.00"))
                .currency("LKR")
                .build();

        when(analyticsService.getPlatformAnalytics()).thenReturn(analyticsDto);

        mockMvc.perform(get("/api/admin/analytics/platform")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grossBookingValue").value(10000.00))
                .andExpect(jsonPath("$.netRevenue").value(2000.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPlatformSummary() throws Exception {
        PlatformSummaryDto summaryDto = PlatformSummaryDto.builder()
                .totalBookings(150L)
                .activeBookings(50L)
                .currency("LKR")
                .build();

        when(analyticsService.getPlatformSummary()).thenReturn(summaryDto);

        mockMvc.perform(get("/api/admin/analytics/platform-summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(150))
                .andExpect(jsonPath("$.activeBookings").value(50));
    }
}
