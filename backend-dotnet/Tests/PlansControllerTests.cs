using System.Net;
using System.Net.Http.Json;
using System.Text.Json;

namespace Fitness.Training.Api.Tests;

public class PlansControllerTests(TrainingApiFactory factory) : IClassFixture<TrainingApiFactory>
{
    [Theory]
    [InlineData("BEGINNER", "Full body", 3)]
    [InlineData("INTERMEDIATE", "Upper/Lower", 4)]
    [InlineData("ADVANCED", "Push/Pull/Legs", 3)]
    public async Task CreatesPlanFromTemplateForLevel(string level, string template, int dayCount)
    {
        var response = await CreatePlan("STRENGTH", level);
        var plan = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        Assert.Equal(level, plan.GetProperty("level").GetString());
        Assert.Equal("STRENGTH", plan.GetProperty("goal").GetString());
        Assert.Equal(template, plan.GetProperty("template").GetString());

        var days = plan.GetProperty("days");
        Assert.Equal(dayCount, days.GetArrayLength());
        Assert.Equal(1, days[0].GetProperty("dayNo").GetInt32());
        Assert.NotEmpty(days[0].GetProperty("exercises").EnumerateArray());
    }

    [Theory]
    [InlineData("STRENGTH", 5, 5)]
    [InlineData("MUSCLE", 4, 10)]
    [InlineData("ENDURANCE", 3, 15)]
    public async Task GoalSetsSetsAndReps(string goal, int sets, int reps)
    {
        var response = await CreatePlan(goal, "BEGINNER");
        var plan = await response.Content.ReadFromJsonAsync<JsonElement>();

        var exercises = plan.GetProperty("days")
            .EnumerateArray()
            .SelectMany(day => day.GetProperty("exercises").EnumerateArray())
            .ToList();

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        Assert.All(exercises, exercise => Assert.Equal(sets, exercise.GetProperty("sets").GetInt32()));
        Assert.All(exercises, exercise => Assert.Equal(reps, exercise.GetProperty("reps").GetInt32()));
    }

    [Fact]
    public async Task ReturnsCreatedPlanById()
    {
        var memberId = Guid.NewGuid();
        var createResponse = await CreatePlan("MUSCLE", "ADVANCED", memberId);
        var createdPlan = await createResponse.Content.ReadFromJsonAsync<JsonElement>();
        var planId = createdPlan.GetProperty("id").GetString();

        var client = factory.CreateClientWithKey();
        var response = await client.GetAsync($"/plans/{planId}");
        var plan = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(planId, plan.GetProperty("id").GetString());
        Assert.Equal(memberId.ToString(), plan.GetProperty("memberId").GetString());
        Assert.Equal("Push", plan.GetProperty("days")[0].GetProperty("name").GetString());

        var firstExercise = plan.GetProperty("days")[0].GetProperty("exercises")[0];
        Assert.Equal("Bench Press", firstExercise.GetProperty("name").GetString());
        Assert.Equal("Chest", firstExercise.GetProperty("muscleGroup").GetString());
    }

    [Fact]
    public async Task ReturnsNotFoundForUnknownPlan()
    {
        var client = factory.CreateClientWithKey();

        var response = await client.GetAsync($"/plans/{Guid.NewGuid()}");
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
        Assert.Equal("Plan not found", body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task RejectsInvalidPlanId()
    {
        var client = factory.CreateClientWithKey();

        var response = await client.GetAsync("/plans/abc");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Theory]
    [InlineData("""{ "memberId": "11111111-1111-1111-1111-111111111111", "authorId": "22222222-2222-2222-2222-222222222222", "goal": "FLYING", "level": "BEGINNER" }""")]
    [InlineData("not json")]
    public async Task RejectsInvalidBody(string json)
    {
        var client = factory.CreateClientWithKey();
        var content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");

        var response = await client.PostAsync("/plans", content);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    private Task<HttpResponseMessage> CreatePlan(string goal, string level)
    {
        var memberId = Guid.NewGuid();

        return CreatePlan(goal, level, memberId);
    }

    private Task<HttpResponseMessage> CreatePlan(string goal, string level, Guid memberId)
    {
        var client = factory.CreateClientWithKey();
        var request = new { memberId, authorId = Guid.NewGuid(), goal, level };

        return client.PostAsJsonAsync("/plans", request);
    }
}
