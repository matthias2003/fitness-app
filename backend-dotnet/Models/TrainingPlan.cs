namespace Fitness.Training.Api.Models;

public class TrainingPlan
{
    public Guid Id { get; set; }

    public Guid MemberId { get; set; }

    public Guid AuthorId { get; set; }

    public Goal Goal { get; set; }

    public Level Level { get; set; }

    public required string Template { get; set; }

    public DateTime CreatedAt { get; set; }

    public List<PlanExercise> Exercises { get; set; } = [];
}
