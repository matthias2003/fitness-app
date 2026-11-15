using Fitness.Training.Api.Data;
using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage;

namespace Fitness.Training.Api.Repositories;

public class WorkoutSessionRepository(TrainingDbContext db)
{
    public Task<IDbContextTransaction> BeginTransaction()
    {
        return db.Database.BeginTransactionAsync();
    }

    public async Task Add(WorkoutSession session)
    {
        db.WorkoutSessions.Add(session);
        await db.SaveChangesAsync();
    }

    public async Task AddSet(WorkoutSet set)
    {
        db.WorkoutSets.Add(set);
        await db.SaveChangesAsync();
    }

    public Task SaveChanges()
    {
        return db.SaveChangesAsync();
    }

    public Task<WorkoutSession?> FindForMember(Guid memberId, Guid id)
    {
        return WithSets()
            .FirstOrDefaultAsync(session => session.Id == id && session.MemberId == memberId);
    }

    public Task<WorkoutSession?> FindOpen(Guid memberId)
    {
        return WithSets()
            .Where(session => session.MemberId == memberId && session.CompletedAt == null)
            .OrderByDescending(session => session.StartedAt)
            .FirstOrDefaultAsync();
    }

    public Task<WorkoutSession?> FindLastCompleted(Guid memberId)
    {
        return WithSets()
            .Where(session => session.MemberId == memberId && session.CompletedAt != null)
            .OrderByDescending(session => session.CompletedAt)
            .FirstOrDefaultAsync();
    }

    public Task<int> CountForPlan(Guid memberId, Guid planId)
    {
        return db.WorkoutSessions
            .CountAsync(session => session.MemberId == memberId && session.PlanId == planId);
    }

    public Task<int> CountCompleted(Guid memberId)
    {
        return db.WorkoutSessions
            .CountAsync(session => session.MemberId == memberId && session.CompletedAt != null);
    }

    public Task<List<WorkoutSession>> FindCompletedPage(Guid memberId, int skip, int take)
    {
        return WithSets()
            .Where(session => session.MemberId == memberId && session.CompletedAt != null)
            .OrderByDescending(session => session.StartedAt)
            .Skip(skip)
            .Take(take)
            .ToListAsync();
    }

    private IQueryable<WorkoutSession> WithSets()
    {
        return db.WorkoutSessions
            .Include(session => session.Sets)
            .ThenInclude(set => set.Exercise);
    }
}
