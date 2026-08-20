package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", schema = "admin")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk")
    private User user;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entity;

    private String entityDetail;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
