using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record SessionResponse(
    Guid Id,
    Guid MemberId,
    DateTime StartedAt,
    DateTime? CompletedAt,
    PlanDayResponse? PlannedDay,
    List<WorkoutSetResponse> Sets)
{
    public static SessionResponse From(WorkoutSession session, PlanDayResponse? plannedDay)
    {
        var sets = session.Sets
            .OrderBy(set => set.CreatedAt)
            .ThenBy(set => set.Id)
            .Select(WorkoutSetResponse.From)
            .ToList();

        return new SessionResponse(
            session.Id,
            session.MemberId,
            session.StartedAt,
            session.CompletedAt,
            plannedDay,
            sets);
    }
}
