using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fitness.Training.Api.Controllers;

[ApiController]
[Route("members/{memberId}/records")]
public class RecordsController(SessionService sessionService) : ControllerBase
{
    [HttpGet]
    public Task<List<RecordResponse>> GetAll(Guid memberId)
    {
        return sessionService.GetRecords(memberId);
    }
}
