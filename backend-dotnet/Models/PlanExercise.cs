namespace Fitness.Training.Api.Models;

public class PlanExercise
{
    public int Id { get; set; }

    public Guid PlanId { get; set; }

    public int DayNo { get; set; }

    public required string DayName { get; set; }

    public int ExerciseId { get; set; }

    public Exercise? Exercise { get; set; }

    public int Sets { get; set; }

    public int Reps { get; set; }

    public int Position { get; set; }
}
