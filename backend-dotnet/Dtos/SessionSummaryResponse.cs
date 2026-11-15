using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record SessionSummaryResponse(
    Guid Id,
    DateTime StartedAt,
    DateTime? CompletedAt,
    string? DayName,
    int SetsCount,
    decimal TotalVolume)
{
    public static SessionSummaryResponse From(WorkoutSession session, string? dayName)
    {
        var totalVolume = session.TotalVolume();

        return new SessionSummaryResponse(
            session.Id,
            session.StartedAt,
            session.CompletedAt,
            dayName,
            session.Sets.Count,
            totalVolume);
    }
}
