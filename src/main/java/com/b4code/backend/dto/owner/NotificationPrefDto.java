package com.b4code.backend.dto.owner;

import com.b4code.backend.models.NotificationPref;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPrefDto {
    private Boolean emailBooking;
    private Boolean emailCancellation;
    private Boolean emailReview;
    private Boolean smsBooking;
    private Boolean smsCancellation;

    public static NotificationPrefDto fromEntity(NotificationPref p) {
        return NotificationPrefDto.builder()
                .emailBooking(p.getEmailBooking())
                .emailCancellation(p.getEmailCancellation())
                .emailReview(p.getEmailReview())
                .smsBooking(p.getSmsBooking())
                .smsCancellation(p.getSmsCancellation())
                .build();
    }

    public static NotificationPrefDto defaults() {
        return NotificationPrefDto.builder()
                .emailBooking(true).emailCancellation(true).emailReview(false)
                .smsBooking(false).smsCancellation(false)
                .build();
    }
}
