package com.fitness.api.dto;

import java.util.UUID;

import com.fitness.api.model.Role;
import com.fitness.api.model.User;

public record UserResponse(UUID id, String email, String firstName, String lastName, Role role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
	}

}
