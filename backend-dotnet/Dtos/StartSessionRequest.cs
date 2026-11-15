namespace Fitness.Training.Api.Dtos;

public record StartSessionRequest(Guid MemberId, Guid? PlanId);
