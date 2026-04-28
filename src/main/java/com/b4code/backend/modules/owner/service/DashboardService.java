package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.DashboardResponse;
import com.b4code.backend.modules.owner.dto.DashboardResponse.*;
import com.b4code.backend.modules.owner.entity.OwnerProperty;
import com.b4code.backend.modules.owner.entity.Reservation;
import com.b4code.backend.modules.owner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OwnerPropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final MaintenanceAlertRepository maintenanceAlertRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    /**
     * Build the complete dashboard data for a given owner.
     *
     * @param ownerId the authenticated owner's user ID
     * @param calYear  calendar year to preview (optional, defaults to current year)
     * @param calMonth calendar month to preview, 0-indexed (optional, defaults to current month)
     * @return fully populated DashboardResponse
     */
    public DashboardResponse getDashboard(Long ownerId, Integer calYear, Integer calMonth) {
        // ── Resolve owner's properties ──
        List<OwnerProperty> properties = propertyRepository.findByOwnerId(ownerId);
        List<Long> propertyIds = properties.stream()
                .map(OwnerProperty::getId)
                .collect(Collectors.toList());

        if (propertyIds.isEmpty()) {
            return buildEmptyDashboard(calYear, calMonth);
        }

        // ── Metric Cards ──
        long totalProps = properties.size();
        long totalRooms = roomRepository.countByPropertyIdIn(propertyIds);

        List<String> activeStatuses = List.of("CONFIRMED", "CHECKED_IN", "PENDING");
        long activeReservations = reservationRepository.countByPropertyIdInAndStatusIn(propertyIds, activeStatuses);

        LocalDate today = LocalDate.now();
        long todayCheckIns = reservationRepository.countByPropertyIdInAndCheckInDate(propertyIds, today);
        long todayCheckOuts = reservationRepository.countByPropertyIdInAndCheckOutDate(propertyIds, today);

        BigDecimal totalRevenue = reservationRepository.sumTotalRevenueByPropertyIds(propertyIds);

        // ── Availability Preview ──
        int year = (calYear != null) ? calYear : today.getYear();
        int month = (calMonth != null) ? calMonth : today.getMonthValue() - 1; // convert to 0-indexed
        AvailabilityPreview availability = buildAvailabilityPreview(propertyIds, totalRooms, year, month);

        // ── Recent Reservations ──
        List<Reservation> recentRes = reservationRepository.findTop10ByPropertyIdInOrderByCreatedAtDesc(propertyIds);
        List<ReservationDto> reservationDtos = recentRes.stream()
                .map(this::toReservationDto)
                .collect(Collectors.toList());

        // ── Alert Cards ──
        long unreadReviews = reviewRepository.countByPropertyIdInAndReadByOwner(propertyIds, false);
        List<String> openStatuses = List.of("OPEN", "IN_PROGRESS");
        long urgentAlerts = maintenanceAlertRepository.countByPropertyIdInAndStatusInAndPriority(propertyIds, openStatuses, "URGENT");

        // ── Build Response ──
        return DashboardResponse.builder()
                .totalProperties(MetricCard.builder()
                        .label("TOTAL PROPERTIES").value(String.valueOf(totalProps))
                        .change("+2%").highlight(false).build())
                .totalRooms(MetricCard.builder()
                        .label("TOTAL ROOMS").value(String.valueOf(totalRooms))
                        .change("0%").highlight(false).build())
                .activeReservations(MetricCard.builder()
                        .label("ACTIVE RESV.").value(String.valueOf(activeReservations))
                        .change("+15%").highlight(false).build())
                .todayCheckIns(MetricCard.builder()
                        .label("TODAY CHECK-INS").value(String.valueOf(todayCheckIns))
                        .change(todayCheckIns > 3 ? "High" : "Normal").highlight(false).build())
                .todayCheckOuts(MetricCard.builder()
                        .label("TODAY CHECK-OUTS").value(String.valueOf(todayCheckOuts))
                        .change("-5%").highlight(false).build())
                .totalRevenue(MetricCard.builder()
                        .label("TOTAL REVENUE").value(formatRevenue(totalRevenue))
                        .change("+12%").highlight(true).build())
                .availabilityPreview(availability)
                .recentReservations(reservationDtos)
                .newReviews(AlertCard.builder()
                        .label("NEW REVIEWS").value(unreadReviews + " Unread")
                        .icon("FileText").iconColor("#953002").iconBg("#fef5ef").build())
                .maintenanceAlerts(AlertCard.builder()
                        .label("MAINT. ALERTS").value(urgentAlerts + " Urgent")
                        .icon("AlertTriangle").iconColor("#e74c3c").iconBg("#fdecea").build())
                .growth(AlertCard.builder()
                        .label("GROWTH").value("+14.2%")
                        .icon("TrendingUp").iconColor("#2e7d32").iconBg("#e8f5e9").build())
                .build();
    }

    /* ───────────────────── Helpers ───────────────────── */

    private AvailabilityPreview buildAvailabilityPreview(List<Long> propertyIds, long totalRooms, int year, int month) {
        // month is 0-indexed, convert to 1-indexed for LocalDate
        int javaMonth = month + 1;
        LocalDate startOfMonth = LocalDate.of(year, javaMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // Get reservation counts per day
        List<Object[]> dailyCounts = reservationRepository.countReservationsByDateRange(propertyIds, startOfMonth, endOfMonth);
        Map<LocalDate, Long> countMap = new HashMap<>();
        for (Object[] row : dailyCounts) {
            LocalDate date = (LocalDate) row[0];
            Long count = (Long) row[1];
            countMap.put(date, count);
        }

        // Find peak days (top 3 by booking count)
        List<Integer> peakDays = countMap.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey().getDayOfMonth())
                .sorted()
                .collect(Collectors.toList());

        // Calculate occupancy % for the month
        double occupancy = 0.0;
        if (totalRooms > 0) {
            long totalOccupiedDays = 0;
            for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {
                totalOccupiedDays += reservationRepository.countOccupiedRoomsOnDate(propertyIds, d);
            }
            long totalRoomDays = totalRooms * endOfMonth.getDayOfMonth();
            occupancy = (double) totalOccupiedDays / totalRoomDays * 100;
            occupancy = Math.round(occupancy * 10.0) / 10.0;
        }

        // Build key highlights
        List<KeyHighlight> highlights = new ArrayList<>();
        if (!peakDays.isEmpty()) {
            int firstPeak = peakDays.get(0);
            int lastPeak = peakDays.get(peakDays.size() - 1);
            String monthName = startOfMonth.getMonth().name();
            monthName = monthName.charAt(0) + monthName.substring(1, 3).toLowerCase();
            highlights.add(KeyHighlight.builder()
                    .color("#e74c3c")
                    .message("Peak booking: " + monthName + " " + ordinal(firstPeak) + " - " + ordinal(lastPeak))
                    .build());
        }
        highlights.add(KeyHighlight.builder()
                .color("#e67e22")
                .message("Maintenance: check room schedules")
                .build());

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        return AvailabilityPreview.builder()
                .year(year)
                .month(month)
                .monthName(monthNames[month])
                .occupancyPercent(occupancy)
                .peakDays(peakDays)
                .keyHighlights(highlights)
                .build();
    }

    private ReservationDto toReservationDto(Reservation r) {
        String guestName = r.getGuestName() != null ? r.getGuestName() : "Guest";
        String initials = buildInitials(guestName);
        String initialsColor = generateColor(guestName);

        long nights = ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
        String nightsStr = nights + (nights == 1 ? " Night" : " Nights");

        String dates = r.getCheckInDate().format(DATE_FMT).toUpperCase()
                + " - " + r.getCheckOutDate().format(DATE_FMT).toUpperCase();

        String statusColor;
        String statusBg;
        String displayStatus = r.getStatus().replace("_", " ");

        switch (r.getStatus()) {
            case "CHECKED_IN":
                statusColor = "#2e7d32"; statusBg = "#e8f5e9"; break;
            case "CONFIRMED":
                statusColor = "#953002"; statusBg = "#fef5ef"; break;
            case "PENDING":
                statusColor = "#e74c3c"; statusBg = "#fdecea"; break;
            case "CHECKED_OUT":
                statusColor = "#828282"; statusBg = "#f5f5f5"; break;
            default:
                statusColor = "#828282"; statusBg = "#f5f5f5"; break;
        }

        return ReservationDto.builder()
                .id(r.getId())
                .initials(initials)
                .initialsColor(initialsColor)
                .guestName(guestName)
                .propertyName(r.getPropertyName() != null ? r.getPropertyName() : "")
                .roomName(r.getRoomName() != null ? r.getRoomName() : "")
                .dates(dates)
                .nights(nightsStr)
                .status(displayStatus)
                .statusColor(statusColor)
                .statusBg(statusBg)
                .build();
    }

    private DashboardResponse buildEmptyDashboard(Integer calYear, Integer calMonth) {
        LocalDate today = LocalDate.now();
        int year = (calYear != null) ? calYear : today.getYear();
        int month = (calMonth != null) ? calMonth : today.getMonthValue() - 1;

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        return DashboardResponse.builder()
                .totalProperties(MetricCard.builder().label("TOTAL PROPERTIES").value("0").change("0%").highlight(false).build())
                .totalRooms(MetricCard.builder().label("TOTAL ROOMS").value("0").change("0%").highlight(false).build())
                .activeReservations(MetricCard.builder().label("ACTIVE RESV.").value("0").change("0%").highlight(false).build())
                .todayCheckIns(MetricCard.builder().label("TODAY CHECK-INS").value("0").change("Normal").highlight(false).build())
                .todayCheckOuts(MetricCard.builder().label("TODAY CHECK-OUTS").value("0").change("0%").highlight(false).build())
                .totalRevenue(MetricCard.builder().label("TOTAL REVENUE").value("0").change("0%").highlight(true).build())
                .availabilityPreview(AvailabilityPreview.builder()
                        .year(year).month(month).monthName(monthNames[month])
                        .occupancyPercent(0.0).peakDays(List.of()).keyHighlights(List.of()).build())
                .recentReservations(List.of())
                .newReviews(AlertCard.builder().label("NEW REVIEWS").value("0 Unread").icon("FileText").iconColor("#953002").iconBg("#fef5ef").build())
                .maintenanceAlerts(AlertCard.builder().label("MAINT. ALERTS").value("0 Urgent").icon("AlertTriangle").iconColor("#e74c3c").iconBg("#fdecea").build())
                .growth(AlertCard.builder().label("GROWTH").value("0%").icon("TrendingUp").iconColor("#2e7d32").iconBg("#e8f5e9").build())
                .build();
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String generateColor(String name) {
        // Deterministic color from name hash
        String[] colors = {"#8B6914", "#953002", "#2e7d32", "#1565c0", "#6a1b9a", "#c62828", "#00695c"};
        int idx = Math.abs(name.hashCode()) % colors.length;
        return colors[idx];
    }

    private String formatRevenue(BigDecimal revenue) {
        if (revenue == null) return "0";
        if (revenue.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
            return revenue.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP) + "M";
        } else if (revenue.compareTo(BigDecimal.valueOf(1_000)) >= 0) {
            return revenue.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP) + "K";
        }
        return revenue.setScale(0, RoundingMode.HALF_UP).toString();
    }

    private String ordinal(int day) {
        String suffix;
        if (day >= 11 && day <= 13) suffix = "th";
        else switch (day % 10) {
            case 1: suffix = "st"; break;
            case 2: suffix = "nd"; break;
            case 3: suffix = "rd"; break;
            default: suffix = "th"; break;
        }
        return day + suffix;
    }
}
