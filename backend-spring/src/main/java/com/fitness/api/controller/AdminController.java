package com.fitness.api.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.MemberResponse;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.service.MemberService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final MemberService memberService;

	public AdminController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/members")
	public PageResponse<MemberResponse> getMembers(
			@RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		return memberService.getMembers(page, size);
	}

}
