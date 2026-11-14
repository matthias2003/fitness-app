using System.Text.Json;
using System.Text.Json.Serialization;
using Fitness.Training.Api.Data;
using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Repositories;
using Fitness.Training.Api.Services;
using Microsoft.EntityFrameworkCore;

DotNetEnv.Env.TraversePath().Load();

var builder = WebApplication.CreateBuilder(args);

var internalApiKey = builder.Configuration["INTERNAL_API_KEY"];

if (string.IsNullOrWhiteSpace(internalApiKey))
{
    throw new InvalidOperationException("INTERNAL_API_KEY is not configured");
}

builder.Services
    .AddControllers()
    .AddJsonOptions(options =>
    {
        var enumConverter = new JsonStringEnumConverter(JsonNamingPolicy.SnakeCaseUpper);
        options.JsonSerializerOptions.Converters.Add(enumConverter);
    });

builder.Services.AddDbContext<TrainingDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("Training")));

builder.Services.AddScoped<StatusService>();
builder.Services.AddScoped<ExerciseRepository>();
builder.Services.AddScoped<TrainingPlanRepository>();
builder.Services.AddScoped<ExerciseService>();
builder.Services.AddScoped<PlanService>();

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<TrainingDbContext>();
    db.Database.EnsureCreated();
}

app.Use(async (context, next) =>
{
    var isStatus = context.Request.Path.StartsWithSegments("/status");
    var providedKey = context.Request.Headers["X-Internal-Api-Key"].ToString();

    if (!isStatus && providedKey != internalApiKey)
    {
        context.Response.StatusCode = StatusCodes.Status401Unauthorized;
        await context.Response.WriteAsJsonAsync(new ErrorResponse("Invalid service key"));
        return;
    }

    await next(context);
});

app.MapControllers();

app.Run();

public partial class Program
{
}
