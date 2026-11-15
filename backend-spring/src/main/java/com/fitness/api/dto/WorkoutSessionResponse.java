package com.fitness.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkoutSessionResponse(UUID id, UUID memberId, Instant startedAt, Instant completedAt,
		TrainingDayData plannedDay, List<WorkoutSetResponse> sets) {
}
