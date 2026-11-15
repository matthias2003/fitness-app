using Fitness.Training.Api.Data;
using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace Fitness.Training.Api.Repositories;

public class ExerciseRepository(TrainingDbContext db)
{
    public Task<List<Exercise>> FindAll()
    {
        return db.Exercises
            .OrderBy(exercise => exercise.MuscleGroup)
            .ThenBy(exercise => exercise.Name)
            .ToListAsync();
    }

    public Task<bool> Exists(int id)
    {
        return db.Exercises.AnyAsync(exercise => exercise.Id == id);
    }
}
