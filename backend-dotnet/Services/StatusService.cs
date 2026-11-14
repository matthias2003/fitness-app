using Fitness.Training.Api.Data;
using Fitness.Training.Api.Dtos;

namespace Fitness.Training.Api.Services;

public class StatusService(TrainingDbContext db)
{
    public async Task<StatusResponse> GetStatus()
    {
        var databaseUp = await db.Database.CanConnectAsync();
        return new StatusResponse("training-api", "UP", databaseUp ? "UP" : "DOWN");
    }
}
