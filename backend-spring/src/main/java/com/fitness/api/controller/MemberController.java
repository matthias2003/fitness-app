package com.fitness.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.BuyMembershipRequest;
import com.fitness.api.dto.CheckInResponse;
import com.fitness.api.dto.MembershipResponse;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.QrTokenResponse;
import com.fitness.api.service.CheckInService;
import com.fitness.api.service.MemberService;
import com.fitness.api.service.MembershipService;

@RestController
@RequestMapping("/api/members/me")
public class MemberController {

	private final MembershipService membershipService;

	private final MemberService memberService;

	private final CheckInService checkInService;

	public MemberController(MembershipService membershipService, MemberService memberService,
			CheckInService checkInService) {
		this.membershipService = membershipService;
		this.memberService = memberService;
		this.checkInService = checkInService;
	}

	@GetMapping("/membership")
	public MembershipResponse getMembership(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return membershipService.getCurrentMembership(memberId);
	}

	@PostMapping("/memberships")
	@ResponseStatus(HttpStatus.CREATED)
	public MembershipResponse buyMembership(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody BuyMembershipRequest request) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return membershipService.buy(memberId, request);
	}

	@GetMapping("/qr")
	public QrTokenResponse getQrToken(@AuthenticationPrincipal Jwt jwt) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return memberService.getQrToken(memberId);
	}

	@GetMapping("/check-ins")
	public PageResponse<CheckInResponse> getCheckIns(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		UUID memberId = UUID.fromString(jwt.getSubject());

		return checkInService.getHistory(memberId, page, size);
	}

}
