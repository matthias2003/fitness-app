package com.fitness.api.dto;

import java.time.LocalDate;

import com.fitness.api.model.Membership;

public record MembershipResponse(Long id, String typeName, LocalDate startDate, LocalDate endDate) {

	public static MembershipResponse from(Membership membership) {
		String typeName = membership.getType().getName();

		return new MembershipResponse(membership.getId(), typeName, membership.getStartDate(), membership.getEndDate());
	}

}
