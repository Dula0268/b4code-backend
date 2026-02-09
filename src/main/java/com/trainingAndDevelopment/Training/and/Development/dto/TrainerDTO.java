package com.trainingAndDevelopment.Training.and.Development.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trainingAndDevelopment.Training.and.Development.models.Trainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainerDTO {
    private Long id;
    private String name;
    private String expertise;
    private String email;
    private String mobileNumber;

    @JsonIgnore
    public Trainer getEntity() {
        Trainer trainer = new Trainer();
        BeanUtils.copyProperties(this, trainer);
        return trainer;
    }

}

