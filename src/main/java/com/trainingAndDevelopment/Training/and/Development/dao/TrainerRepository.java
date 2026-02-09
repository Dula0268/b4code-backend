package com.trainingAndDevelopment.Training.and.Development.dao;

import com.trainingAndDevelopment.Training.and.Development.models.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Trainer findByEmail(String email);
}
