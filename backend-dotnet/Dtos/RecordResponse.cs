using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record RecordResponse(
    int ExerciseId,
    string ExerciseName,
    string MuscleGroup,
    decimal MaxWeight,
    DateTime AchievedAt)
{
    public static RecordResponse From(PersonalRecord record)
    {
        var exercise = record.Exercise!;

        return new RecordResponse(
            exercise.Id,
            exercise.Name,
            exercise.MuscleGroup,
            record.MaxWeight,
            record.AchievedAt);
    }
}
