package com.fitness.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.ExerciseResponse;

@Service
public class ExerciseService {

	private final TrainingApiClient trainingApiClient;

	public ExerciseService(TrainingApiClient trainingApiClient) {
		this.trainingApiClient = trainingApiClient;
	}

	public List<ExerciseResponse> getExercises() {
		return trainingApiClient.getExercises();
	}

}
