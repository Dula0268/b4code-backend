package com.b4code.backend.modules.admin.service.impl;

import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.dto.UserDto;
import com.b4code.backend.modules.admin.dto.UserPageDto;
import com.b4code.backend.modules.admin.dto.UserStatusUpdateDto;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.admin.service.UserService;
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
public class UserServiceImpl implements UserService {

    private final AdminUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   

    // ── GET ALL USERS (paginated + filtered) ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size) {
        log.debug("Fetching users — search='{}', role={}, status={}, page={}, size={}", search, role, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        Page<AdminUser> pageResult = userRepository.findAllWithFilters(searchTerm, role, status, pageable);

        List<UserDto> content = pageResult.getContent()
                .stream()
                .map(UserDto::fromEntity)
                .toList();

        log.debug("Found {} users (total={}, pages={})", content.size(), pageResult.getTotalElements(), pageResult.getTotalPages());

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

        if (userRepository.existsByEmailAndDeletedFalse(userDto.getEmail())) {
            throw new CustomException(
                    "A user with email '" + userDto.getEmail() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        AdminUser newUser = userDto.toEntity();
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        newUser.setStatus(UserStatus.ACTIVE);   
        AdminUser saved = userRepository.save(newUser);
        log.info("User created successfully with id={}", saved.getId());

        return UserDto.fromEntity(saved);
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user id={}", id);

        AdminUser existing = findActiveUserOrThrow(id);

        if (userDto.getFirstName() != null) existing.setFirstName(userDto.getFirstName());
        if (userDto.getLastName()  != null) existing.setLastName(userDto.getLastName());
        if (userDto.getRole()      != null) existing.setRole(userDto.getRole());

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmailAndDeletedFalse(userDto.getEmail())) {
                throw new CustomException(
                        "Email '" + userDto.getEmail() + "' is already in use by another user.",
                        HttpStatus.CONFLICT
                );
            }
            existing.setEmail(userDto.getEmail());
        }

        AdminUser updated = userRepository.save(existing);
        log.info("User id={} updated successfully", id);

        return UserDto.fromEntity(updated);
    }

    // ── UPDATE STATUS (Active ↔ Suspended) ───────────────────────────────────

    @Override
    @Transactional
    public UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate) {
        log.info("Updating status for user id={} → {}", id, statusUpdate.getStatus());

        if (statusUpdate.getStatus() == null) {
            throw new CustomException("Status field is required.", HttpStatus.BAD_REQUEST);
        }

        AdminUser user = findActiveUserOrThrow(id);
        user.setStatus(statusUpdate.getStatus());

        AdminUser updated = userRepository.save(user);
        log.info("User id={} status changed to {}", id, updated.getStatus());

        return UserDto.fromEntity(updated);
    }

    // ── SOFT DELETE ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.warn("Soft-deleting user id={}", id);   

        AdminUser user = findActiveUserOrThrow(id);

        user.setDeleted(true);
        userRepository.save(user);

        log.warn("User id={} (email='{}') has been soft-deleted", id, user.getEmail());
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private AdminUser findActiveUserOrThrow(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "User with id=" + id + " was not found.",
                        HttpStatus.NOT_FOUND
                ));
    }
}
