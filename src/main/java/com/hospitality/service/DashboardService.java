package com.hospitality.service;

import com.hospitality.dto.admin.DashboardKpiDto;
import com.hospitality.dto.admin.RecentVerificationDto;
import com.hospitality.dto.admin.RevenueTrendPointDto;

import java.util.List;

public interface DashboardService {
    DashboardKpiDto getDashboardKpis();
    List<RevenueTrendPointDto> getDashboardRevenueTrend();
    List<RecentVerificationDto> getRecentVerifications();
}
