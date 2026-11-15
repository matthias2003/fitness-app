using Fitness.Training.Api.Dtos;
using Fitness.Training.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fitness.Training.Api.Controllers;

[ApiController]
public class SessionsController(SessionService sessionService) : ControllerBase
{
    [HttpPost("sessions/start")]
    public async Task<ActionResult<SessionResponse>> Start(StartSessionRequest request)
    {
        var (session, created) = await sessionService.StartSession(request);

        if (!created)
        {
            return Ok(session);
        }

        return CreatedAtAction(nameof(GetById), new { memberId = session.MemberId, id = session.Id }, session);
    }

    [HttpGet("members/{memberId}/sessions/current")]
    public Task<SessionResponse> GetCurrent(Guid memberId)
    {
        return sessionService.GetCurrentSession(memberId);
    }

    [HttpGet("members/{memberId}/sessions")]
    public Task<PageResponse<SessionSummaryResponse>> GetHistory(Guid memberId, int page = 0, int size = 10)
    {
        return sessionService.GetHistory(memberId, page, size);
    }

    [HttpGet("members/{memberId}/sessions/{id}")]
    public Task<SessionResponse> GetById(Guid memberId, Guid id)
    {
        return sessionService.GetSession(memberId, id);
    }

    [HttpPost("members/{memberId}/sessions/{id}/sets")]
    public async Task<ActionResult<SessionResponse>> LogSet(Guid memberId, Guid id, LogSetRequest request)
    {
        var session = await sessionService.LogSet(memberId, id, request);

        return CreatedAtAction(nameof(GetById), new { memberId, id }, session);
    }

    [HttpPost("members/{memberId}/sessions/{id}/complete")]
    public Task<CompleteSessionResponse> Complete(Guid memberId, Guid id)
    {
        return sessionService.CompleteSession(memberId, id);
    }
}
