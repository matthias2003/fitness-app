package com.fitness.api.dto;

public record AuthSession(String accessToken, String refreshToken, UserResponse user) {
}
