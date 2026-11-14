namespace Fitness.Training.Api.Models;

public class Exercise
{
    public int Id { get; set; }

    public required string Name { get; set; }

    public required string MuscleGroup { get; set; }

    public required string Equipment { get; set; }
}
