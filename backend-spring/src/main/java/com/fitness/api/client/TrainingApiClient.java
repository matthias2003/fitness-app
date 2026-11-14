package com.fitness.api.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.dto.ExerciseResponse;
import com.fitness.api.dto.PlanGenerationRequest;
import com.fitness.api.dto.ServiceStatus;
import com.fitness.api.dto.TrainingPlanData;
import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;

@Component
public class TrainingApiClient {

	private final RestClient restClient;

	public TrainingApiClient(@Value("${training-api.base-url}") String baseUrl,
			@Value("${training-api.key}") String apiKey) {
		this.restClient = RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader("X-Internal-Api-Key", apiKey)
			.build();
	}

	public ServiceStatus getStatus() {
		try {
			return restClient.get()
				.uri("/status")
				.retrieve()
				.body(ServiceStatus.class);
		}
		catch (RestClientException ex) {
			return new ServiceStatus("training-api", "DOWN", "UNKNOWN");
		}
	}

	public List<ExerciseResponse> getExercises() {
		try {
			ExerciseResponse[] exercises = restClient.get()
				.uri("/exercises")
				.retrieve()
				.body(ExerciseResponse[].class);

			return List.of(exercises);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public TrainingPlanData createPlan(UUID memberId, UUID authorId, Goal goal, Level level) {
		PlanGenerationRequest request = new PlanGenerationRequest(memberId, authorId, goal, level);

		try {
			return restClient.post()
				.uri("/plans")
				.body(request)
				.retrieve()
				.body(TrainingPlanData.class);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public Optional<TrainingPlanData> getPlan(UUID planId) {
		try {
			TrainingPlanData plan = restClient.get()
				.uri("/plans/{id}", planId)
				.retrieve()
				.body(TrainingPlanData.class);

			return Optional.ofNullable(plan);
		}
		catch (HttpClientErrorException.NotFound ex) {
			return Optional.empty();
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	private static ResponseStatusException serviceUnavailable() {
		return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Training service is unavailable");
	}

}
