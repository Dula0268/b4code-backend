package com.trainingAndDevelopment.Training.and.Development.rest;

import com.trainingAndDevelopment.Training.and.Development.dto.TrainerDTO;
import com.trainingAndDevelopment.Training.and.Development.service.TrainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainer")
@CrossOrigin(origins = "*")
@Slf4j
public class TrainerController {
    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping("")
    public ResponseEntity<TrainerDTO> createTrainer(@RequestBody TrainerDTO trainerDTO) {
        TrainerDTO savedDTO = this.trainerService.create(trainerDTO);
        return ResponseEntity.ok(savedDTO);
    }

    @GetMapping("")
    public ResponseEntity<List<TrainerDTO>> findAllTrainers() {
        List<TrainerDTO> trainerDTOS = trainerService.findAll();
        return ResponseEntity.ok(trainerDTOS);
    }

    @PutMapping("")
    public ResponseEntity<TrainerDTO> updateTrainer(@RequestBody TrainerDTO trainerDTO) {
        TrainerDTO updatedDTO = trainerService.update(trainerDTO);
        return ResponseEntity.ok(updatedDTO);
    }
}

