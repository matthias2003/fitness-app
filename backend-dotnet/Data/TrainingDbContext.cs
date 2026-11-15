using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace Fitness.Training.Api.Data;

public class TrainingDbContext(DbContextOptions<TrainingDbContext> options) : DbContext(options)
{
    public DbSet<Exercise> Exercises => Set<Exercise>();

    public DbSet<TrainingPlan> TrainingPlans => Set<TrainingPlan>();

    public DbSet<PlanExercise> PlanExercises => Set<PlanExercise>();

    public DbSet<WorkoutSession> WorkoutSessions => Set<WorkoutSession>();

    public DbSet<WorkoutSet> WorkoutSets => Set<WorkoutSet>();

    public DbSet<PersonalRecord> PersonalRecords => Set<PersonalRecord>();

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

        modelBuilder.Entity<WorkoutSession>()
            .HasMany(session => session.Sets)
            .WithOne()
            .HasForeignKey(set => set.SessionId);

        modelBuilder.Entity<WorkoutSet>()
            .Property(set => set.Weight)
            .HasPrecision(5, 2);

        modelBuilder.Entity<PersonalRecord>()
            .HasKey(record => new { record.MemberId, record.ExerciseId });

        modelBuilder.Entity<PersonalRecord>()
            .Property(record => record.MaxWeight)
            .HasPrecision(5, 2);
    }
}
