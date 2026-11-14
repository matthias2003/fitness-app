using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Repositories;

namespace Fitness.Training.Api.Services;

public class ExerciseService(ExerciseRepository exerciseRepository)
{
    public async Task<List<ExerciseResponse>> GetExercises()
    {
        var exercises = await exerciseRepository.FindAll();

        return exercises
            .Select(ExerciseResponse.From)
            .ToList();
    }
}
