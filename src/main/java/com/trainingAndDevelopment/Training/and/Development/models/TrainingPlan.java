package com.trainingAndDevelopment.Training.and.Development.models;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainingPlanDTO;
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
@Table(name = "training_plan")

public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "training_code", nullable = false, unique = true)
    private String trainingCode;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "covering_scope")
    private String coveringScope;

    @Column(name = "training_details")
    private String trainingDetails;

    @Column(name = "date_and_time")
    private LocalDateTime dateAndTime;

    @Column(name = "locations")
    private String locations;

    @Column(name = "status")
    private String status;

    @Column(name = "max_attendees_limit")
    private Integer maxAttendeesLimit;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "special_remarks")
    private String specialRemarks;

    public TrainingPlanDTO getDTO(){
        TrainingPlanDTO trainingPlan = new TrainingPlanDTO();
        BeanUtils.copyProperties(this, trainingPlan);
        return trainingPlan;
    }

}
