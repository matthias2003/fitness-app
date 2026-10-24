using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fitness.Training.Api.Controllers;

[ApiController]
[Route("status")]
public class StatusController(StatusService statusService) : ControllerBase
{
    [HttpGet]
    public Task<StatusResponse> Get(CancellationToken cancellationToken)
    {
        return statusService.GetStatus(cancellationToken);
    }
}
