package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerStaffServiceImpl implements OwnerStaffService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public List<StaffPendingResponse> getPendingStaff(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            return List.of();
        }

        User owner = userRepository.findByEmail(ownerEmail.toLowerCase())
                .orElseThrow(() -> new CustomException("Owner account not found", HttpStatus.NOT_FOUND));

        List<Property> ownerProperties = propertyRepository.findByOwnerId(owner.getId());

        if (ownerProperties == null || ownerProperties.isEmpty()) {
            return List.of();
        }

        // Build a lookup map safely: propertyId -> propertyName (merging duplicates if any)
        Map<Long, String> propertyNameMap = ownerProperties.stream()
                .filter(p -> p != null && p.getId() != null)
                .collect(Collectors.toMap(
                        Property::getId,
                        p -> p.getName() != null ? p.getName() : "Unnamed Property",
                        (existing, replacement) -> existing
                ));

        List<Long> propertyIds = new ArrayList<>(propertyNameMap.keySet());
        if (propertyIds.isEmpty()) {
            return List.of();
        }

        List<User> pendingStaff = userRepository.findByPropertyIdInAndRoleAndStatusAndDeletedFalse(
                propertyIds,
                UserRole.STAFF,
                UserStatus.PENDING
        );

        if (pendingStaff == null || pendingStaff.isEmpty()) {
            return List.of();
        }

        return pendingStaff.stream()
                .map(staff -> StaffPendingResponse.builder()
                        .id(staff.getId())
                        .email(staff.getEmail() != null ? staff.getEmail() : "")
                        .firstName(staff.getFirstName() != null ? staff.getFirstName() : "")
                        .lastName(staff.getLastName() != null ? staff.getLastName() : "")
                        .phone(staff.getPhone() != null ? staff.getPhone() : "")
                        .propertyName(staff.getPropertyId() != null
                                ? propertyNameMap.getOrDefault(staff.getPropertyId(), "Assigned Property")
                                : "No Property Assigned")
                        .status(staff.getStatus() != null ? staff.getStatus().name() : "PENDING")
                        .role(staff.getStaffRole() != null && !staff.getStaffRole().isBlank() ? staff.getStaffRole() : "Staff Member")
                        .registeredAt(staff.getCreatedAt() != null ? staff.getCreatedAt().toString() : java.time.LocalDateTime.now().toString())
                        .build())
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

    private void updateStaffStatus(String ownerEmail, Long staffId, UserStatus newStatus) {
        User owner = userRepository.findByEmail(ownerEmail.toLowerCase())
                .orElseThrow(() -> new CustomException("Owner account not found", HttpStatus.NOT_FOUND));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new CustomException("Staff account not found", HttpStatus.NOT_FOUND));

        if (staff.getRole() != UserRole.STAFF) {
            throw new CustomException("User is not a staff member", HttpStatus.BAD_REQUEST);
        }

        if (staff.getPropertyId() == null) {
            throw new CustomException("Staff member is not assigned to any property", HttpStatus.BAD_REQUEST);
        }

        Property property = propertyRepository.findById(staff.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        if (!property.getOwnerId().equals(owner.getId())) {
            throw new CustomException("You do not have permission to manage staff for this property", HttpStatus.FORBIDDEN);
        }

        staff.setStatus(newStatus);
        userRepository.save(staff);
    }
}
