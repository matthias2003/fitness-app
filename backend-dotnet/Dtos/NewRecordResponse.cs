namespace Fitness.Training.Api.Dtos;

public record NewRecordResponse(int ExerciseId, string ExerciseName, decimal MaxWeight, decimal? PreviousMaxWeight);
