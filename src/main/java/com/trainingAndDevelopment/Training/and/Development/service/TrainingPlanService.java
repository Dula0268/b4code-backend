package com.trainingAndDevelopment.Training.and.Development.service;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainingPlanDTO;

public interface TrainingPlanService extends SuperService<TrainingPlanDTO, Long> {
    TrainingPlanDTO update(TrainingPlanDTO trainingPlanDTO);
}

