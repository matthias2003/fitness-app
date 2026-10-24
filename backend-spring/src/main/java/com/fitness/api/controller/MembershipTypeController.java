package com.fitness.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.MembershipTypeResponse;
import com.fitness.api.service.MembershipService;

@RestController
@RequestMapping("/api/membership-types")
public class MembershipTypeController {

	private final MembershipService membershipService;

	public MembershipTypeController(MembershipService membershipService) {
		this.membershipService = membershipService;
	}

	@GetMapping
	public List<MembershipTypeResponse> getTypes() {
		return membershipService.getTypes();
	}

}
