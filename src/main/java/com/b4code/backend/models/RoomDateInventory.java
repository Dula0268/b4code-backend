package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_date_inventory", schema = "guest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDateInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer bookedQuantity = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceOverride;
}
