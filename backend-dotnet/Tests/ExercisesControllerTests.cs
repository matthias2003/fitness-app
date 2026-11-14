using System.Net;
using System.Net.Http.Json;
using System.Text.Json;

namespace Fitness.Training.Api.Tests;

public class ExercisesControllerTests(TrainingApiFactory factory) : IClassFixture<TrainingApiFactory>
{
    [Fact]
    public async Task ListsSeededExercisesSortedByMuscleGroupThenName()
    {
        var client = factory.CreateClientWithKey();

        var response = await client.GetAsync("/exercises");
        var exercises = await response.Content.ReadFromJsonAsync<List<JsonElement>>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.NotNull(exercises);
        Assert.Equal(24, exercises.Count);

        var first = exercises[0];
        Assert.Equal("Arms", first.GetProperty("muscleGroup").GetString());
        Assert.Equal("Barbell Curl", first.GetProperty("name").GetString());
        Assert.Equal("Barbell", first.GetProperty("equipment").GetString());
    }
}
