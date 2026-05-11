package com.b4code.backend.modules.staff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

<<<<<<< HEAD
=======
    // ADD THIS FIELD
>>>>>>> origin/dev
    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

<<<<<<< HEAD
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
=======
    @Column(nullable = false)
    private java.math.BigDecimal price;
>>>>>>> origin/dev

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_item_images", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();
}