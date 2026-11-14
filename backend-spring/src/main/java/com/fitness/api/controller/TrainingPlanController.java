package com.fitness.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.CreateTrainingPlanRequest;
import com.fitness.api.dto.TrainingPlanResponse;
import com.fitness.api.service.TrainingPlanService;

@RestController
@RequestMapping("/api/members")
public class TrainingPlanController {

	private final TrainingPlanService trainingPlanService;

	public TrainingPlanController(TrainingPlanService trainingPlanService) {
		this.trainingPlanService = trainingPlanService;
	}

	@GetMapping("/me/training-plan")
	public TrainingPlanResponse getMyPlan(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return trainingPlanService.getCurrentPlan(memberId);
	}

	@GetMapping("/{memberId}/training-plan")
	public TrainingPlanResponse getMemberPlan(@PathVariable UUID memberId) {
		return trainingPlanService.getMemberPlan(memberId);
	}

	@PostMapping("/{memberId}/training-plan")
	@ResponseStatus(HttpStatus.CREATED)
	public TrainingPlanResponse createPlan(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId,
			@Valid @RequestBody CreateTrainingPlanRequest request) {
		UUID trainerId = UUID.fromString(jwt.getSubject());

		return trainingPlanService.createPlan(memberId, trainerId, request);
	}

}
