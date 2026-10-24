using Microsoft.EntityFrameworkCore;

namespace Fitness.Training.Api.Data;

public class TrainingDbContext(DbContextOptions<TrainingDbContext> options) : DbContext(options)
{
}
