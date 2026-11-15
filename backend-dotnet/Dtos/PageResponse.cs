namespace Fitness.Training.Api.Dtos;

public record PageResponse<T>(List<T> Items, int Page, int Size, long Total);
