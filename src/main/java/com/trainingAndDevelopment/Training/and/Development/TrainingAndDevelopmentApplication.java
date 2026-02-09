package com.trainingAndDevelopment.Training.and.Development;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({"com.trainingAndDevelopment"})
@Slf4j
@EnableScheduling
public class TrainingAndDevelopmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingAndDevelopmentApplication.class, args);
	}

}
