package com.fitness.api.dto;

import jakarta.validation.constraints.NotNull;

public record BuyMembershipRequest(

		@NotNull(message = "Membership type is required")
		Long typeId

) {
}
