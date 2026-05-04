package com.b4code.backend.modules.owner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Data
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long propertyId;
    
    private String name;
    private String roomType;
    
    private Integer maxAdults;
    private Integer maxChildren;
    private Integer maxOccupancy;
    
    private BigDecimal baseRate;
    private String currency;
    
    @Column(length = 1000)
    private String description;
    
    private String status;
}
