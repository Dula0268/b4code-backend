package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_ical_sync", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyIcalSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "channel_name", nullable = false, length = 80)
    private String channelName; // e.g., "Airbnb", "Booking.com", "VRBO", "Google Calendar"

    @Column(name = "feed_url", nullable = false, length = 1000)
    private String feedUrl;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "sync_status", length = 30)
    @Builder.Default
    private String syncStatus = "PENDING"; // PENDING, SUCCESS, FAILED

    @Column(name = "sync_error", length = 500)
    private String syncError;

    @Column(name = "events_imported")
    @Builder.Default
    private Integer eventsImported = 0;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
