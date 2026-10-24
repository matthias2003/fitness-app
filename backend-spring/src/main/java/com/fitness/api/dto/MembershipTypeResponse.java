package com.fitness.api.dto;

import java.math.BigDecimal;

import com.fitness.api.model.MembershipType;

public record MembershipTypeResponse(Long id, String name, int durationDays, BigDecimal price) {

	public static MembershipTypeResponse from(MembershipType type) {
		return new MembershipTypeResponse(type.getId(), type.getName(), type.getDurationDays(), type.getPrice());
	}

}
