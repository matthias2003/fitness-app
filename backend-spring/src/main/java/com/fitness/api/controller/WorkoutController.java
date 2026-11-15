package com.fitness.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.CompleteSummaryResponse;
import com.fitness.api.dto.LogSetRequest;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.PersonalRecordResponse;
import com.fitness.api.dto.SessionSummaryResponse;
import com.fitness.api.dto.WorkoutSessionResponse;
import com.fitness.api.service.WorkoutService;

@RestController
@RequestMapping("/api")
public class WorkoutController {

	private final WorkoutService workoutService;

	public WorkoutController(WorkoutService workoutService) {
		this.workoutService = workoutService;
	}

	@PostMapping("/workouts/sessions")
	public WorkoutSessionResponse startSession(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.startSession(memberId);
	}

	@GetMapping("/workouts/sessions/current")
	public WorkoutSessionResponse getCurrentSession(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.getCurrentSession(memberId);
	}

	@GetMapping("/workouts/sessions")
	public PageResponse<SessionSummaryResponse> getHistory(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.getHistory(memberId, page, size);
	}

	@GetMapping("/workouts/sessions/{sessionId}")
	public WorkoutSessionResponse getSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.getSession(memberId, sessionId);
	}

	@PostMapping("/workouts/sessions/{sessionId}/sets")
	@ResponseStatus(HttpStatus.CREATED)
	public WorkoutSessionResponse logSet(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId,
			@Valid @RequestBody LogSetRequest request) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.logSet(memberId, sessionId, request);
	}

	@PostMapping("/workouts/sessions/{sessionId}/complete")
	public CompleteSummaryResponse completeSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.completeSession(memberId, sessionId);
	}

	@GetMapping("/members/me/records")
	public List<PersonalRecordResponse> getRecords(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return workoutService.getRecords(memberId);
	}

}
