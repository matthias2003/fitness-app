package com.fitness.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutSetResponse(Long id, Integer exerciseId, String exerciseName, int reps, BigDecimal weight,
		Instant createdAt) {
}
