package com.fitness.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;

public record TrainingPlanData(UUID id, UUID memberId, UUID authorId, Goal goal, Level level, String template,
		Instant createdAt, List<TrainingDayData> days) {
}
