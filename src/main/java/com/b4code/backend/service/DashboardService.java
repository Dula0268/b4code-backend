package com.b4code.backend.service;

import com.b4code.backend.dto.DashboardKpiDto;
import com.b4code.backend.dto.RecentVerificationDto;
import com.b4code.backend.dto.RevenueTrendPointDto;

import java.util.List;

public interface DashboardService {
    DashboardKpiDto getDashboardKpis();
    List<RevenueTrendPointDto> getDashboardRevenueTrend();
    List<RecentVerificationDto> getRecentVerifications();
}
