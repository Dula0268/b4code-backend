package com.b4code.backend.rest;

import com.b4code.backend.dto.AuthResponse;
import com.b4code.backend.dto.LoginRequest;
import com.b4code.backend.dto.RegisterRequest;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit test
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.b4code.backend.common.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.b4code.backend.common.security.JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin() throws Exception {
        // Prepare Mock Request
        String requestJson = "{\"email\":\"test@test.com\",\"password\":\"password123\"}";

        // Prepare Mock Response using AllArgsConstructor
        AuthResponse response = new AuthResponse(
                "mocked-jwt-token-for-login",
                "mocked-refresh-token",
                "test@test.com",
                "GUEST",
                1L,
                "ACTIVE",
                null,
                null
        );

        // Mock Service Layer
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Perform Request and Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token-for-login"));
    }

    @Test
    void testRegister() throws Exception {
        // Prepare Mock Request
        String requestJson = "{" +
                "\"firstName\":\"John\"," +
                "\"lastName\":\"Doe\"," +
                "\"email\":\"johndoe@test.com\"," +
                "\"password\":\"password123\"," +
                "\"role\":\"GUEST\"" +
                "}";

        // Prepare Mock Response using AllArgsConstructor
        AuthResponse response = new AuthResponse(
                "mocked-jwt-token-for-register",
                "mocked-refresh-token",
                "johndoe@test.com",
                "GUEST",
                2L,
                "ACTIVE",
                null,
                null
        );

        // Mock Service Layer
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Perform Request and Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token-for-register"));
    }
}
