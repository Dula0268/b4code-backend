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

    @Column(nullable = false)
    private String caseId;             

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationAction actionTaken;

    private Long adminId;
    private String adminName;          
    private String adminInitials;      
    private String adminColor;         

    private String outcome;            
    @Column(nullable = false)
    private LocalDateTime resolvedAt;
}
