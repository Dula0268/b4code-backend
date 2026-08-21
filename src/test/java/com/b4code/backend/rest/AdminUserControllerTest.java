package com.b4code.backend.rest;

import com.b4code.backend.dto.UserDto;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disabling security filters for plain unit test of controller logic
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.b4code.backend.common.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.b4code.backend.common.security.JwtUtil jwtUtil;

    @MockBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("admin@test.com")
                .firstName("Admin")
                .role(UserRole.ADMIN)
                .build();

        when(adminUserService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        AdminUserController.CreateUserRequest request = new AdminUserController.CreateUserRequest(
                "New", "User", "new@test.com", UserRole.STAFF, "password");

        UserDto createdUser = UserDto.builder()
                .id(2L)
                .email("new@test.com")
                .role(UserRole.STAFF)
                .build();

        when(adminUserService.createUser(any(UserDto.class), eq("password"))).thenReturn(createdUser);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
