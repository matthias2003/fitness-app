package com.fitness.api.dto;

public record LoginResponse(String token, UserResponse user) {
}
