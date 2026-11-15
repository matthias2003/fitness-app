namespace Fitness.Training.Api.Models;

public class PersonalRecord
{
    public Guid MemberId { get; set; }

    public int ExerciseId { get; set; }

    public Exercise? Exercise { get; set; }

    public decimal MaxWeight { get; set; }

    public DateTime AchievedAt { get; set; }
}
