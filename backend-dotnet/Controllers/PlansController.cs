using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fitness.Training.Api.Controllers;

[ApiController]
[Route("plans")]
public class PlansController(PlanService planService) : ControllerBase
{
    [HttpPost]
    public async Task<ActionResult<PlanResponse>> Create(CreatePlanRequest request)
    {
        var plan = await planService.CreatePlan(request);

        return CreatedAtAction(nameof(GetById), new { id = plan.Id }, plan);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<PlanResponse>> GetById(Guid id)
    {
        var plan = await planService.GetPlan(id);

        if (plan is null)
        {
            return NotFound(new ErrorResponse("Plan not found"));
        }

        return plan;
    }
}
