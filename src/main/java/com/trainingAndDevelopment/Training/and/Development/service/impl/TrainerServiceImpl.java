package com.trainingAndDevelopment.Training.and.Development.service.impl;

import com.trainingAndDevelopment.Training.and.Development.dao.TrainerRepository;
import com.trainingAndDevelopment.Training.and.Development.dto.TrainerDTO;
import com.trainingAndDevelopment.Training.and.Development.dto.TrainingScheduleDTO;
import com.trainingAndDevelopment.Training.and.Development.models.Trainer;
import com.trainingAndDevelopment.Training.and.Development.models.TrainingSchedule;
import com.trainingAndDevelopment.Training.and.Development.service.TrainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerServiceImpl(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public TrainerDTO update(TrainerDTO trainerDTO) {
        TrainerDTO dto = null;
        Trainer existing = trainerRepository.findByEmail(trainerDTO.getEmail());
        if (existing != null) {
            trainerDTO.setId(existing.getId());
        }
        try {
            dto = trainerRepository.save(trainerDTO.getEntity()).getDTO();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }

    @Override
    public List<TrainerDTO> saveAll(List<TrainerDTO> list) {
        try {
            List<Trainer> all = trainerRepository.saveAll(list.stream().map(TrainerDTO::getEntity).toList());
            return all.stream().map(Trainer::getDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TrainerDTO create(TrainerDTO trainerDTO) {
        return trainerRepository.save(trainerDTO.getEntity()).getDTO();
    }

    @Override
    public TrainerDTO findById(Long id) {
        TrainerDTO trainerDTO = null;
        try {
            Trainer trainer = trainerRepository.findById(id).orElse(null);
            if (trainer != null) {
                trainerDTO = trainer.getDTO();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainerDTO;
    }

    @Override
    public List<TrainerDTO> findAll() {
        try {
            List<Trainer> all = trainerRepository.findAll();
            return all.stream().map(Trainer::getDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}