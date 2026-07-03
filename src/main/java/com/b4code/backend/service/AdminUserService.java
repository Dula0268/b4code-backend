package com.b4code.backend.service;

import com.b4code.backend.dto.UserDto;
import com.b4code.backend.dto.UserPageDto;
import com.b4code.backend.dto.UserStatusUpdateDto;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;

public interface AdminUserService {

    UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size);

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto, String rawPassword);

    UserDto updateUser(Long id, UserDto userDto);

    UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate);

    java.util.List<com.b4code.backend.dto.AuditLogDto> getUserActivityLogs(Long userId, int limit);

    void deleteUser(Long id);
}
