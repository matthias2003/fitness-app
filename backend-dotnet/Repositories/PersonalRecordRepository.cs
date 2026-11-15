using Fitness.Training.Api.Data;
using Fitness.Training.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace Fitness.Training.Api.Repositories;

public class PersonalRecordRepository(TrainingDbContext db)
{
    public Task<List<PersonalRecord>> FindByMember(Guid memberId)
    {
        return db.PersonalRecords
            .Include(record => record.Exercise)
            .Where(record => record.MemberId == memberId)
            .OrderBy(record => record.Exercise!.Name)
            .ToListAsync();
    }

    public async Task Save(List<PersonalRecord> addedRecords)
    {
        db.PersonalRecords.AddRange(addedRecords);
        await db.SaveChangesAsync();
    }
}
