package com.fitness.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckInRequest(

		@NotBlank(message = "QR token is required")
		@Size(max = 100, message = "QR token is too long")
		String token

) {
}
