package com.b4code.backend.models.messaging;

import com.b4code.backend.models.Property;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_quick_replies", schema = "owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffQuickReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
