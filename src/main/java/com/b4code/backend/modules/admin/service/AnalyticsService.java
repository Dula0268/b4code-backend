package com.b4code.backend.modules.admin.service;

import com.b4code.backend.modules.admin.dto.*;

import java.util.List;

public interface AnalyticsService {
    PlatformAnalyticsDto getPlatformAnalytics();
    PlatformSummaryDto getPlatformSummary();
    List<RevParDto> getRevParBreakdown();
    List<BookingChartPointDto> getBookingsChart();  
}
