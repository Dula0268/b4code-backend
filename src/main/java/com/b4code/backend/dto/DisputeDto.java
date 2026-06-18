package com.b4code.backend.dto;

import com.b4code.backend.models.enums.DisputeStatus;
import com.b4code.backend.models.Dispute;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisputeDto {

    private String id;
    private String disputeId;         
    private String guestName;
    private String propertyName;
    private String reason;            
    private String amount;            
    private String status;            
    private String bookingId;         
    private String stayDates;
    private String cancellationPolicy;
    private Integer daysUntilAutoClose;
    private String internalNote;

    public static DisputeDto fromEntity(Dispute d) {
        return DisputeDto.builder()
                .id(String.valueOf(d.getId()))
                .disputeId(d.getDisputeId())
                .guestName(d.getGuest() != null ? d.getGuest().getFullName() : null)
                .propertyName(d.getProperty() != null ? d.getProperty().getName() : null)
                .reason(d.getReason())
                .amount(d.getCurrency() + " " + String.format("%,.2f", d.getAmount()))
                .status(toLabel(d.getStatus()))
                .bookingId(d.getBooking() != null ? String.valueOf(d.getBooking().getId()) : null)
                .stayDates(d.getStayDates())
                .cancellationPolicy(d.getCancellationPolicy())
                .daysUntilAutoClose(d.getDaysUntilAutoClose())
                .internalNote(d.getInternalNote())
                .build();
    }

    private static String toLabel(DisputeStatus s) {
        return switch (s) {
            case OPEN              -> "Open";
            case EVIDENCE_UPLOADED -> "Evidence Uploaded";
            case DECISION_PENDING  -> "Decision Pending";
            case RESOLVED          -> "Resolved";
        };
    }
}

