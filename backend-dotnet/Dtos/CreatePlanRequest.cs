using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Dtos;

public record CreatePlanRequest(Guid MemberId, Guid AuthorId, Goal Goal, Level Level);
