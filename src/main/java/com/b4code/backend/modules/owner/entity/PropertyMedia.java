package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String url;

    private String fileName;
    private String mediaType; // IMAGE, VIDEO
    private Long fileSize;
    private Boolean isPrimary;
    private Integer sortOrder;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
}
