package com.fitness.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.CompleteSummaryResponse;
import com.fitness.api.dto.LogSetRequest;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.PersonalRecordResponse;
import com.fitness.api.dto.SessionSummaryResponse;
import com.fitness.api.dto.WorkoutSessionResponse;
import com.fitness.api.model.PlanAssignment;
import com.fitness.api.repository.PlanAssignmentRepository;

@Service
public class WorkoutService {

	private final PlanAssignmentRepository planAssignmentRepository;

	private final TrainingApiClient trainingApiClient;

	public WorkoutService(PlanAssignmentRepository planAssignmentRepository, TrainingApiClient trainingApiClient) {
		this.planAssignmentRepository = planAssignmentRepository;
		this.trainingApiClient = trainingApiClient;
	}

	@Transactional(readOnly = true)
	public WorkoutSessionResponse startSession(UUID memberId) {
		UUID planId = planAssignmentRepository.findFirstByMemberIdOrderByAssignedAtDesc(memberId)
			.map(PlanAssignment::getTrainingPlanId)
			.orElse(null);

		return trainingApiClient.startSession(memberId, planId);
	}

	public WorkoutSessionResponse getCurrentSession(UUID memberId) {
		return trainingApiClient.getCurrentSession(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active workout"));
	}

	public PageResponse<SessionSummaryResponse> getHistory(UUID memberId, int page, int size) {
		return trainingApiClient.getSessions(memberId, page, size);
	}

	public WorkoutSessionResponse getSession(UUID memberId, UUID sessionId) {
		return trainingApiClient.getSession(memberId, sessionId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
	}

	public WorkoutSessionResponse logSet(UUID memberId, UUID sessionId, LogSetRequest request) {
		return trainingApiClient.logSet(memberId, sessionId, request);
	}

	public CompleteSummaryResponse completeSession(UUID memberId, UUID sessionId) {
		return trainingApiClient.completeSession(memberId, sessionId);
	}

	public List<PersonalRecordResponse> getRecords(UUID memberId) {
		return trainingApiClient.getRecords(memberId);
	}

}
