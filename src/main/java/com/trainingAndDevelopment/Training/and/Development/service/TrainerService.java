package com.trainingAndDevelopment.Training.and.Development.service;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainerDTO;

public interface TrainerService extends SuperService<TrainerDTO, Long> {
    TrainerDTO update(TrainerDTO trainingPlanDTO);
}

