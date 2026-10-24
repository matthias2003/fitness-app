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

import com.fitness.api.dto.MemberResponse;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.QrTokenResponse;
import com.fitness.api.model.Membership;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.MembershipRepository;
import com.fitness.api.repository.UserRepository;

@Service
public class MemberService {

	private final UserRepository userRepository;

	private final MembershipRepository membershipRepository;

	public MemberService(UserRepository userRepository, MembershipRepository membershipRepository) {
		this.userRepository = userRepository;
		this.membershipRepository = membershipRepository;
	}

	@Transactional(readOnly = true)
	public QrTokenResponse getQrToken(UUID memberId) {
		User member = userRepository.findById(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));

		return new QrTokenResponse(member.getQrToken());
	}

	@Transactional(readOnly = true)
	public PageResponse<MemberResponse> getMembers(int page, int size) {
		Sort byName = Sort.by("lastName", "firstName", "email");
		PageRequest pageRequest = PageRequest.of(page, size, byName);

		Page<User> members = userRepository.findByRole(Role.MEMBER, pageRequest);
		LocalDate today = LocalDate.now();

		return PageResponse.from(members, (member) -> toMemberResponse(member, today));
	}

	private MemberResponse toMemberResponse(User member, LocalDate today) {
		Membership latestMembership = membershipRepository.findFirstByMemberIdOrderByEndDateDesc(member.getId())
			.orElse(null);

		return MemberResponse.from(member, latestMembership, today);
	}

}
