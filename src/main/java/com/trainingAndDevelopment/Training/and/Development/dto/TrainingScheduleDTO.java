package com.trainingAndDevelopment.Training.and.Development.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trainingAndDevelopment.Training.and.Development.models.TrainingSchedule;
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
public class TrainingScheduleDTO {

    private Long id;
    private String trainingCode;
    private String type;
    private String coveringScope;
    private String trainingDetails;
    private LocalDateTime startDateAndTime;
    private LocalDateTime endDateAndTime;
    private String locations;
    private String status;
    private Integer maxAttendeesLimit;
    private Double cost;
    private String specialRemarks;

    @JsonIgnore
    public TrainingSchedule getEntity() {
        TrainingSchedule trainingSchedule = new TrainingSchedule();
        BeanUtils.copyProperties(this, trainingSchedule);
        return trainingSchedule;
    }
}
