namespace Fitness.Training.Api.Models;

public class WorkoutSet
{
    public int Id { get; set; }

    public Guid SessionId { get; set; }

    public int ExerciseId { get; set; }

    public Exercise? Exercise { get; set; }

    public int Reps { get; set; }

    public decimal Weight { get; set; }

    public DateTime CreatedAt { get; set; }
}
