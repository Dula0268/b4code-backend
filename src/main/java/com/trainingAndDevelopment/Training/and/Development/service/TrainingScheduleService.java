package com.trainingAndDevelopment.Training.and.Development.service;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainingScheduleDTO;

public interface TrainingScheduleService extends SuperService<TrainingScheduleDTO, Long> {

    TrainingScheduleDTO update(TrainingScheduleDTO trainingScheduleDTO);
}
