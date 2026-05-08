package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.StaffDto.*;
import com.b4code.backend.modules.owner.entity.StaffMember;
import com.b4code.backend.modules.owner.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffMemberRepository staffRepository;

    public StaffListResponse getStaffByProperty(Long propertyId, String roleFilter) {
        List<StaffMember> staff = roleFilter != null && !roleFilter.isEmpty()
            ? staffRepository.findByPropertyIdAndRole(propertyId, roleFilter)
            : staffRepository.findByPropertyIdOrderByLastNameAsc(propertyId);

        StaffListResponse resp = new StaffListResponse();
        resp.setStaff(staff.stream().map(this::toResponse).collect(Collectors.toList()));
        resp.setTotalCount(staff.size());
        return resp;
    }

    public StaffResponse getStaffById(Long id) {
        return toResponse(staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Staff not found")));
    }

    @Transactional
    public StaffResponse createStaff(StaffRequest req) {
        StaffMember s = new StaffMember();
        s.setPropertyId(req.getPropertyId()); s.setFirstName(req.getFirstName()); s.setLastName(req.getLastName());
        s.setEmail(req.getEmail()); s.setPhone(req.getPhone()); s.setRole(req.getRole());
        s.setDepartment(req.getDepartment()); s.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");
        return toResponse(staffRepository.save(s));
    }

    @Transactional
    public StaffResponse updateStaff(Long id, StaffRequest req) {
        StaffMember s = staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (req.getFirstName() != null) s.setFirstName(req.getFirstName());
        if (req.getLastName() != null) s.setLastName(req.getLastName());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getRole() != null) s.setRole(req.getRole());
        if (req.getDepartment() != null) s.setDepartment(req.getDepartment());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        return toResponse(staffRepository.save(s));
    }

    @Transactional
    public void deleteStaff(Long id) { staffRepository.deleteById(id); }

    private StaffResponse toResponse(StaffMember s) {
        StaffResponse r = new StaffResponse();
        r.setId(s.getId()); r.setPropertyId(s.getPropertyId()); r.setFirstName(s.getFirstName());
        r.setLastName(s.getLastName()); r.setEmail(s.getEmail()); r.setPhone(s.getPhone());
        r.setRole(s.getRole()); r.setDepartment(s.getDepartment()); r.setStatus(s.getStatus()); r.setPhotoUrl(s.getPhotoUrl());
        return r;
    }
}
