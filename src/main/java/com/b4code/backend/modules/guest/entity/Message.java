package com.b4code.backend.modules.guest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "property_id")
    private Long propertyId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}
