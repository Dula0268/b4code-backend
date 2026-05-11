package com.b4code.backend.modules.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String name;

    // ADD THIS FIELD
    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private java.math.BigDecimal price;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_item_images", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "image_url")
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();
}