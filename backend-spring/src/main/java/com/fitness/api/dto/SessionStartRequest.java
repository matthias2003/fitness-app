package com.fitness.api.dto;

import java.util.UUID;

public record SessionStartRequest(UUID memberId, UUID planId) {
}
