package com.b4code.backend.service;

import com.b4code.backend.dao.AuditLogRepository;
import com.b4code.backend.dao.PasswordResetTokenRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.UserDto;
import com.b4code.backend.dto.UserPageDto;
import com.b4code.backend.dto.UserStatusUpdateDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@test.com");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setRole(UserRole.ADMIN);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAllWithFilters(any(), any(), any(), eq(pageable))).thenReturn(userPage);

        UserPageDto result = adminUserService.getAllUsers(null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("admin@test.com", result.getContent().get(0).getEmail());
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));

        UserDto result = adminUserService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin@test.com", result.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> adminUserService.getUserById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateUser_Success() {
        UserDto dto = UserDto.builder()
                .email("new@test.com")
                .firstName("New")
                .lastName("User")
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(dto.getEmail());
        savedUser.setRole(dto.getRole());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = adminUserService.createUser(dto, "password");

        assertNotNull(result);
        assertEquals("new@test.com", result.getEmail());
        assertEquals(UserRole.STAFF, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailExists() {
        UserDto dto = UserDto.builder().email("existing@test.com").build();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> adminUserService.createUser(dto, "password"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserStatus() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserStatusUpdateDto statusUpdate = new UserStatusUpdateDto();
        statusUpdate.setStatus(UserStatus.SUSPENDED);

        UserDto result = adminUserService.updateUserStatus(1L, statusUpdate);

        assertNotNull(result);
        assertEquals(UserStatus.SUSPENDED, testUser.getStatus()); // Check mutated state
        verify(auditLogRepository, times(1)).save(any());
    }
}
