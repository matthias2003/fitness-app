package com.fitness.api.dto;

import java.util.List;

public record TrainingDayData(int dayNo, String name, List<PlanExerciseData> exercises) {
}
