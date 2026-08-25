package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerDashboardDto;

public interface OwnerDashboardService {
    OwnerDashboardDto getDashboard(String ownerEmail);
    OwnerDashboardDto getDashboard(String ownerEmail, Integer year, Integer month);
}
