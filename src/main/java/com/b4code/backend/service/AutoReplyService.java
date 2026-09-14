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
import java.util.stream.Collectors;

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
    public void evaluateAndReply(Booking booking, String incomingMessage, String originalTargetRole) {
        if (booking == null || booking.getProperty() == null) {
            return;
        }

        Long propertyId = booking.getProperty().getId();
        List<AutoReplyRule> rules = autoReplyRuleRepository.findByPropertyIdAndIsActiveTrue(propertyId)
            .stream()
            .filter(r -> originalTargetRole.equalsIgnoreCase(r.getTargetRole()) || (r.getTargetRole() == null && "STAFF".equalsIgnoreCase(originalTargetRole)))
            .collect(Collectors.toList());

        String messageLower = incomingMessage.toLowerCase();

        for (AutoReplyRule rule : rules) {
            if (messageLower.contains(rule.getKeyword().toLowerCase())) {
                log.info("Auto-reply triggered for booking {} due to keyword '{}'", booking.getId(), rule.getKeyword());
                
                try {
                    // Send reply using the same role the guest targeted
                    String replyRole = ("OWNER".equals(originalTargetRole)) ? "OWNER" : "STAFF";
                    bookingMessageService.sendMessage(
                        booking.getId().toString(),
                        "system@b4code.com", // System user for auto replies
                        replyRole,
                        rule.getReplyMessage(),
                        "GUEST"
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
