package com.b4code.backend.service;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCronService {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Runs every day at 08:00 AM to send reminders.
     * Uses a cron expression: second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        log.info("[CRON] Starting daily booking reminders job...");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate checkOutDay = today.plusDays(1);

        List<Booking> allBookings = bookingRepository.findAll();

        for (Booking booking : allBookings) {
            // Only remind for CONFIRMED or PENDING active bookings
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED || booking.getStatus() == Booking.BookingStatus.COMPLETED) {
                continue;
            }

            User guest = userRepository.findByEmailAndDeletedFalse(booking.getGuestEmail()).orElse(null);
            if (guest == null) continue;

            // 1. Check-in day reminder
            if (booking.getCheckIn().equals(today)) {
                notificationService.createNotification(guest, 
                    "Check-in Day!", 
                    "Welcome! Your check-in at " + booking.getProperty().getName() + " is today. Please have your confirmation code ready: " + booking.getConfirmationCode());
            }

            // 2. Upcoming Stay Reminder (1 day before check-in)
            if (booking.getCheckIn().equals(tomorrow)) {
                String paymentNote = booking.getPaymentMethod() == Booking.PaymentMethod.PAY_AT_PROPERTY 
                    ? " Please be ready to pay " + booking.getTotalAmount() + " upon arrival." : "";
                    
                notificationService.createNotification(guest, 
                    "Upcoming Stay Tomorrow!", 
                    "Reminder: Your stay at " + booking.getProperty().getName() + " starts tomorrow!" + paymentNote);
            }

            // 3. Check-out reminder (1 day before check-out)
            if (booking.getCheckOut().equals(checkOutDay)) {
                notificationService.createNotification(guest, 
                    "Check-out Reminder", 
                    "We hope you are enjoying your stay at " + booking.getProperty().getName() + ". Please note your check-out is tomorrow.");
            }
        }
        
        log.info("[CRON] Finished daily booking reminders job.");
    }
}
