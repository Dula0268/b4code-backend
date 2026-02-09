package com.trainingAndDevelopment.Training.and.Development.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trainingAndDevelopment.Training.and.Development.models.TrainingPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainingPlanDTO {
    private Long id;
    private String trainingCode;
    private String type;
    private String coveringScope;
    private String trainingDetails;
    private LocalDateTime dateAndTime;
    private String locations;
    private String status;
    private Integer maxAttendeesLimit;
    private Double cost;
    private String specialRemarks;

    @JsonIgnore
    public TrainingPlan getEntity() {
        TrainingPlan trainingPlan = new TrainingPlan();
        BeanUtils.copyProperties(this, trainingPlan);
        return trainingPlan;
    }
}

