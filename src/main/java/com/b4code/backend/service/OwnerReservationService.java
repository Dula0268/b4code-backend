package com.b4code.backend.service;

import com.b4code.backend.dto.owner.ManualBookingRequest;
import com.b4code.backend.dto.owner.OwnerReservationDto;

import java.util.List;

public interface OwnerReservationService {
    List<OwnerReservationDto> listReservations(String ownerEmail, String search, String status);
    OwnerReservationDto getReservation(String ownerEmail, Long id);
    OwnerReservationDto createManualBooking(String ownerEmail, ManualBookingRequest request);
    OwnerReservationDto checkIn(String ownerEmail, Long id);
    OwnerReservationDto checkOut(String ownerEmail, Long id);
    OwnerReservationDto cancel(String ownerEmail, Long id);
}
