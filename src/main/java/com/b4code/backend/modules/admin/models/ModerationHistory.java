package com.b4code.backend.modules.admin.models;

import com.b4code.backend.modules.admin.enums.ModerationAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_history")
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

    private Long adminId;
    @Column(columnDefinition = "TEXT")
    private String adminName;          
    @Column(columnDefinition = "TEXT")
    private String adminInitials;      
    @Column(columnDefinition = "TEXT")
    private String adminColor;         

    @Column(columnDefinition = "TEXT")
    private String outcome;            
    @Column(nullable = false)
    private LocalDateTime resolvedAt;
}
