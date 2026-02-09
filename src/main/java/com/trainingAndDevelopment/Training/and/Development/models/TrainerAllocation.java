package com.trainingAndDevelopment.Training.and.Development.models;

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
@Table(name = "trainer_allocation")
public class TrainerAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Column(name = "training_plan_code", nullable = false)
    private String trainingPlanCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
