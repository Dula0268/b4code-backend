package com.b4code.backend.service;

import com.b4code.backend.dto.*;

import java.util.List;

public interface AnalyticsService {
    PlatformAnalyticsDto getPlatformAnalytics();

    PlatformSummaryDto getPlatformSummary();

    List<RevParDto> getRevParBreakdown();

    List<BookingChartPointDto> getBookingsChart();
}
