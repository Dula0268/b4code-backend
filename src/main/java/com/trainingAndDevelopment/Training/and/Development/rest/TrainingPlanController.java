package com.trainingAndDevelopment.Training.and.Development.rest;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainingPlanDTO;
import com.trainingAndDevelopment.Training.and.Development.service.TrainingPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainingPlan")
@CrossOrigin(origins = "*")
@Slf4j
public class TrainingPlanController {
    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @PostMapping("")
    public ResponseEntity<TrainingPlanDTO> createTrainingPlan(@RequestBody TrainingPlanDTO trainingPlanDTO) {
        TrainingPlanDTO savedDTO = this.trainingPlanService.create(trainingPlanDTO);
        return ResponseEntity.ok(savedDTO);
    }

    @GetMapping("")
    public ResponseEntity<List<TrainingPlanDTO>> findAllTrainingPlans() {
        List<TrainingPlanDTO> trainingPlanDTOS = trainingPlanService.findAll();
        return ResponseEntity.ok(trainingPlanDTOS);
    }

    @PutMapping("")
    public ResponseEntity<TrainingPlanDTO> updateTrainingPlan(@RequestBody TrainingPlanDTO trainingPlanDTO) {
        TrainingPlanDTO updatedDTO = trainingPlanService.update(trainingPlanDTO);
        return ResponseEntity.ok(updatedDTO);
    }
}

