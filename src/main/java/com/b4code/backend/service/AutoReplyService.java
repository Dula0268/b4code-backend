package com.b4code.backend.service;

import com.b4code.backend.models.Booking;
import com.b4code.backend.models.messaging.AutoReplyRule;
import com.b4code.backend.repository.AutoReplyRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AutoReplyService {

    private final AutoReplyRuleRepository autoReplyRuleRepository;
    private final BookingMessageService bookingMessageService;

    public AutoReplyService(AutoReplyRuleRepository autoReplyRuleRepository, @Lazy BookingMessageService bookingMessageService) {
        this.autoReplyRuleRepository = autoReplyRuleRepository;
        this.bookingMessageService = bookingMessageService;
    }

    @Async
    public void evaluateAndReply(Booking booking, String incomingMessage) {
        if (booking == null || booking.getProperty() == null) {
            return;
        }

        Long propertyId = booking.getProperty().getId();
        List<AutoReplyRule> rules = autoReplyRuleRepository.findByPropertyIdAndIsActiveTrue(propertyId);

        String messageLower = incomingMessage.toLowerCase();

        for (AutoReplyRule rule : rules) {
            if (messageLower.contains(rule.getKeyword().toLowerCase())) {
                log.info("Auto-reply triggered for booking {} due to keyword '{}'", booking.getId(), rule.getKeyword());
                
                try {
                    // Send reply as STAFF
                    bookingMessageService.sendMessage(
                        booking.getId().toString(),
                        "system@b4code.com", // System user for auto replies
                        "STAFF",
                        rule.getReplyMessage()
                    );
                } catch (Exception e) {
                    log.error("Failed to send auto-reply for booking {}", booking.getId(), e);
                }
                
                // Only trigger the first matched rule to avoid spam
                break;
            }
        }
    }
}
