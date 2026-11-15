package com.fitness.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PersonalRecordResponse(Integer exerciseId, String exerciseName, String muscleGroup,
		BigDecimal maxWeight, Instant achievedAt) {
}
