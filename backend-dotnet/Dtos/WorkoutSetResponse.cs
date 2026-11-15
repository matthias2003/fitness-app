using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record WorkoutSetResponse(
    int Id,
    int ExerciseId,
    string ExerciseName,
    int Reps,
    decimal Weight,
    DateTime CreatedAt)
{
    public static WorkoutSetResponse From(WorkoutSet set)
    {
        var exercise = set.Exercise!;

        return new WorkoutSetResponse(set.Id, exercise.Id, exercise.Name, set.Reps, set.Weight, set.CreatedAt);
    }
}
