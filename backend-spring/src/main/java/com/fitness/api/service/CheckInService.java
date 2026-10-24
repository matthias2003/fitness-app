package com.fitness.api.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.dto.CheckInRequest;
import com.fitness.api.dto.CheckInResponse;
import com.fitness.api.dto.CheckInResultResponse;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.model.CheckIn;
import com.fitness.api.model.Membership;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.CheckInRepository;
import com.fitness.api.repository.MembershipRepository;
import com.fitness.api.repository.UserRepository;

@Service
public class CheckInService {

	private final UserRepository userRepository;

	private final MembershipRepository membershipRepository;

	private final CheckInRepository checkInRepository;

	public CheckInService(UserRepository userRepository, MembershipRepository membershipRepository,
			CheckInRepository checkInRepository) {
		this.userRepository = userRepository;
		this.membershipRepository = membershipRepository;
		this.checkInRepository = checkInRepository;
	}

	@Transactional
	public CheckInResultResponse checkIn(CheckInRequest request) {
		String token = request.token().trim();

		User member = userRepository.findByQrTokenAndRole(token, Role.MEMBER)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown QR code"));

		Membership membership = membershipRepository.findFirstByMemberIdOrderByEndDateDesc(member.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Member has no membership"));

		LocalDate today = LocalDate.now();

		if (!membership.isActiveOn(today)) {
			String message = "Membership expired on " + membership.getEndDate();
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
		}

		CheckIn checkIn = new CheckIn(member);
		CheckIn savedCheckIn = checkInRepository.save(checkIn);

		return CheckInResultResponse.from(savedCheckIn, membership);
	}

	@Transactional(readOnly = true)
	public PageResponse<CheckInResponse> getHistory(UUID memberId, int page, int size) {
		Sort newestFirst = Sort.by(Sort.Direction.DESC, "checkedInAt");
		PageRequest pageRequest = PageRequest.of(page, size, newestFirst);

		Page<CheckIn> checkIns = checkInRepository.findByMemberId(memberId, pageRequest);

		return PageResponse.from(checkIns, CheckInResponse::from);
	}

}
