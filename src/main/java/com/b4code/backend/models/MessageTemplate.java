package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_templates", schema = "guest")
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
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    public enum TemplateType {
        PRE_ARRIVAL,
        CHECK_IN,
        WELCOME,
        CHECK_OUT,
        POST_STAY_REVIEW
    }
}
