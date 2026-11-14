using Fitness.Training.Api.Data;
using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage;

namespace Fitness.Training.Api.Repositories;

public class TrainingPlanRepository(TrainingDbContext db)
{
    public Task<IDbContextTransaction> BeginTransaction()
    {
        return db.Database.BeginTransactionAsync();
    }

    public async Task AddPlan(TrainingPlan plan)
    {
        db.TrainingPlans.Add(plan);
        await db.SaveChangesAsync();
    }

    public async Task AddExercises(List<PlanExercise> planExercises)
    {
        db.PlanExercises.AddRange(planExercises);
        await db.SaveChangesAsync();
    }

    public Task<TrainingPlan?> FindById(Guid id)
    {
        return db.TrainingPlans
            .Include(plan => plan.Exercises)
            .ThenInclude(planExercise => planExercise.Exercise)
            .FirstOrDefaultAsync(plan => plan.Id == id);
    }
}
