package com.fitness.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.dto.BuyMembershipRequest;
import com.fitness.api.dto.MembershipResponse;
import com.fitness.api.dto.MembershipTypeResponse;
import com.fitness.api.model.Membership;
import com.fitness.api.model.MembershipType;
import com.fitness.api.model.User;
import com.fitness.api.repository.MembershipRepository;
import com.fitness.api.repository.MembershipTypeRepository;
import com.fitness.api.repository.UserRepository;

@Service
public class MembershipService {

	private final MembershipTypeRepository membershipTypeRepository;

	private final MembershipRepository membershipRepository;

	private final UserRepository userRepository;

	public MembershipService(MembershipTypeRepository membershipTypeRepository,
			MembershipRepository membershipRepository, UserRepository userRepository) {
		this.membershipTypeRepository = membershipTypeRepository;
		this.membershipRepository = membershipRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<MembershipTypeResponse> getTypes() {
		Sort byDuration = Sort.by("durationDays");

		return membershipTypeRepository.findAll(byDuration)
			.stream()
			.map(MembershipTypeResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public MembershipResponse getCurrentMembership(UUID memberId) {
		LocalDate today = LocalDate.now();

		Membership membership = findActiveMembership(memberId, today)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active membership"));

		return MembershipResponse.from(membership);
	}

	@Transactional
	public MembershipResponse buy(UUID memberId, BuyMembershipRequest request) {
		User member = userRepository.findById(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));

		MembershipType type = membershipTypeRepository.findById(request.typeId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership type not found"));

		LocalDate today = LocalDate.now();
		Optional<Membership> activeMembership = findActiveMembership(memberId, today);

		if (activeMembership.isPresent()) {
			Membership membership = activeMembership.get();
			membership.extend(type);

			return MembershipResponse.from(membership);
		}

		LocalDate endDate = today.plusDays(type.getDurationDays() - 1);
		Membership membership = new Membership(member, type, today, endDate);
		Membership savedMembership = membershipRepository.save(membership);

		return MembershipResponse.from(savedMembership);
	}

	private Optional<Membership> findActiveMembership(UUID memberId, LocalDate today) {
		return membershipRepository.findFirstByMemberIdOrderByEndDateDesc(memberId)
			.filter((membership) -> membership.isActiveOn(today));
	}

}
