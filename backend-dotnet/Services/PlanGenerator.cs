using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Services;

public static class PlanGenerator
{
    private static readonly (string Name, string[] Exercises)[] FullBodyDays =
    [
        ("Full body A", ["Back Squat", "Bench Press", "Barbell Row", "Plank"]),
        ("Full body B", ["Deadlift", "Overhead Press", "Lat Pulldown", "Hanging Leg Raise"]),
        ("Full body C", ["Leg Press", "Incline Dumbbell Press", "Pull-up", "Russian Twist"]),
    ];

    private static readonly (string Name, string[] Exercises)[] UpperLowerDays =
    [
        ("Upper A", ["Bench Press", "Barbell Row", "Overhead Press", "Barbell Curl", "Triceps Pushdown"]),
        ("Lower A", ["Back Squat", "Romanian Deadlift", "Walking Lunge", "Plank"]),
        ("Upper B", ["Incline Dumbbell Press", "Pull-up", "Lateral Raise", "Hammer Curl", "Skull Crusher"]),
        ("Lower B", ["Deadlift", "Leg Press", "Walking Lunge", "Cable Crunch"]),
    ];

    private static readonly (string Name, string[] Exercises)[] PushPullLegsDays =
    [
        ("Push", ["Bench Press", "Overhead Press", "Incline Dumbbell Press", "Lateral Raise", "Triceps Pushdown"]),
        ("Pull", ["Deadlift", "Pull-up", "Barbell Row", "Face Pull", "Barbell Curl"]),
        ("Legs", ["Back Squat", "Romanian Deadlift", "Leg Press", "Walking Lunge", "Hanging Leg Raise"]),
    ];

    public static string GetTemplateName(Level level)
    {
        return level switch
        {
            Level.Beginner => "Full body",
            Level.Intermediate => "Upper/Lower",
            _ => "Push/Pull/Legs",
        };
    }

    public static List<PlanExercise> BuildExercises(Guid planId, Goal goal, Level level, List<Exercise> exercises)
    {
        var days = GetDays(level);
        var (sets, reps) = GetSetsAndReps(goal);

        var planExercises = new List<PlanExercise>();

        for (var dayIndex = 0; dayIndex < days.Length; dayIndex++)
        {
            var day = days[dayIndex];

            for (var position = 0; position < day.Exercises.Length; position++)
            {
                var exerciseName = day.Exercises[position];
                var exercise = exercises.First(candidate => candidate.Name == exerciseName);

                var planExercise = new PlanExercise
                {
                    PlanId = planId,
                    DayNo = dayIndex + 1,
                    DayName = day.Name,
                    ExerciseId = exercise.Id,
                    Sets = sets,
                    Reps = reps,
                    Position = position + 1,
                };

                planExercises.Add(planExercise);
            }
        }

        return planExercises;
    }

    private static (string Name, string[] Exercises)[] GetDays(Level level)
    {
        return level switch
        {
            Level.Beginner => FullBodyDays,
            Level.Intermediate => UpperLowerDays,
            _ => PushPullLegsDays,
        };
    }

    private static (int Sets, int Reps) GetSetsAndReps(Goal goal)
    {
        return goal switch
        {
            Goal.Strength => (5, 5),
            Goal.Muscle => (4, 10),
            _ => (3, 15),
        };
    }
}
