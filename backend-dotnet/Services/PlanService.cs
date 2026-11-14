using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Models;
using Fitness.Training.Api.Repositories;

namespace Fitness.Training.Api.Services;

public class PlanService(TrainingPlanRepository planRepository, ExerciseRepository exerciseRepository)
{
    public async Task<PlanResponse> CreatePlan(CreatePlanRequest request)
    {
        var template = PlanGenerator.GetTemplateName(request.Level);

        var plan = new TrainingPlan
        {
            Id = Guid.NewGuid(),
            MemberId = request.MemberId,
            AuthorId = request.AuthorId,
            Goal = request.Goal,
            Level = request.Level,
            Template = template,
            CreatedAt = DateTime.UtcNow,
        };

        var exercises = await exerciseRepository.FindAll();
        var planExercises = PlanGenerator.BuildExercises(plan.Id, request.Goal, request.Level, exercises);

        await using var transaction = await planRepository.BeginTransaction();

        await planRepository.AddPlan(plan);
        await planRepository.AddExercises(planExercises);

        await transaction.CommitAsync();

        var savedPlan = await planRepository.FindById(plan.Id);

        return PlanResponse.From(savedPlan!);
    }

    public async Task<PlanResponse?> GetPlan(Guid id)
    {
        var plan = await planRepository.FindById(id);

        if (plan is null)
        {
            return null;
        }

        return PlanResponse.From(plan);
    }
}
