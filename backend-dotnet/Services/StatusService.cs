using Fitness.Training.Api.Data;
using Fitness.Training.Api.Dtos;

namespace Fitness.Training.Api.Services;

public class StatusService(TrainingDbContext db)
{
    public async Task<StatusResponse> GetStatus(CancellationToken cancellationToken)
    {
        var databaseUp = await db.Database.CanConnectAsync(cancellationToken);
        return new StatusResponse("training-api", "UP", databaseUp ? "UP" : "DOWN");
    }
}
