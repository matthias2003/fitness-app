package com.fitness.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.CheckInRequest;
import com.fitness.api.dto.CheckInResultResponse;
import com.fitness.api.service.CheckInService;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

	private final CheckInService checkInService;

	public CheckInController(CheckInService checkInService) {
		this.checkInService = checkInService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CheckInResultResponse checkIn(@Valid @RequestBody CheckInRequest request) {
		return checkInService.checkIn(request);
	}

}
