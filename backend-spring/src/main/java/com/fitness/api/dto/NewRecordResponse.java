package com.fitness.api.dto;

import java.math.BigDecimal;

public record NewRecordResponse(Integer exerciseId, String exerciseName, BigDecimal maxWeight,
		BigDecimal previousMaxWeight) {
}
