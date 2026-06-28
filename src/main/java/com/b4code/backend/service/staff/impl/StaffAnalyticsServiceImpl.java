package com.b4code.backend.service.staff.impl;

import com.b4code.backend.dto.staff.analytics.OrderSummaryDto;
import com.b4code.backend.dto.staff.analytics.OrderTrendDto;
import com.b4code.backend.dto.staff.analytics.TopMenuItemDto;
import com.b4code.backend.service.staff.StaffAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAnalyticsServiceImpl implements StaffAnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDto getOrderSummary(Long propertyId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT 
                    COALESCE(SUM(total_amount), 0) as totalRevenue,
                    COUNT(id) as totalOrders,
                    COUNT(CASE WHEN status IN ('DELIVERED', 'COMPLETED') THEN 1 END) as completedCount,
                    COUNT(CASE WHEN status IN ('REJECTED', 'CANCELLED') THEN 1 END) as rejectedCount
                FROM staff.orders
                WHERE property_id = ?
                  AND created_at >= COALESCE(?, created_at)
                  AND created_at <= COALESCE(?, created_at)
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    BigDecimal totalRev = rs.getBigDecimal("totalRevenue");
                    long totalOrders = rs.getLong("totalOrders");
                    BigDecimal aov = totalOrders > 0 ? 
                            totalRev.divide(new BigDecimal(totalOrders), 2, java.math.RoundingMode.HALF_UP) : 
                            BigDecimal.ZERO;
                    
                    return OrderSummaryDto.builder()
                            .totalRevenue(totalRev)
                            .totalOrders(totalOrders)
                            .completedCount(rs.getLong("completedCount"))
                            .rejectedCount(rs.getLong("rejectedCount"))
                            .averageOrderValue(aov)
                            .build();
                },
                propertyId, 
                startDate != null ? Timestamp.valueOf(startDate) : null,
                endDate != null ? Timestamp.valueOf(endDate) : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrendDto> getOrderTrends(Long propertyId, LocalDateTime startDate, LocalDateTime endDate, String interval) {
        // Postgres date_trunc
        String sql = """
                SELECT 
                    TO_CHAR(DATE_TRUNC(?, created_at), 'YYYY-MM-DD HH24:MI:SS') as timestamp,
                    COUNT(id) as count,
                    COALESCE(SUM(total_amount), 0) as revenue
                FROM staff.orders
                WHERE property_id = ?
                  AND created_at >= COALESCE(?, created_at)
                  AND created_at <= COALESCE(?, created_at)
                GROUP BY DATE_TRUNC(?, created_at)
                ORDER BY DATE_TRUNC(?, created_at) ASC
                """;
        
        String pgInterval = switch (interval != null ? interval.toLowerCase() : "day") {
            case "hour" -> "hour";
            case "month" -> "month";
            case "week" -> "week";
            default -> "day";
        };

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> OrderTrendDto.builder()
                        .timestamp(rs.getString("timestamp"))
                        .count(rs.getLong("count"))
                        .revenue(rs.getBigDecimal("revenue"))
                        .build(),
                pgInterval,
                propertyId,
                startDate != null ? Timestamp.valueOf(startDate) : null,
                endDate != null ? Timestamp.valueOf(endDate) : null,
                pgInterval,
                pgInterval
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopMenuItemDto> getTopMenuItems(Long propertyId, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        String sql = """
                SELECT 
                    oi.menu_item_id as menuItemId,
                    mi.name as name,
                    SUM(oi.quantity) as volume,
                    SUM(oi.quantity * oi.price_at_order) as revenue
                FROM staff.order_items oi
                JOIN staff.orders o ON o.id = oi.order_id
                JOIN staff.menu_items mi ON mi.id = oi.menu_item_id
                WHERE o.property_id = ?
                  AND o.created_at >= COALESCE(?, o.created_at)
                  AND o.created_at <= COALESCE(?, o.created_at)
                  AND o.status NOT IN ('REJECTED', 'CANCELLED')
                GROUP BY oi.menu_item_id, mi.name
                ORDER BY volume DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> TopMenuItemDto.builder()
                        .menuItemId(rs.getLong("menuItemId"))
                        .name(rs.getString("name"))
                        .volume(rs.getLong("volume"))
                        .revenue(rs.getBigDecimal("revenue"))
                        .build(),
                propertyId,
                startDate != null ? Timestamp.valueOf(startDate) : null,
                endDate != null ? Timestamp.valueOf(endDate) : null,
                limit
        );
    }
}
