package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.service.OwnerStaffService;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerStaffServiceImpl implements OwnerStaffService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public List<User> getPendingStaff(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Long> propertyIds = propertyRepository.findByOwnerId(owner.getId()).stream()
                .map(Property::getId)
                .collect(Collectors.toList());

        if (propertyIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findByPropertyIdInAndRoleAndStatus(
                propertyIds, 
                User.Role.STAFF, 
                User.UserStatus.PENDING
        );
    }

    @Override
    public void approveStaff(String ownerEmail, Long staffId) {
        updateStaffStatus(ownerEmail, staffId, User.UserStatus.APPROVED);
    }

    @Override
    public void rejectStaff(String ownerEmail, Long staffId) {
        updateStaffStatus(ownerEmail, staffId, User.UserStatus.REJECTED);
    }

    private void updateStaffStatus(String ownerEmail, Long staffId, User.UserStatus newStatus) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (staff.getRole() != User.Role.STAFF) {
            throw new CustomException("User is not a staff member", HttpStatus.BAD_REQUEST);
        }

        if (staff.getPropertyId() == null) {
            throw new CustomException("Staff member is not assigned to any property", HttpStatus.BAD_REQUEST);
        }

        Property property = propertyRepository.findById(staff.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        if (!property.getOwnerId().equals(owner.getId())) {
            throw new CustomException("You are not the owner of this property", HttpStatus.FORBIDDEN);
        }

        staff.setStatus(newStatus);
        userRepository.save(staff);
    }
}
