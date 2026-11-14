package com.fitness.api.dto;

import java.util.UUID;

import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;

public record PlanGenerationRequest(UUID memberId, UUID authorId, Goal goal, Level level) {
}
