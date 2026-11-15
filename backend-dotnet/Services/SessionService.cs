using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Models;
using Fitness.Training.Api.Repositories;

namespace Fitness.Training.Api.Services;

public class SessionService(
    WorkoutSessionRepository sessionRepository,
    PersonalRecordRepository recordRepository,
    TrainingPlanRepository planRepository,
    ExerciseRepository exerciseRepository)
{
    private const int MaxPage = 100000;

    public async Task<(SessionResponse Session, bool Created)> StartSession(StartSessionRequest request)
    {
        var openSession = await sessionRepository.FindOpen(request.MemberId);

        if (openSession is not null)
        {
            var existing = await ToResponse(openSession);

            return (existing, false);
        }

        var session = new WorkoutSession
        {
            Id = Guid.NewGuid(),
            MemberId = request.MemberId,
            StartedAt = DateTime.UtcNow,
        };

        if (request.PlanId is not null)
        {
            await AssignPlannedDay(session, request.PlanId.Value);
        }

        await sessionRepository.Add(session);

        var created = await ToResponse(session);

        return (created, true);
    }

    public async Task<SessionResponse> GetCurrentSession(Guid memberId)
    {
        var session = await sessionRepository.FindOpen(memberId);

        if (session is null)
        {
            throw new ApiException(StatusCodes.Status404NotFound, "No active workout");
        }

        return await ToResponse(session);
    }

    public async Task<SessionResponse> GetSession(Guid memberId, Guid id)
    {
        var session = await FindSession(memberId, id);

        return await ToResponse(session);
    }

    public async Task<PageResponse<SessionSummaryResponse>> GetHistory(Guid memberId, int page, int size)
    {
        var invalidPaging = page < 0 || page > MaxPage || size < 1 || size > 100;

        if (invalidPaging)
        {
            throw new ApiException(StatusCodes.Status400BadRequest, "Invalid request parameters");
        }

        var total = await sessionRepository.CountCompleted(memberId);
        var sessions = await sessionRepository.FindCompletedPage(memberId, page * size, size);

        var planIds = sessions
            .Where(session => session.PlanId is not null)
            .Select(session => session.PlanId!.Value)
            .Distinct()
            .ToList();

        var planExercises = await planRepository.FindExercisesOfPlans(planIds);

        var items = sessions
            .Select(session =>
            {
                var dayName = FindDayName(session, planExercises);

                return SessionSummaryResponse.From(session, dayName);
            })
            .ToList();

        return new PageResponse<SessionSummaryResponse>(items, page, size, total);
    }

    public async Task<SessionResponse> LogSet(Guid memberId, Guid id, LogSetRequest request)
    {
        var session = await FindSession(memberId, id);

        if (session.CompletedAt is not null)
        {
            throw new ApiException(StatusCodes.Status409Conflict, "Workout already completed");
        }

        if (request.Reps < 1 || request.Reps > 100)
        {
            throw new ApiException(StatusCodes.Status400BadRequest, "Reps must be between 1 and 100");
        }

        if (request.Weight < 0 || request.Weight > 500)
        {
            throw new ApiException(StatusCodes.Status400BadRequest, "Weight must be between 0 and 500 kg");
        }

        var exerciseExists = await exerciseRepository.Exists(request.ExerciseId);

        if (!exerciseExists)
        {
            throw new ApiException(StatusCodes.Status400BadRequest, "Unknown exercise");
        }

        var set = new WorkoutSet
        {
            SessionId = session.Id,
            ExerciseId = request.ExerciseId,
            Reps = request.Reps,
            Weight = decimal.Round(request.Weight, 2),
            CreatedAt = DateTime.UtcNow,
        };

        await sessionRepository.AddSet(set);

        var updatedSession = await FindSession(memberId, id);

        return await ToResponse(updatedSession);
    }

    public async Task<CompleteSessionResponse> CompleteSession(Guid memberId, Guid id)
    {
        var session = await FindSession(memberId, id);

        if (session.CompletedAt is not null)
        {
            throw new ApiException(StatusCodes.Status409Conflict, "Workout already completed");
        }

        if (session.Sets.Count == 0)
        {
            throw new ApiException(StatusCodes.Status400BadRequest, "Log at least one set first");
        }

        var previousSession = await sessionRepository.FindLastCompleted(memberId);
        var previousVolume = previousSession?.TotalVolume();
        var completedAt = DateTime.UtcNow;

        await using var transaction = await sessionRepository.BeginTransaction();

        session.CompletedAt = completedAt;
        await sessionRepository.SaveChanges();

        var newRecords = await UpdateRecords(session, completedAt);

        await transaction.CommitAsync();

        var totalVolume = session.TotalVolume();

        return new CompleteSessionResponse(
            session.Id,
            completedAt,
            session.Sets.Count,
            totalVolume,
            previousVolume,
            newRecords);
    }

    public async Task<List<RecordResponse>> GetRecords(Guid memberId)
    {
        var records = await recordRepository.FindByMember(memberId);

        return records
            .Select(RecordResponse.From)
            .ToList();
    }

    private async Task<List<NewRecordResponse>> UpdateRecords(WorkoutSession session, DateTime achievedAt)
    {
        var records = await recordRepository.FindByMember(session.MemberId);
        var addedRecords = new List<PersonalRecord>();
        var newRecords = new List<NewRecordResponse>();

        var bestSets = session.Sets
            .GroupBy(set => set.ExerciseId)
            .Select(exerciseSets => exerciseSets.MaxBy(set => set.Weight)!)
            .Where(set => set.Weight > 0)
            .OrderBy(set => set.Exercise!.Name)
            .ToList();

        foreach (var bestSet in bestSets)
        {
            var record = records.FirstOrDefault(candidate => candidate.ExerciseId == bestSet.ExerciseId);
            var previousMaxWeight = record?.MaxWeight;

            if (record is not null && bestSet.Weight <= record.MaxWeight)
            {
                continue;
            }

            if (record is null)
            {
                record = new PersonalRecord
                {
                    MemberId = session.MemberId,
                    ExerciseId = bestSet.ExerciseId,
                };

                addedRecords.Add(record);
            }

            record.MaxWeight = bestSet.Weight;
            record.AchievedAt = achievedAt;

            var newRecord = new NewRecordResponse(
                bestSet.ExerciseId,
                bestSet.Exercise!.Name,
                bestSet.Weight,
                previousMaxWeight);

            newRecords.Add(newRecord);
        }

        await recordRepository.Save(addedRecords);

        return newRecords;
    }

    private async Task AssignPlannedDay(WorkoutSession session, Guid planId)
    {
        var plan = await planRepository.FindById(planId);

        if (plan is null || plan.MemberId != session.MemberId)
        {
            return;
        }

        var dayCount = plan.Exercises
            .Select(planExercise => planExercise.DayNo)
            .Distinct()
            .Count();

        var earlierSessions = await sessionRepository.CountForPlan(session.MemberId, plan.Id);

        session.PlanId = plan.Id;
        session.DayNo = earlierSessions % dayCount + 1;
    }

    private async Task<WorkoutSession> FindSession(Guid memberId, Guid id)
    {
        var session = await sessionRepository.FindForMember(memberId, id);

        if (session is null)
        {
            throw new ApiException(StatusCodes.Status404NotFound, "Workout not found");
        }

        return session;
    }

    private async Task<SessionResponse> ToResponse(WorkoutSession session)
    {
        var plannedDay = await FindPlannedDay(session);

        return SessionResponse.From(session, plannedDay);
    }

    private async Task<PlanDayResponse?> FindPlannedDay(WorkoutSession session)
    {
        if (session.PlanId is null || session.DayNo is null)
        {
            return null;
        }

        var dayExercises = await planRepository.FindDayExercises(session.PlanId.Value, session.DayNo.Value);

        if (dayExercises.Count == 0)
        {
            return null;
        }

        var day = dayExercises
            .GroupBy(planExercise => planExercise.DayNo)
            .First();

        return PlanDayResponse.From(day);
    }

    private static string? FindDayName(WorkoutSession session, List<PlanExercise> planExercises)
    {
        var dayExercise = planExercises.FirstOrDefault(planExercise =>
            planExercise.PlanId == session.PlanId && planExercise.DayNo == session.DayNo);

        return dayExercise?.DayName;
    }
}
