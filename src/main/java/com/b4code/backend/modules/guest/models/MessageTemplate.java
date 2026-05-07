package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String content;  // Supports placeholders: {{guestName}}, {{checkInDate}}, etc.

    @Column(nullable = false)
    private Boolean enabled = true;

    public enum TemplateType {
        PRE_ARRIVAL,      // 24hrs before check-in
        CHECK_IN,         // Day of check-in
        WELCOME,          // After check-in
        CHECK_OUT,        // Day of check-out
        POST_STAY_REVIEW  // 24hrs after check-out
    }
}
