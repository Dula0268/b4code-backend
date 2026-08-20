package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_messages", schema = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "sender_identifier")
    private String senderIdentifier; // GUEST: order's guestSessionId; STAFF: staff email

    @Column(name = "sender_role", nullable = false)
    private String senderRole; // "GUEST" or "STAFF"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
