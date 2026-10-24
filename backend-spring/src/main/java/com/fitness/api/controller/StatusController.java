package com.fitness.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.SystemStatus;
import com.fitness.api.service.StatusService;

@RestController
@RequestMapping("/api/status")
public class StatusController {

	private final StatusService statusService;

	public StatusController(StatusService statusService) {
		this.statusService = statusService;
	}

	@GetMapping
	public SystemStatus getStatus() {
		return statusService.getStatus();
	}

}
