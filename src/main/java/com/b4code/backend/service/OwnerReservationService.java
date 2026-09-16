package com.b4code.backend.service;

import com.b4code.backend.dto.owner.ManualBookingRequest;
import com.b4code.backend.dto.owner.OwnerReservationDto;

import java.util.List;

public interface OwnerReservationService {
    List<OwnerReservationDto> listReservations(String ownerEmail, String search, String status);
    OwnerReservationDto getReservation(String ownerEmail, Long id);
    OwnerReservationDto createManualBooking(String ownerEmail, ManualBookingRequest request);
    OwnerReservationDto checkIn(String ownerEmail, Long bookingId);
    OwnerReservationDto checkOut(String ownerEmail, Long bookingId);
    OwnerReservationDto cancel(String ownerEmail, Long bookingId);
    OwnerReservationDto toggleLateArrival(String ownerEmail, Long bookingId, boolean allowed);
}
