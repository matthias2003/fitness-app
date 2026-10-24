package com.fitness.api.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.fitness.api.model.CheckIn;
import com.fitness.api.model.Membership;
import com.fitness.api.model.User;

public record CheckInResultResponse(String firstName, String lastName, String membershipType,
		LocalDate membershipEndDate, Instant checkedInAt) {

	public static CheckInResultResponse from(CheckIn checkIn, Membership membership) {
		User member = checkIn.getMember();
		String typeName = membership.getType().getName();

		return new CheckInResultResponse(member.getFirstName(), member.getLastName(), typeName,
				membership.getEndDate(), checkIn.getCheckedInAt());
	}

}
