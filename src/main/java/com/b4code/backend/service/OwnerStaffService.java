package com.b4code.backend.service;

import com.b4code.backend.dto.StaffPendingResponse;
import java.util.List;

public interface OwnerStaffService {
    List<StaffPendingResponse> getPendingStaff(String ownerEmail);
    List<StaffPendingResponse> getAllStaff(String ownerEmail);
    void approveStaff(String ownerEmail, Long staffId);
    void rejectStaff(String ownerEmail, Long staffId);
}
