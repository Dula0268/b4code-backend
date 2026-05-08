package com.hospitality.service;

import com.hospitality.dto.admin.*;

import java.util.List;

public interface AnalyticsService {
    PlatformAnalyticsDto getPlatformAnalytics();
    PlatformSummaryDto getPlatformSummary();
    List<RevParDto> getRevParBreakdown();
    List<BookingChartPointDto> getBookingsChart();  
}
