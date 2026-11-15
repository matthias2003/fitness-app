import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { PageResponse, SessionSummary } from '../../api/types'
import { formatDateTime, formatSets, formatWeight } from '../../components/format'
import { Pagination } from '../../components/Pagination'
import { card, pageTitle } from '../../components/styles'

const PAGE_SIZE = 10

export function WorkoutHistoryPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<PageResponse<SessionSummary> | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadPage() {
      setError(null)

      try {
        const path = `/api/workouts/sessions?page=${page}&size=${PAGE_SIZE}`
        const sessions = await api<PageResponse<SessionSummary>>(path)
        if (!ignore) setResult(sessions)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      }
    }

    loadPage()

    return () => {
      ignore = true
    }
  }, [page])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Workout history</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !result && <p>Loading…</p>}

      {result && (
        <article className={card}>
          {result.items.length === 0 ? (
            <p>No finished workouts yet.</p>
          ) : (
            <ul className="divide-y divide-surface-2">
              {result.items.map((session) => (
                <HistoryRow key={session.id} session={session} />
              ))}
            </ul>
          )}

          <Pagination page={page} size={PAGE_SIZE} total={result.total} onChange={setPage} />
        </article>
      )}
    </section>
  )
}

function HistoryRow({ session }: { session: SessionSummary }) {
  return (
    <li className="flex flex-col gap-1 py-3 text-sm sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="font-medium text-ink">{session.dayName ?? 'Free workout'}</p>
        <p>{formatDateTime(session.startedAt)}</p>
      </div>
      <div className="flex gap-2">
        <span className="rounded-full bg-surface-2 px-3 py-1 text-[13px]">
          {formatSets(session.setsCount)}
        </span>
        <span className="rounded-full bg-surface-2 px-3 py-1 text-[13px] font-semibold text-accent">
          {formatWeight(session.totalVolume)}
        </span>
      </div>
    </li>
  )
}
