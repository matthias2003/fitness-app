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

    public Task<List<PlanExercise>> FindDayExercises(Guid planId, int dayNo)
    {
        return db.PlanExercises
            .Include(planExercise => planExercise.Exercise)
            .Where(planExercise => planExercise.PlanId == planId && planExercise.DayNo == dayNo)
            .ToListAsync();
    }

    public Task<List<PlanExercise>> FindExercisesOfPlans(List<Guid> planIds)
    {
        return db.PlanExercises
            .Where(planExercise => planIds.Contains(planExercise.PlanId))
            .ToListAsync();
    }
}
