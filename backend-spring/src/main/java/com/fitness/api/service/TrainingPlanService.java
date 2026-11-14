package com.fitness.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.CreateTrainingPlanRequest;
import com.fitness.api.dto.TrainingPlanData;
import com.fitness.api.dto.TrainingPlanResponse;
import com.fitness.api.dto.UserResponse;
import com.fitness.api.model.PlanAssignment;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.PlanAssignmentRepository;
import com.fitness.api.repository.UserRepository;

@Service
public class TrainingPlanService {

	private static final String NO_PLAN_MESSAGE = "No training plan yet";

	private final UserRepository userRepository;

	private final PlanAssignmentRepository planAssignmentRepository;

	private final TrainingApiClient trainingApiClient;

	public TrainingPlanService(UserRepository userRepository, PlanAssignmentRepository planAssignmentRepository,
			TrainingApiClient trainingApiClient) {
		this.userRepository = userRepository;
		this.planAssignmentRepository = planAssignmentRepository;
		this.trainingApiClient = trainingApiClient;
	}

	@Transactional
	public TrainingPlanResponse createPlan(UUID memberId, UUID trainerId, CreateTrainingPlanRequest request) {
		User member = findMember(memberId);

		User trainer = userRepository.findById(trainerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));

		TrainingPlanData plan = trainingApiClient.createPlan(memberId, trainerId, request.goal(), request.level());

		PlanAssignment assignment = new PlanAssignment(member, trainer, plan.id());
		PlanAssignment savedAssignment = planAssignmentRepository.save(assignment);

		return TrainingPlanResponse.from(plan, savedAssignment);
	}

	@Transactional(readOnly = true)
	public TrainingPlanResponse getCurrentPlan(UUID memberId) {
		PlanAssignment assignment = planAssignmentRepository.findFirstByMemberIdOrderByAssignedAtDesc(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NO_PLAN_MESSAGE));

		TrainingPlanData plan = trainingApiClient.getPlan(assignment.getTrainingPlanId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NO_PLAN_MESSAGE));

		return TrainingPlanResponse.from(plan, assignment);
	}

	@Transactional(readOnly = true)
	public TrainingPlanResponse getMemberPlan(UUID memberId) {
		findMember(memberId);

		return getCurrentPlan(memberId);
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getClients(UUID trainerId) {
		List<PlanAssignment> assignments = planAssignmentRepository.findByTrainerIdOrderByMemberLastNameAsc(trainerId);

		return assignments.stream()
			.map(PlanAssignment::getMember)
			.distinct()
			.map(UserResponse::from)
			.toList();
	}

	private User findMember(UUID memberId) {
		return userRepository.findById(memberId)
			.filter((user) -> user.getRole() == Role.MEMBER)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
	}

}
