package com.trainingAndDevelopment.Training.and.Development.service.impl;

import com.trainingAndDevelopment.Training.and.Development.dao.TrainingPlanRepository;
import com.trainingAndDevelopment.Training.and.Development.dto.TrainingPlanDTO;
import com.trainingAndDevelopment.Training.and.Development.exceptions.CustomException;
import com.trainingAndDevelopment.Training.and.Development.models.TrainingPlan;
import com.trainingAndDevelopment.Training.and.Development.service.TrainingPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {
    private final TrainingPlanRepository trainingPlanRepository;

    public TrainingPlanServiceImpl(TrainingPlanRepository trainingPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
    }

    @Override
    public List<TrainingPlanDTO> saveAll(List<TrainingPlanDTO> list) {
        try {
            List<TrainingPlan> all = trainingPlanRepository.saveAll(list.stream().map(TrainingPlanDTO::getEntity).toList());
            return all.stream().map(TrainingPlan::getDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TrainingPlanDTO create(TrainingPlanDTO trainingPlanDTO) {
        TrainingPlanDTO dto = null;
        try {
            dto = trainingPlanRepository.save(trainingPlanDTO.getEntity()).getDTO();
        } catch (Exception e) {
            throw new CustomException(e.getCause().getMessage());
        }
        return dto;
    }


    @Override
    public TrainingPlanDTO findById(Long key) {
        TrainingPlanDTO trainingPlanDTO = null;
        try {
            TrainingPlan trainingPlan = trainingPlanRepository.findById(key).orElse(null);
            if (trainingPlan != null) {
                trainingPlanDTO = trainingPlan.getDTO();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainingPlanDTO;
    }

    @Override
    public List<TrainingPlanDTO> findAll() {
        try {
            List<TrainingPlan> all = trainingPlanRepository.findAll();
            return all.stream().map(TrainingPlan::getDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TrainingPlanDTO update(TrainingPlanDTO trainingPlanDTO) {
        TrainingPlanDTO dto = null;
        log.info("plan update dto : {}", trainingPlanDTO);
        TrainingPlan trainingPlan = trainingPlanRepository.findByTrainingCode(trainingPlanDTO.getTrainingCode());
        if (trainingPlan != null) {
            trainingPlanDTO.setId(trainingPlan.getId());
        }
        try {
            dto = trainingPlanRepository.save(trainingPlanDTO.getEntity()).getDTO();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dto;
    }

}

