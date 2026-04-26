package com.b4code.backend.modules.admin.service;

import com.b4code.backend.modules.admin.dto.UserDto;
import com.b4code.backend.modules.admin.dto.UserPageDto;
import com.b4code.backend.modules.admin.dto.UserStatusUpdateDto;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;

public interface UserService {

    UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size);

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto, String rawPassword);

    UserDto updateUser(Long id, UserDto userDto);

    UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate);

    void deleteUser(Long id);
}
