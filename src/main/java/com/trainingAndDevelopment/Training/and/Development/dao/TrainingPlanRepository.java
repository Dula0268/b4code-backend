package com.trainingAndDevelopment.Training.and.Development.dao;

import com.trainingAndDevelopment.Training.and.Development.models.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    TrainingPlan findByTrainingCode(String trainingCode);
}

