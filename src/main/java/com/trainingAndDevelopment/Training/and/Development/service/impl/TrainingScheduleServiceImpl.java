package com.trainingAndDevelopment.Training.and.Development.service.impl;

import com.trainingAndDevelopment.Training.and.Development.dao.TrainingScheduleRepository;
import com.trainingAndDevelopment.Training.and.Development.dto.TrainingScheduleDTO;
import com.trainingAndDevelopment.Training.and.Development.models.TrainingSchedule;
import com.trainingAndDevelopment.Training.and.Development.service.TrainingScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TrainingScheduleServiceImpl implements TrainingScheduleService {

    private final TrainingScheduleRepository trainingScheduleRepository;

    public TrainingScheduleServiceImpl(TrainingScheduleRepository trainingScheduleRepository) {
        this.trainingScheduleRepository = trainingScheduleRepository;
    }

    @Override
    public List<TrainingScheduleDTO> saveAll(List<TrainingScheduleDTO> list) {
        try {
            List<TrainingSchedule> all = trainingScheduleRepository.saveAll(list.stream().map(TrainingScheduleDTO::getEntity).toList());
            return all.stream().map(TrainingSchedule::getDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TrainingScheduleDTO create(TrainingScheduleDTO trainingScheduleDTO) {
        TrainingScheduleDTO dto = null;
        try {
                dto = trainingScheduleRepository.save(trainingScheduleDTO.getEntity()).getDTO();
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
        return dto;
    }

    @Override
    public TrainingScheduleDTO findById(Long key) {
        TrainingScheduleDTO trainingScheduleDTO = null;
        try {
            TrainingSchedule trainingSchedule = trainingScheduleRepository.findById(key).orElse(null);
            if (trainingSchedule != null) {
                trainingScheduleDTO = trainingSchedule.getDTO();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainingScheduleDTO;
    }

    @Override
    public List<TrainingScheduleDTO> findAll() {
        try{
            List<TrainingSchedule> all = trainingScheduleRepository.findAll();
            return all.stream().map(TrainingSchedule::getDTO).toList();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public TrainingScheduleDTO update(TrainingScheduleDTO trainingScheduleDTO) {
        TrainingScheduleDTO dto = null;
        log.info("schedule update dto : {}", trainingScheduleDTO);
        TrainingSchedule trainingSchedule = trainingScheduleRepository.findByTrainingCode(trainingScheduleDTO.getTrainingCode());
        if (trainingSchedule != null) {
            trainingScheduleDTO.setId(trainingSchedule.getId());
        }
        try {
                dto = trainingScheduleRepository.save(trainingScheduleDTO.getEntity()).getDTO();
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
        return dto;
    }
}
