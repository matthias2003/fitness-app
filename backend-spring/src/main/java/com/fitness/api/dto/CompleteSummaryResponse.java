package com.fitness.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompleteSummaryResponse(UUID sessionId, Instant completedAt, int setsCount, BigDecimal totalVolume,
		BigDecimal previousVolume, List<NewRecordResponse> newRecords) {
}
