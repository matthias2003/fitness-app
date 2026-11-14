using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record PlanResponse(
    Guid Id,
    Guid MemberId,
    Guid AuthorId,
    Goal Goal,
    Level Level,
    string Template,
    DateTime CreatedAt,
    List<PlanDayResponse> Days)
{
    public static PlanResponse From(TrainingPlan plan)
    {
        var days = plan.Exercises
            .GroupBy(planExercise => planExercise.DayNo)
            .OrderBy(day => day.Key)
            .Select(PlanDayResponse.From)
            .ToList();

        return new PlanResponse(
            plan.Id,
            plan.MemberId,
            plan.AuthorId,
            plan.Goal,
            plan.Level,
            plan.Template,
            plan.CreatedAt,
            days);
    }
}
