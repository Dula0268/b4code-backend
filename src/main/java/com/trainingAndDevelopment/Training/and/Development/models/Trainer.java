package com.trainingAndDevelopment.Training.and.Development.models;


import com.trainingAndDevelopment.Training.and.Development.dto.TrainerDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trainer_details")

public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "expertise")
    private String expertise;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    public TrainerDTO getDTO(){
        TrainerDTO trainer = new TrainerDTO();
        BeanUtils.copyProperties(this, trainer);
        return trainer;
    }
}
