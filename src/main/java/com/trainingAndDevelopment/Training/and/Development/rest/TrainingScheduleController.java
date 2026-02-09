package com.trainingAndDevelopment.Training.and.Development.rest;

import com.trainingAndDevelopment.Training.and.Development.dao.TrainingScheduleRepository;
import com.trainingAndDevelopment.Training.and.Development.dto.TrainingScheduleDTO;
import com.trainingAndDevelopment.Training.and.Development.service.TrainingScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainingSchedule")
@CrossOrigin(origins = "*")
@Slf4j
public class TrainingScheduleController {

    private final TrainingScheduleService trainingScheduleService;

    public TrainingScheduleController(TrainingScheduleService trainingScheduleService) {
        this.trainingScheduleService = trainingScheduleService;
    }

    @PostMapping(value = "")
    public ResponseEntity<TrainingScheduleDTO> createTrainingSchedule(@RequestBody TrainingScheduleDTO trainingScheduleDTO) {

        TrainingScheduleDTO savedDTO = this.trainingScheduleService.create(trainingScheduleDTO);
        return ResponseEntity.ok(savedDTO);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<TrainingScheduleDTO>> findAllTrainingSchedules() {
        List<TrainingScheduleDTO> trainingScheduleDTOS = trainingScheduleService.findAll();
        return ResponseEntity.ok(trainingScheduleDTOS);
    }

    @PutMapping(value = "")
    public ResponseEntity<TrainingScheduleDTO> updateTrainingSchedule(@RequestBody TrainingScheduleDTO trainingScheduleDTO) {
        TrainingScheduleDTO updatedDTO = trainingScheduleService.update(trainingScheduleDTO);
        return ResponseEntity.ok(updatedDTO);
    }

}
