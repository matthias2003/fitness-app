using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace Fitness.Training.Api.Data;

public class TrainingDbContext(DbContextOptions<TrainingDbContext> options) : DbContext(options)
{
    public DbSet<Exercise> Exercises => Set<Exercise>();

    public DbSet<TrainingPlan> TrainingPlans => Set<TrainingPlan>();

    public DbSet<PlanExercise> PlanExercises => Set<PlanExercise>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Exercise>()
            .HasData(ExerciseSeed.Exercises);

        modelBuilder.Entity<TrainingPlan>()
            .Property(plan => plan.Goal)
            .HasConversion<string>();

        modelBuilder.Entity<TrainingPlan>()
            .Property(plan => plan.Level)
            .HasConversion<string>();

        modelBuilder.Entity<TrainingPlan>()
            .HasMany(plan => plan.Exercises)
            .WithOne()
            .HasForeignKey(planExercise => planExercise.PlanId);
    }
}
