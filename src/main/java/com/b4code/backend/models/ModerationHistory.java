package com.b4code.backend.models;

import com.b4code.backend.models.enums.ModerationAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_history", schema = "admin")
@Getter
@Setter
public class ModerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationAction actionTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_fk")
    private User admin;

    @Column(columnDefinition = "TEXT")
    private String outcome;
    @Column(nullable = false)
    private LocalDateTime resolvedAt;
}
