using System.Net;
using System.Net.Http.Json;
using System.Text.Json;

namespace Fitness.Training.Api.Tests;

public class SessionsControllerTests(TrainingApiFactory factory) : IClassFixture<TrainingApiFactory>
{
    private const int BenchPress = 1;

    private const int BackSquat = 9;

    [Fact]
    public async Task StartsSessionWithoutPlan()
    {
        var memberId = Guid.NewGuid();

        var response = await Start(memberId, null);
        var session = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        Assert.Equal(memberId.ToString(), session.GetProperty("memberId").GetString());
        Assert.Equal(JsonValueKind.Null, session.GetProperty("plannedDay").ValueKind);
        Assert.Equal(JsonValueKind.Null, session.GetProperty("completedAt").ValueKind);
        Assert.Equal(0, session.GetProperty("sets").GetArrayLength());
    }

    [Fact]
    public async Task StartWithPlanPicksNextPlanDay()
    {
        var memberId = Guid.NewGuid();
        var planId = await CreatePlan(memberId);

        var firstSession = await StartAndRead(memberId, planId);
        var firstDay = firstSession.GetProperty("plannedDay");

        Assert.Equal(1, firstDay.GetProperty("dayNo").GetInt32());
        Assert.Equal("Full body A", firstDay.GetProperty("name").GetString());
        Assert.Equal("Back Squat", firstDay.GetProperty("exercises")[0].GetProperty("name").GetString());

        await FinishWorkout(memberId, Id(firstSession), BenchPress, 50m);

        var secondSession = await StartAndRead(memberId, planId);
        var secondDay = secondSession.GetProperty("plannedDay");

        Assert.Equal(2, secondDay.GetProperty("dayNo").GetInt32());
        Assert.Equal("Full body B", secondDay.GetProperty("name").GetString());
    }

    [Fact]
    public async Task IgnoresPlanOfAnotherMember()
    {
        var planId = await CreatePlan(Guid.NewGuid());

        var session = await StartAndRead(Guid.NewGuid(), planId);

        Assert.Equal(JsonValueKind.Null, session.GetProperty("plannedDay").ValueKind);
    }

    [Fact]
    public async Task StartReturnsOpenSessionWhenOneExists()
    {
        var memberId = Guid.NewGuid();
        var firstSession = await StartAndRead(memberId, null);

        var response = await Start(memberId, null);
        var secondSession = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(Id(firstSession), Id(secondSession));
    }

