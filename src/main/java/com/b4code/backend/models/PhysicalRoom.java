package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "physical_rooms", schema = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomType roomType;

    @Column(name = "door_number", nullable = false)
    private String doorNumber;

    @Column(name = "status", nullable = false)
    private String status = "AVAILABLE";
}
