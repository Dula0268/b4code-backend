package com.b4code.backend.service.staff;

import com.b4code.backend.dto.staff.analytics.OrderSummaryDto;
import com.b4code.backend.dto.staff.analytics.OrderTrendDto;
import com.b4code.backend.dto.staff.analytics.TopMenuItemDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StaffAnalyticsService {
    OrderSummaryDto getOrderSummary(Long propertyId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderTrendDto> getOrderTrends(Long propertyId, LocalDateTime startDate, LocalDateTime endDate, String interval);
    List<TopMenuItemDto> getTopMenuItems(Long propertyId, LocalDateTime startDate, LocalDateTime endDate, int limit);
}
