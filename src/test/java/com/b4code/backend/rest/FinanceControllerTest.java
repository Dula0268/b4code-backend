package com.b4code.backend.rest;

import com.b4code.backend.dto.FinanceSummaryDto;
import com.b4code.backend.service.FinanceService;
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

@WebMvcTest(FinanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.b4code.backend.common.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.b4code.backend.common.security.JwtUtil jwtUtil;

    @MockBean
    private FinanceService financeService;

    @MockBean
    private com.b4code.backend.service.FinanceExportService financeExportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetFinanceSummary() throws Exception {
        FinanceSummaryDto summaryDto = FinanceSummaryDto.builder()
                .totalRevenue(new BigDecimal("50000.00"))
                .platformCommission(new BigDecimal("10000.00"))
                .currency("LKR")
                .build();

        when(financeService.getFinanceSummary()).thenReturn(summaryDto);

        mockMvc.perform(get("/api/admin/finance/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000.00))
                .andExpect(jsonPath("$.platformCommission").value(10000.00));
    }
}
