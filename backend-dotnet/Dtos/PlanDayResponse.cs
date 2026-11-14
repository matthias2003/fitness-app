using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record PlanDayResponse(int DayNo, string Name, List<PlanExerciseResponse> Exercises)
{
    public static PlanDayResponse From(IGrouping<int, PlanExercise> day)
    {
        var firstExercise = day.First();

        var exercises = day
            .OrderBy(planExercise => planExercise.Position)
            .Select(PlanExerciseResponse.From)
            .ToList();

        return new PlanDayResponse(day.Key, firstExercise.DayName, exercises);
    }
}
