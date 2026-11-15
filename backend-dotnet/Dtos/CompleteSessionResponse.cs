namespace Fitness.Training.Api.Dtos;

public record CompleteSessionResponse(
    Guid SessionId,
    DateTime CompletedAt,
    int SetsCount,
    decimal TotalVolume,
    decimal? PreviousVolume,
    List<NewRecordResponse> NewRecords);