    [Fact]
    public async Task LogsSet()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);

        var response = await LogSet(memberId, sessionId, BenchPress, 8, 62.5m);
        var session = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);

        var set = session.GetProperty("sets")[0];
        Assert.Equal(BenchPress, set.GetProperty("exerciseId").GetInt32());
        Assert.Equal("Bench Press", set.GetProperty("exerciseName").GetString());
        Assert.Equal(8, set.GetProperty("reps").GetInt32());
        Assert.Equal(62.5m, set.GetProperty("weight").GetDecimal());
    }

    [Theory]
    [InlineData(0, 50, "Reps must be between 1 and 100")]
    [InlineData(101, 50, "Reps must be between 1 and 100")]
    [InlineData(5, -1, "Weight must be between 0 and 500 kg")]
    [InlineData(5, 501, "Weight must be between 0 and 500 kg")]
    public async Task RejectsInvalidSet(int reps, decimal weight, string message)
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);

        var response = await LogSet(memberId, sessionId, BenchPress, reps, weight);
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal(message, body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task RejectsUnknownExercise()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);

        var response = await LogSet(memberId, sessionId, 999, 5, 50m);
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal("Unknown exercise", body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task HidesSessionOfAnotherMember()
    {
        var ownerId = Guid.NewGuid();
        var otherId = Guid.NewGuid();
        var sessionId = await StartSessionId(ownerId, null);
        var client = factory.CreateClientWithKey();

        var getResponse = await client.GetAsync($"/members/{otherId}/sessions/{sessionId}");
        var setResponse = await LogSet(otherId, sessionId, BenchPress, 5, 50m);
        var completeResponse = await Complete(otherId, sessionId);
        var body = await getResponse.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.NotFound, getResponse.StatusCode);
        Assert.Equal("Workout not found", body.GetProperty("message").GetString());
        Assert.Equal(HttpStatusCode.NotFound, setResponse.StatusCode);
        Assert.Equal(HttpStatusCode.NotFound, completeResponse.StatusCode);
    }

    [Fact]
    public async Task CompleteReturnsSummaryWithNewRecord()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);
        await LogSet(memberId, sessionId, BenchPress, 5, 60m);
        await LogSet(memberId, sessionId, BenchPress, 5, 70m);
        await LogSet(memberId, sessionId, BackSquat, 10, 0m);

        var response = await Complete(memberId, sessionId);
        var summary = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(sessionId, summary.GetProperty("sessionId").GetString());
        Assert.Equal(3, summary.GetProperty("setsCount").GetInt32());
        Assert.Equal(650m, summary.GetProperty("totalVolume").GetDecimal());
        Assert.Equal(JsonValueKind.Null, summary.GetProperty("previousVolume").ValueKind);

        var newRecords = summary.GetProperty("newRecords");
        Assert.Equal(1, newRecords.GetArrayLength());
        Assert.Equal("Bench Press", newRecords[0].GetProperty("exerciseName").GetString());
        Assert.Equal(70m, newRecords[0].GetProperty("maxWeight").GetDecimal());
        Assert.Equal(JsonValueKind.Null, newRecords[0].GetProperty("previousMaxWeight").ValueKind);
    }

    [Fact]
    public async Task LowerWeightIsNotANewRecord()
    {
        var memberId = Guid.NewGuid();
        var firstSessionId = await StartSessionId(memberId, null);
        await FinishWorkout(memberId, firstSessionId, BenchPress, 70m);

        var secondSessionId = await StartSessionId(memberId, null);
        await LogSet(memberId, secondSessionId, BenchPress, 5, 60m);
        var response = await Complete(memberId, secondSessionId);
        var summary = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(0, summary.GetProperty("newRecords").GetArrayLength());
        Assert.Equal(350m, summary.GetProperty("previousVolume").GetDecimal());
        Assert.Equal(300m, summary.GetProperty("totalVolume").GetDecimal());
    }

    [Fact]
    public async Task HeavierWeightBeatsPreviousRecord()
    {
        var memberId = Guid.NewGuid();
        var firstSessionId = await StartSessionId(memberId, null);
        await FinishWorkout(memberId, firstSessionId, BenchPress, 70m);

        var secondSessionId = await StartSessionId(memberId, null);
        await LogSet(memberId, secondSessionId, BenchPress, 5, 75m);
        var response = await Complete(memberId, secondSessionId);
        var summary = await response.Content.ReadFromJsonAsync<JsonElement>();

        var newRecord = summary.GetProperty("newRecords")[0];
        Assert.Equal(75m, newRecord.GetProperty("maxWeight").GetDecimal());
        Assert.Equal(70m, newRecord.GetProperty("previousMaxWeight").GetDecimal());
    }

    [Fact]
    public async Task CompleteWithoutSetsIsRejected()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);

        var response = await Complete(memberId, sessionId);
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal("Log at least one set first", body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task CompletedSessionCannotChange()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);
        await FinishWorkout(memberId, sessionId, BenchPress, 50m);

        var completeResponse = await Complete(memberId, sessionId);
        var setResponse = await LogSet(memberId, sessionId, BenchPress, 5, 50m);
        var body = await setResponse.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.Conflict, completeResponse.StatusCode);
        Assert.Equal(HttpStatusCode.Conflict, setResponse.StatusCode);
        Assert.Equal("Workout already completed", body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task HistoryListsOnlyCompletedSessionsInPages()
    {
        var memberId = Guid.NewGuid();
        var planId = await CreatePlan(memberId);

        var firstSessionId = await StartSessionId(memberId, planId);
        await FinishWorkout(memberId, firstSessionId, BenchPress, 40m);

        var secondSessionId = await StartSessionId(memberId, planId);
        await FinishWorkout(memberId, secondSessionId, BenchPress, 50m);

        await StartAndRead(memberId, planId);

        var client = factory.CreateClientWithKey();
        var response = await client.GetAsync($"/members/{memberId}/sessions?page=0&size=1");
        var page = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(0, page.GetProperty("page").GetInt32());
        Assert.Equal(1, page.GetProperty("size").GetInt32());
        Assert.Equal(2, page.GetProperty("total").GetInt64());

        var item = page.GetProperty("items")[0];
        Assert.Equal(secondSessionId, item.GetProperty("id").GetString());
        Assert.Equal("Full body B", item.GetProperty("dayName").GetString());
        Assert.Equal(1, item.GetProperty("setsCount").GetInt32());
        Assert.Equal(250m, item.GetProperty("totalVolume").GetDecimal());
    }

    [Theory]
    [InlineData("page=-1&size=10")]
    [InlineData("page=0&size=0")]
    [InlineData("page=0&size=101")]
    public async Task RejectsInvalidPaging(string query)
    {
        var client = factory.CreateClientWithKey();

        var response = await client.GetAsync($"/members/{Guid.NewGuid()}/sessions?{query}");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ListsRecordsSortedByExerciseName()
    {
        var memberId = Guid.NewGuid();
        var sessionId = await StartSessionId(memberId, null);
        await LogSet(memberId, sessionId, BenchPress, 5, 80m);
        await LogSet(memberId, sessionId, BackSquat, 5, 100m);
        await Complete(memberId, sessionId);

        var client = factory.CreateClientWithKey();
        var response = await client.GetAsync($"/members/{memberId}/records");
        var records = await response.Content.ReadFromJsonAsync<List<JsonElement>>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.NotNull(records);
        Assert.Equal(2, records.Count);
        Assert.Equal("Back Squat", records[0].GetProperty("exerciseName").GetString());
        Assert.Equal("Legs", records[0].GetProperty("muscleGroup").GetString());
        Assert.Equal(100m, records[0].GetProperty("maxWeight").GetDecimal());
        Assert.Equal("Bench Press", records[1].GetProperty("exerciseName").GetString());
    }

    [Fact]
    public async Task CurrentSessionIsNotFoundWithoutOpenWorkout()
    {
        var memberId = Guid.NewGuid();
        var client = factory.CreateClientWithKey();

        var noneResponse = await client.GetAsync($"/members/{memberId}/sessions/current");
        var body = await noneResponse.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.NotFound, noneResponse.StatusCode);
        Assert.Equal("No active workout", body.GetProperty("message").GetString());

        var sessionId = await StartSessionId(memberId, null);
        var currentResponse = await client.GetAsync($"/members/{memberId}/sessions/current");
        var current = await currentResponse.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.OK, currentResponse.StatusCode);
        Assert.Equal(sessionId, Id(current));
    }

    private async Task<Guid> CreatePlan(Guid memberId)
    {
        var client = factory.CreateClientWithKey();
        var request = new { memberId, authorId = Guid.NewGuid(), goal = "STRENGTH", level = "BEGINNER" };

        var response = await client.PostAsJsonAsync("/plans", request);
        var plan = await response.Content.ReadFromJsonAsync<JsonElement>();

        return plan.GetProperty("id").GetGuid();
    }

    private Task<HttpResponseMessage> Start(Guid memberId, Guid? planId)
    {
        var client = factory.CreateClientWithKey();
        var request = new { memberId, planId };

        return client.PostAsJsonAsync("/sessions/start", request);
    }

    private async Task<JsonElement> StartAndRead(Guid memberId, Guid? planId)
    {
        var response = await Start(memberId, planId);

        return await response.Content.ReadFromJsonAsync<JsonElement>();
    }

    private async Task<string> StartSessionId(Guid memberId, Guid? planId)
    {
        var session = await StartAndRead(memberId, planId);

        return Id(session);
    }

    private Task<HttpResponseMessage> LogSet(Guid memberId, string sessionId, int exerciseId, int reps, decimal weight)
    {
        var client = factory.CreateClientWithKey();
        var request = new { exerciseId, reps, weight };

        return client.PostAsJsonAsync($"/members/{memberId}/sessions/{sessionId}/sets", request);
    }

    private Task<HttpResponseMessage> Complete(Guid memberId, string sessionId)
    {
        var client = factory.CreateClientWithKey();

        return client.PostAsync($"/members/{memberId}/sessions/{sessionId}/complete", null);
    }

    private async Task FinishWorkout(Guid memberId, string sessionId, int exerciseId, decimal weight)
    {
        await LogSet(memberId, sessionId, exerciseId, 5, weight);
        await Complete(memberId, sessionId);
    }

    private static string Id(JsonElement session)
    {
        return session.GetProperty("id").GetString()!;
    }
}
