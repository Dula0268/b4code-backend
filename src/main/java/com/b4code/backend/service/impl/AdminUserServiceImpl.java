package com.b4code.backend.service.impl;

import com.b4code.backend.dto.UserDto;
import com.b4code.backend.dto.UserPageDto;
import com.b4code.backend.dto.UserStatusUpdateDto;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.User;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.b4code.backend.dao.AuditLogRepository auditLogRepository;

    // ── GET ALL USERS ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size) {
        log.debug("Fetching users — search='{}', role={}, status={}, page={}, size={}", search, role, status, page,
                size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        Page<User> pageResult = userRepository.findAllWithFilters(searchTerm, role, status, pageable);

        List<UserDto> content = pageResult.getContent()
                .stream()
                .map(UserDto::fromEntity)
                .toList();

        log.debug("Found {} users (total={}, pages={})", content.size(), pageResult.getTotalElements(),
                pageResult.getTotalPages());

        return UserPageDto.builder()
                .content(content)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    // ── GET SINGLE USER ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.debug("Fetching user by id={}", id);

        User user = findActiveUserOrThrow(id);
        return UserDto.fromEntity(user);
    }

    // ── CREATE USER ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto, String rawPassword) {
        log.info("Creating new user with email='{}'", userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new CustomException("Email already in use", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(userDto.getFirstName() != null ? userDto.getFirstName() : "");
        user.setLastName(userDto.getLastName() != null ? userDto.getLastName() : "");
        user.setRole(userDto.getRole());
        user.setStatus(userDto.getStatus() != null ? userDto.getStatus() : UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        log.info("User created — id={}, email={}", saved.getId(), saved.getEmail());
        return UserDto.fromEntity(saved);
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user id={}", id);

        User user = findActiveUserOrThrow(id);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new CustomException("Email already in use", HttpStatus.CONFLICT);
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }

        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }

        User saved = userRepository.save(user);
        log.info("User id={} updated", id);
        return UserDto.fromEntity(saved);
    }

    // ── UPDATE USER STATUS ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate) {
        log.info("Updating user id={} status to {}", id, statusUpdate.getStatus());

        User user = findActiveUserOrThrow(id);
        user.setStatus(statusUpdate.getStatus());

        User saved = userRepository.save(user);

        com.b4code.backend.models.AuditLog logEntry = new com.b4code.backend.models.AuditLog();
        logEntry.setUser(saved);
        logEntry.setAction(statusUpdate.getStatus() == UserStatus.SUSPENDED ? "ACCOUNT_SUSPENDED" : "ACCOUNT_REACTIVATED");
        logEntry.setEntity("USER_MANAGEMENT");
        logEntry.setEntityDetail("Status updated to " + statusUpdate.getStatus());
        logEntry.setTimestamp(java.time.LocalDateTime.now());
        auditLogRepository.save(logEntry);

        log.info("User id={} status updated to {}", id, statusUpdate.getStatus());
        return UserDto.fromEntity(saved);
    }

    // ── GET USER ACTIVITY LOGS ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<com.b4code.backend.dto.AuditLogDto> getUserActivityLogs(Long userId, int limit) {
        log.info("Fetching recent activity logs for user id={}", userId);
        Pageable pageable = PageRequest.of(0, limit);
        List<com.b4code.backend.models.AuditLog> logs = auditLogRepository.findTopRecentByUserId(userId, pageable);
        return logs.stream().map(com.b4code.backend.dto.AuditLogDto::fromEntity).toList();
    }

    // ── DELETE USER ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);

        User user = findActiveUserOrThrow(id);
        user.setDeleted(true);
        userRepository.save(user);

        log.info("User id={} marked as deleted", id);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────

    private User findActiveUserOrThrow(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }
}

