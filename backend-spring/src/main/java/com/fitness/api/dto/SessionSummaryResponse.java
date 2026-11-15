package com.fitness.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionSummaryResponse(UUID id, Instant startedAt, Instant completedAt, String dayName, int setsCount,
		BigDecimal totalVolume) {
}
