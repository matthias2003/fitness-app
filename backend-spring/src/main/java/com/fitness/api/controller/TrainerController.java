package com.fitness.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.UserResponse;
import com.fitness.api.service.TrainingPlanService;

@RestController
@RequestMapping("/api/trainers/me")
public class TrainerController {

	private final TrainingPlanService trainingPlanService;

	public TrainerController(TrainingPlanService trainingPlanService) {
		this.trainingPlanService = trainingPlanService;
	}

	@GetMapping("/clients")
	public List<UserResponse> getClients(@AuthenticationPrincipal Jwt jwt) {
		UUID trainerId = UUID.fromString(jwt.getSubject());

		return trainingPlanService.getClients(trainerId);
	}

}
