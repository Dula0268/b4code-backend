package com.b4code.backend.service.impl;

import com.b4code.backend.modules.admin.dto.UserDto;
import com.b4code.backend.modules.admin.dto.UserPageDto;
import com.b4code.backend.modules.admin.dto.UserStatusUpdateDto;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.models.AdminUser;
import com.b4code.backend.dao.AdminUserRepository;
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

    private final AdminUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── GET ALL USERS ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size) {
        log.debug("Fetching users — search='{}', role={}, status={}, page={}, size={}", search, role, status, page,
                size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        Page<AdminUser> pageResult = userRepository.findAllWithFilters(searchTerm, role, status, pageable);

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

        AdminUser user = findActiveUserOrThrow(id);
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

        AdminUser user = new AdminUser();
        user.setEmail(userDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(userDto.getRole());
        user.setStatus(userDto.getStatus() != null ? userDto.getStatus() : UserStatus.ACTIVE);

        AdminUser saved = userRepository.save(user);
        log.info("User created — id={}, email={}", saved.getId(), saved.getEmail());
        return UserDto.fromEntity(saved);
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user id={}", id);

        AdminUser user = findActiveUserOrThrow(id);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new CustomException("Email already in use", HttpStatus.CONFLICT);
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }

        AdminUser saved = userRepository.save(user);
        log.info("User id={} updated", id);
        return UserDto.fromEntity(saved);
    }

    // ── UPDATE USER STATUS ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate) {
        log.info("Updating user id={} status to {}", id, statusUpdate.getStatus());

        AdminUser user = findActiveUserOrThrow(id);
        user.setStatus(statusUpdate.getStatus());

        AdminUser saved = userRepository.save(user);
        log.info("User id={} status updated to {}", id, statusUpdate.getStatus());
        return UserDto.fromEntity(saved);
    }

    // ── DELETE USER ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);

        AdminUser user = findActiveUserOrThrow(id);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User id={} marked as DELETED", id);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────

    private AdminUser findActiveUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }
}
