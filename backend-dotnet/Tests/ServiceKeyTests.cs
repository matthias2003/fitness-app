using System.Net;
using System.Net.Http.Json;
using System.Text.Json;

namespace Fitness.Training.Api.Tests;

public class ServiceKeyTests(TrainingApiFactory factory) : IClassFixture<TrainingApiFactory>
{
    [Fact]
    public async Task RejectsRequestWithoutKey()
    {
        var client = factory.CreateClient();

        var response = await client.GetAsync("/exercises");
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
        Assert.Equal("Invalid service key", body.GetProperty("message").GetString());
    }

    [Fact]
    public async Task RejectsRequestWithWrongKey()
    {
        var client = factory.CreateClient();
        client.DefaultRequestHeaders.Add("X-Internal-Api-Key", "wrong-key");

        var response = await client.GetAsync("/exercises");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task StatusIsOpenWithoutKey()
    {
        var client = factory.CreateClient();

        var response = await client.GetAsync("/status");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }
}
