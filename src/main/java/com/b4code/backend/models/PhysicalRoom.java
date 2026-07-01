package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "physical_rooms", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(length = 255)
    private String doorNumber;

    @Column(length = 255, columnDefinition = "varchar(255) default 'CLEAN'")
    @Builder.Default
    private String status = "CLEAN";
}
