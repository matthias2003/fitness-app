package com.fitness.api.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.dto.CompleteSummaryResponse;
import com.fitness.api.dto.ExerciseResponse;
import com.fitness.api.dto.LogSetRequest;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.PersonalRecordResponse;
import com.fitness.api.dto.PlanGenerationRequest;
import com.fitness.api.dto.ServiceStatus;
import com.fitness.api.dto.SessionStartRequest;
import com.fitness.api.dto.SessionSummaryResponse;
import com.fitness.api.dto.TrainingApiError;
import com.fitness.api.dto.TrainingPlanData;
import com.fitness.api.dto.WorkoutSessionResponse;
import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;

@Component
public class TrainingApiClient {

	private static final String DEFAULT_ERROR_MESSAGE = "Invalid request";

	private final RestClient restClient;

	public TrainingApiClient(@Value("${training-api.base-url}") String baseUrl,
			@Value("${training-api.key}") String apiKey) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));

		this.restClient = RestClient.builder()
			.requestFactory(requestFactory)
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

	public WorkoutSessionResponse startSession(UUID memberId, UUID planId) {
		SessionStartRequest request = new SessionStartRequest(memberId, planId);

		try {
			return restClient.post()
				.uri("/sessions/start")
				.body(request)
				.retrieve()
				.body(WorkoutSessionResponse.class);
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public Optional<WorkoutSessionResponse> getCurrentSession(UUID memberId) {
		try {
			WorkoutSessionResponse session = restClient.get()
				.uri("/members/{memberId}/sessions/current", memberId)
				.retrieve()
				.body(WorkoutSessionResponse.class);

			return Optional.ofNullable(session);
		}
		catch (HttpClientErrorException.NotFound ex) {
			return Optional.empty();
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public PageResponse<SessionSummaryResponse> getSessions(UUID memberId, int page, int size) {
		ParameterizedTypeReference<PageResponse<SessionSummaryResponse>> pageType =
				new ParameterizedTypeReference<>() {
				};

		try {
			return restClient.get()
				.uri("/members/{memberId}/sessions?page={page}&size={size}", memberId, page, size)
				.retrieve()
				.body(pageType);
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public Optional<WorkoutSessionResponse> getSession(UUID memberId, UUID sessionId) {
		try {
			WorkoutSessionResponse session = restClient.get()
				.uri("/members/{memberId}/sessions/{id}", memberId, sessionId)
				.retrieve()
				.body(WorkoutSessionResponse.class);

			return Optional.ofNullable(session);
		}
		catch (HttpClientErrorException.NotFound ex) {
			return Optional.empty();
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public WorkoutSessionResponse logSet(UUID memberId, UUID sessionId, LogSetRequest request) {
		try {
			return restClient.post()
				.uri("/members/{memberId}/sessions/{id}/sets", memberId, sessionId)
				.body(request)
				.retrieve()
				.body(WorkoutSessionResponse.class);
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public CompleteSummaryResponse completeSession(UUID memberId, UUID sessionId) {
		try {
			return restClient.post()
				.uri("/members/{memberId}/sessions/{id}/complete", memberId, sessionId)
				.retrieve()
				.body(CompleteSummaryResponse.class);
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	public List<PersonalRecordResponse> getRecords(UUID memberId) {
		try {
			PersonalRecordResponse[] records = restClient.get()
				.uri("/members/{memberId}/records", memberId)
				.retrieve()
				.body(PersonalRecordResponse[].class);

			return List.of(records);
		}
		catch (HttpClientErrorException ex) {
			throw clientError(ex);
		}
		catch (RestClientException ex) {
			throw serviceUnavailable();
		}
	}

	private static ResponseStatusException clientError(HttpClientErrorException ex) {
		HttpStatusCode status = ex.getStatusCode();
		boolean badRequest = status.isSameCodeAs(HttpStatus.BAD_REQUEST);
		boolean notFound = status.isSameCodeAs(HttpStatus.NOT_FOUND);
		boolean conflict = status.isSameCodeAs(HttpStatus.CONFLICT);

		if (!badRequest && !notFound && !conflict) {
			return serviceUnavailable();
		}

		String message = errorMessage(ex);

		return new ResponseStatusException(status, message);
	}

	private static String errorMessage(HttpClientErrorException ex) {
		try {
			TrainingApiError error = ex.getResponseBodyAs(TrainingApiError.class);
			boolean hasMessage = error != null && error.message() != null;

			return hasMessage ? error.message() : DEFAULT_ERROR_MESSAGE;
		}
		catch (RuntimeException conversionError) {
			return DEFAULT_ERROR_MESSAGE;
		}
	}

	private static ResponseStatusException serviceUnavailable() {
		return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Training service is unavailable");
	}

}
