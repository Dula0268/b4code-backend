package com.b4code.backend.rest;

import com.b4code.backend.dto.FlaggedReviewDto;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.service.ModerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.b4code.backend.common.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.b4code.backend.common.security.JwtUtil jwtUtil;

    @MockBean
    private ModerationService moderationService;

    @MockBean
    private com.b4code.backend.service.ModerationExportService moderationExportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApproveReview() throws Exception {
        FlaggedReviewDto dto = FlaggedReviewDto.builder()
                .id(1L)
                .status("Approved")
                .adminNote("Looks fine")
                .build();

        when(moderationService.approveReview(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/moderation/reviews/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":\"Looks fine\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("Approved"));
    }
}
