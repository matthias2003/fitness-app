using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record ExerciseResponse(int Id, string Name, string MuscleGroup, string Equipment)
{
    public static ExerciseResponse From(Exercise exercise)
    {
        return new ExerciseResponse(exercise.Id, exercise.Name, exercise.MuscleGroup, exercise.Equipment);
    }
}
