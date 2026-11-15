package com.fitness.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LogSetRequest(

		@NotNull(message = "Exercise is required")
		Integer exerciseId,

		@NotNull(message = "Reps are required")
		@Min(value = 1, message = "Reps must be between 1 and 100")
		@Max(value = 100, message = "Reps must be between 1 and 100")
		Integer reps,

		@NotNull(message = "Weight is required")
		@DecimalMin(value = "0", message = "Weight must be between 0 and 500 kg")
		@DecimalMax(value = "500", message = "Weight must be between 0 and 500 kg")
		@Digits(integer = 3, fraction = 2, message = "Weight can have at most 2 decimal places")
		BigDecimal weight

) {
}
