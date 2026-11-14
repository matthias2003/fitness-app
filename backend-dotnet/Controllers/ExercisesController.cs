using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fitness.Training.Api.Controllers;

[ApiController]
[Route("exercises")]
public class ExercisesController(ExerciseService exerciseService) : ControllerBase
{
    [HttpGet]
    public Task<List<ExerciseResponse>> GetAll()
    {
        return exerciseService.GetExercises();
    }
}
