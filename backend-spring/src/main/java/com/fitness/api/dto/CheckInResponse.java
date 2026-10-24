package com.fitness.api.dto;

import java.time.Instant;

import com.fitness.api.model.CheckIn;

public record CheckInResponse(Long id, Instant checkedInAt) {

	public static CheckInResponse from(CheckIn checkIn) {
		return new CheckInResponse(checkIn.getId(), checkIn.getCheckedInAt());
	}

}
