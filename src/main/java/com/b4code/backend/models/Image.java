package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images", schema = "owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ImageType type; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = true)
    private Property property;
}
