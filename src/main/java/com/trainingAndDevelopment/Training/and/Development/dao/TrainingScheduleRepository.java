package com.trainingAndDevelopment.Training.and.Development.dao;

import com.trainingAndDevelopment.Training.and.Development.models.TrainingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@Repository
public interface TrainingScheduleRepository extends JpaRepository<TrainingSchedule, Long> {

    TrainingSchedule findByTrainingCode(String trainingCode);
}
