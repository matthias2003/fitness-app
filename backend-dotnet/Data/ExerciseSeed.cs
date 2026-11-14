using Fitness.Training.Api.Models;

namespace Fitness.Training.Api.Data;

public static class ExerciseSeed
{
    public static readonly Exercise[] Exercises =
    [
        new() { Id = 1, Name = "Bench Press", MuscleGroup = "Chest", Equipment = "Barbell" },
        new() { Id = 2, Name = "Incline Dumbbell Press", MuscleGroup = "Chest", Equipment = "Dumbbells" },
        new() { Id = 3, Name = "Push-up", MuscleGroup = "Chest", Equipment = "Bodyweight" },
        new() { Id = 4, Name = "Cable Fly", MuscleGroup = "Chest", Equipment = "Cable" },

        new() { Id = 5, Name = "Deadlift", MuscleGroup = "Back", Equipment = "Barbell" },
        new() { Id = 6, Name = "Pull-up", MuscleGroup = "Back", Equipment = "Bodyweight" },
        new() { Id = 7, Name = "Barbell Row", MuscleGroup = "Back", Equipment = "Barbell" },
        new() { Id = 8, Name = "Lat Pulldown", MuscleGroup = "Back", Equipment = "Cable" },

        new() { Id = 9, Name = "Back Squat", MuscleGroup = "Legs", Equipment = "Barbell" },
        new() { Id = 10, Name = "Romanian Deadlift", MuscleGroup = "Legs", Equipment = "Barbell" },
        new() { Id = 11, Name = "Leg Press", MuscleGroup = "Legs", Equipment = "Machine" },
        new() { Id = 12, Name = "Walking Lunge", MuscleGroup = "Legs", Equipment = "Dumbbells" },

        new() { Id = 13, Name = "Overhead Press", MuscleGroup = "Shoulders", Equipment = "Barbell" },
        new() { Id = 14, Name = "Lateral Raise", MuscleGroup = "Shoulders", Equipment = "Dumbbells" },
        new() { Id = 15, Name = "Face Pull", MuscleGroup = "Shoulders", Equipment = "Cable" },
        new() { Id = 16, Name = "Arnold Press", MuscleGroup = "Shoulders", Equipment = "Dumbbells" },

        new() { Id = 17, Name = "Barbell Curl", MuscleGroup = "Arms", Equipment = "Barbell" },
        new() { Id = 18, Name = "Hammer Curl", MuscleGroup = "Arms", Equipment = "Dumbbells" },
        new() { Id = 19, Name = "Triceps Pushdown", MuscleGroup = "Arms", Equipment = "Cable" },
        new() { Id = 20, Name = "Skull Crusher", MuscleGroup = "Arms", Equipment = "Barbell" },

        new() { Id = 21, Name = "Plank", MuscleGroup = "Core", Equipment = "Bodyweight" },
        new() { Id = 22, Name = "Hanging Leg Raise", MuscleGroup = "Core", Equipment = "Bodyweight" },
        new() { Id = 23, Name = "Cable Crunch", MuscleGroup = "Core", Equipment = "Cable" },
        new() { Id = 24, Name = "Russian Twist", MuscleGroup = "Core", Equipment = "Bodyweight" },
    ];
}
