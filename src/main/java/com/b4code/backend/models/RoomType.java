package com.b4code.backend.models;

import com.b4code.backend.models.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "room_types", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomCategory roomCategory;

    @Column(nullable = false)
    private Integer maxOccupancy;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer maxChildren = 0;

    @Enumerated(EnumType.STRING)
    private BedType bedType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = false, columnDefinition = "integer default 3")
    @Builder.Default
    private Integer inventory = 3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'AVAILABLE'")
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(length = 255)
    private String roomNumbers;

}

