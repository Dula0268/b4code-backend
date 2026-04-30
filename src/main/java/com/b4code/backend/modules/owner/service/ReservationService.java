package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.ReservationDto.*;
import com.b4code.backend.modules.owner.entity.Reservation;
import com.b4code.backend.modules.owner.repository.OwnerPropertyRepository;
import com.b4code.backend.modules.owner.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final OwnerPropertyRepository propertyRepository;
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MMM dd");

    public ReservationKpiResponse getReservationOverview(Long ownerId, String statusFilter, String search) {
        List<Long> pids = propertyRepository.findByOwnerId(ownerId).stream().map(p -> p.getId()).collect(Collectors.toList());
        List<Reservation> all = pids.stream().flatMap(pid -> reservationRepository.findByPropertyIdOrderByCheckInDateDesc(pid).stream()).collect(Collectors.toList());
        LocalDate today = LocalDate.now();

        ReservationKpiResponse resp = new ReservationKpiResponse();
        resp.setConfirmed((int) all.stream().filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus())).count());
        resp.setPending((int) all.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        resp.setCheckInsToday((int) all.stream().filter(r -> r.getCheckInDate().equals(today)).count());
        resp.setCancellations((int) all.stream().filter(r -> "CANCELLED".equalsIgnoreCase(r.getStatus())).count());
        resp.setTotalBookingsThisMonth((int) all.stream().filter(r -> r.getCreatedAt() != null && r.getCreatedAt().getMonth() == today.getMonth()).count());

        List<Reservation> filtered = all.stream()
            .filter(r -> statusFilter == null || "All".equals(statusFilter) || r.getStatus().equalsIgnoreCase(statusFilter))
            .filter(r -> search == null || search.isEmpty() || (r.getGuestName() != null && r.getGuestName().toLowerCase().contains(search.toLowerCase())))
            .collect(Collectors.toList());

        resp.setReservations(filtered.stream().map(this::toResponse).collect(Collectors.toList()));
        resp.setTotalItems(filtered.size());
        return resp;
    }

    public ReservationResponse getReservationById(Long id) {
        return toResponse(reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id)));
    }

    @Transactional
    public ReservationResponse createManualBooking(ManualBookingRequest req) {
        Reservation r = new Reservation();
        r.setPropertyId(req.getPropertyId()); r.setRoomId(req.getRoomId());
        r.setGuestName(req.getGuestName()); r.setGuestEmail(req.getGuestEmail()); r.setGuestId(0L);
        r.setCheckInDate(req.getCheckInDate()); r.setCheckOutDate(req.getCheckOutDate());
        r.setTotalPrice(req.getTotalPrice()); r.setPaymentStatus(req.getPaymentStatus() != null ? req.getPaymentStatus() : "UNPAID");
        r.setStatus("CONFIRMED");
        return toResponse(reservationRepository.save(r));
    }

    @Transactional
    public ReservationResponse updateStatus(Long id, String status) {
        Reservation r = reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        r.setStatus(status); reservationRepository.save(r);
        return toResponse(r);
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse resp = new ReservationResponse();
        resp.setId(r.getId()); resp.setGuestName(r.getGuestName()); resp.setGuestEmail(r.getGuestEmail());
        resp.setGuestTier(r.getGuestTier()); resp.setPropertyName(r.getPropertyName()); resp.setRoomName(r.getRoomName());
        resp.setCheckIn(r.getCheckInDate() != null ? r.getCheckInDate().format(DISPLAY_FMT) : "");
        resp.setCheckOut(r.getCheckOutDate() != null ? r.getCheckOutDate().format(DISPLAY_FMT) : "");
        resp.setPaymentStatus(r.getPaymentStatus()); resp.setStatus(r.getStatus()); resp.setTotalPrice(r.getTotalPrice());
        String name = r.getGuestName();
        if (name != null && !name.isEmpty()) { String[] p = name.split("\\s+"); resp.setGuestInitials(p.length >= 2 ? (""+p[0].charAt(0)+p[1].charAt(0)).toUpperCase() : (""+p[0].charAt(0)).toUpperCase()); }
        return resp;
    }
}
