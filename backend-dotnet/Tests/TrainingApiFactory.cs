using Fitness.Training.Api.Data;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace Fitness.Training.Api.Tests;

public class TrainingApiFactory : WebApplicationFactory<Program>
{
    public const string ApiKey = "test-key";

    private readonly SqliteConnection connection = new("DataSource=:memory:");

    public TrainingApiFactory()
    {
        connection.Open();
    }

    public HttpClient CreateClientWithKey()
    {
        var client = CreateClient();
        client.DefaultRequestHeaders.Add("X-Internal-Api-Key", ApiKey);

        return client;
    }

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseSetting("INTERNAL_API_KEY", ApiKey);

        builder.ConfigureServices(services =>
        {
            services.RemoveAll<DbContextOptions<TrainingDbContext>>();
            services.RemoveAll<IDbContextOptionsConfiguration<TrainingDbContext>>();

            services.AddDbContext<TrainingDbContext>(options => options.UseSqlite(connection));
        });
    }

    protected override void Dispose(bool disposing)
    {
        base.Dispose(disposing);

        if (disposing)
        {
            connection.Dispose();
        }
    }
}
