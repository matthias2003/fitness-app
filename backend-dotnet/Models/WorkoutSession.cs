namespace Fitness.Training.Api.Models;

public class WorkoutSession
{
    public Guid Id { get; set; }

    public Guid MemberId { get; set; }

    public Guid? PlanId { get; set; }

    public int? DayNo { get; set; }

    public DateTime StartedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public List<WorkoutSet> Sets { get; set; } = [];

    public decimal TotalVolume()
    {
        return Sets.Sum(set => set.Reps * set.Weight);
    }
}
