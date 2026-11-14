using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record PlanExerciseResponse(int ExerciseId, string Name, string MuscleGroup, int Sets, int Reps)
{
    public static PlanExerciseResponse From(PlanExercise planExercise)
    {
        var exercise = planExercise.Exercise!;

        return new PlanExerciseResponse(
            exercise.Id,
            exercise.Name,
            exercise.MuscleGroup,
            planExercise.Sets,
            planExercise.Reps);
    }
}
