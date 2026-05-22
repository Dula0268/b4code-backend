package com.b4code.backend.modules.owner.service;

import com.b4code.backend.models.User;
import java.util.List;

public interface OwnerStaffService {
    List<User> getPendingStaff(String ownerEmail);
    void approveStaff(String ownerEmail, Long staffId);
    void rejectStaff(String ownerEmail, Long staffId);
}
