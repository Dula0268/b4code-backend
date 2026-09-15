package com.b4code.backend.service;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.models.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NightAuditService {

    private final BookingRepository bookingRepository;

    /**
     * Runs every day at 12:01 AM server time.
     * Finds all pending/confirmed bookings where the check-in date is before today,
     * and the guest has not been marked as 'lateArrivalAllowed'.
     * Marks them as NO_SHOW.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void performNightAudit() {
        log.info("Starting automated Night Audit for No-Shows...");
        
        LocalDate today = LocalDate.now();
        List<Booking> pastDueBookings = bookingRepository.findPastDueBookings(today);
        
        if (pastDueBookings.isEmpty()) {
            log.info("No past-due bookings found. Night Audit complete.");
            return;
        }

        for (Booking booking : pastDueBookings) {
            log.info("Marking booking ID {} as NO_SHOW (Check-in was {})", booking.getId(), booking.getCheckIn());
            booking.setStatus(Booking.BookingStatus.NO_SHOW);
            booking.setCancellationReason("Automated Night Audit: No-Show");
        }
        
        bookingRepository.saveAll(pastDueBookings);
        log.info("Night Audit complete. Marked {} bookings as NO_SHOW.", pastDueBookings.size());
    }
}
