package com.hospitality.service;

import com.hospitality.dto.admin.UserDto;
import com.hospitality.dto.admin.UserPageDto;
import com.hospitality.dto.admin.UserStatusUpdateDto;
import com.hospitality.enums.UserRole;
import com.hospitality.enums.UserStatus;

public interface AdminUserService {

    UserPageDto getAllUsers(String search, UserRole role, UserStatus status, int page, int size);

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto, String rawPassword);

    UserDto updateUser(Long id, UserDto userDto);

    UserDto updateUserStatus(Long id, UserStatusUpdateDto statusUpdate);

    void deleteUser(Long id);
}
