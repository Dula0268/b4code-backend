package com.b4code.backend.modules.guest.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    private Double latitude;
    private Double longitude;

    private String imageSrc;  // URL to property image
    
    @Column(length = 2000)
    private String description;

    private Double averageRating;
    private Integer reviewCount;

    @Column(nullable = false)
    private Boolean published = false;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;
}