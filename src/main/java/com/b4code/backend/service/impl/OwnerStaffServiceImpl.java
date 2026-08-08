package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dto.StaffInviteRequest;
import com.b4code.backend.dto.StaffPendingResponse;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.models.enums.UserStatus;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.service.OwnerStaffService;
import com.b4code.backend.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerStaffServiceImpl implements OwnerStaffService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<StaffPendingResponse> getPendingStaff(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Property> ownerProperties = propertyRepository.findByOwnerId(owner.getId());

        if (ownerProperties.isEmpty()) {
            return List.of();
        }

        Map<Long, String> propertyNameMap = ownerProperties.stream()
                .collect(Collectors.toMap(Property::getId, Property::getName));

        List<Long> propertyIds = ownerProperties.stream()
                .map(Property::getId)
                .collect(Collectors.toList());

        List<User> pendingStaff = userRepository.findByPropertyIdInAndRoleAndStatusAndDeletedFalse(
                propertyIds,
                UserRole.STAFF,
                UserStatus.PENDING
        );

        return pendingStaff.stream()
                .map(staff -> toResponse(staff, propertyNameMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffPendingResponse> getAllStaff(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Property> ownerProperties = propertyRepository.findByOwnerId(owner.getId());

        if (ownerProperties.isEmpty()) {
            return List.of();
        }

        Map<Long, String> propertyNameMap = ownerProperties.stream()
                .collect(Collectors.toMap(Property::getId, Property::getName));

        List<Long> propertyIds = ownerProperties.stream()
                .map(Property::getId)
                .collect(Collectors.toList());

        List<User> staff = userRepository.findByPropertyIdInAndRoleAndDeletedFalse(
                propertyIds,
                UserRole.STAFF
        );

        return staff.stream()
                .map(s -> toResponse(s, propertyNameMap))
                .collect(Collectors.toList());
    }

    @Override
    public void approveStaff(String ownerEmail, Long staffId) {
        updateStaffStatus(ownerEmail, staffId, UserStatus.APPROVED);
    }

    @Override
    public void rejectStaff(String ownerEmail, Long staffId) {
        updateStaffStatus(ownerEmail, staffId, UserStatus.REJECTED);
    }

    @Override
    public StaffPendingResponse inviteStaff(String ownerEmail, StaffInviteRequest request) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        if (!property.getOwnerId().equals(owner.getId())) {
            throw new CustomException("You are not the owner of this property", HttpStatus.FORBIDDEN);
        }

        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        User staff;
        if (existing.isPresent()) {
            staff = existing.get();
            if (staff.getRole() != UserRole.STAFF) {
                throw new CustomException("A user with this email already exists but is not a staff member", HttpStatus.CONFLICT);
            }
            staff.setPropertyId(request.getPropertyId());
            staff.setStatus(UserStatus.PENDING);
            if (request.getFirstName() != null && !request.getFirstName().isBlank()) staff.setFirstName(request.getFirstName());
            if (request.getLastName() != null && !request.getLastName().isBlank()) staff.setLastName(request.getLastName());
            if (request.getPhone() != null && !request.getPhone().isBlank()) staff.setPhone(request.getPhone());
        } else {
            staff = new User();
            staff.setEmail(request.getEmail());
            staff.setFirstName(request.getFirstName() != null ? request.getFirstName() : "Staff");
            staff.setLastName(request.getLastName() != null ? request.getLastName() : "Member");
            staff.setPhone(request.getPhone());
            staff.setRole(UserRole.STAFF);
            staff.setStatus(UserStatus.PENDING);
            staff.setPropertyId(request.getPropertyId());
            staff.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        }

        staff = userRepository.save(staff);
        Map<Long, String> nameMap = Map.of(property.getId(), property.getName());
        return toResponse(staff, nameMap);
    }

    private StaffPendingResponse toResponse(User staff, Map<Long, String> propertyNameMap) {
        return StaffPendingResponse.builder()
                .id(staff.getId())
                .email(staff.getEmail())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName() != null ? staff.getLastName() : "")
                .phone(staff.getPhone())
                .propertyName(staff.getPropertyId() != null
                        ? propertyNameMap.getOrDefault(staff.getPropertyId(), "Unknown Property")
                        : "No Property Assigned")
                .status(staff.getStatus().name())
                .createdAt(staff.getCreatedAt() != null ? staff.getCreatedAt().toString() : null)
                .build();
    }

    private void updateStaffStatus(String ownerEmail, Long staffId, UserStatus newStatus) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (staff.getRole() != UserRole.STAFF) {
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
