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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingReminderService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    /**
     * Runs every day at 8:00 AM server time to send reminders for bookings checking in tomorrow.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendRemindersForUpcomingBookings() {
        log.info("[BOOKING REMINDER] Starting daily upcoming booking reminder job...");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Booking> upcomingBookings = bookingRepository.findByCheckInAndStatus(tomorrow, Booking.BookingStatus.CONFIRMED);
        
        if (upcomingBookings.isEmpty()) {
            log.info("[BOOKING REMINDER] No upcoming confirmed bookings found for {}.", tomorrow);
            return;
        }

        log.info("[BOOKING REMINDER] Found {} upcoming bookings for {}. Sending reminders...", upcomingBookings.size(), tomorrow);
        
        for (Booking booking : upcomingBookings) {
            try {
                emailService.sendUpcomingStayReminder(booking);
            } catch (Exception e) {
                log.error("[BOOKING REMINDER] Failed to send reminder for booking ID {}: {}", booking.getId(), e.getMessage());
            }
        }
        
        log.info("[BOOKING REMINDER] Finished daily upcoming booking reminder job.");
    }
}
