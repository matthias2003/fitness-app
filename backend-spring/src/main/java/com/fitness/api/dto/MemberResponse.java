package com.fitness.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fitness.api.model.Membership;
import com.fitness.api.model.User;

public record MemberResponse(UUID id, String email, String firstName, String lastName, String membershipType,
		LocalDate membershipEndDate, boolean membershipActive) {

	public static MemberResponse from(User user, Membership latestMembership, LocalDate today) {
		if (latestMembership == null) {
			return new MemberResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), null,
					null, false);
		}

		String typeName = latestMembership.getType().getName();
		LocalDate endDate = latestMembership.getEndDate();
		boolean active = latestMembership.isActiveOn(today);

		return new MemberResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), typeName,
				endDate, active);
	}

}
