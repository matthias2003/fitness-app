package com.fitness.api.dto;

import jakarta.validation.constraints.NotNull;

import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;

public record CreateTrainingPlanRequest(

		@NotNull(message = "Goal is required")
		Goal goal,

		@NotNull(message = "Level is required")
		Level level

) {
}
