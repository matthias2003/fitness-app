import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { PersonalRecord } from '../../api/types'
import { formatDateTime, formatWeight } from '../../components/format'
import { card, pageTitle } from '../../components/styles'

export function RecordsPage() {
  const [records, setRecords] = useState<PersonalRecord[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadRecords() {
      try {
        const personalRecords = await api<PersonalRecord[]>('/api/members/me/records')
        if (!ignore) setRecords(personalRecords)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      }
    }

    loadRecords()

    return () => {
      ignore = true
    }
  }, [])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Personal records</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !records && <p>Loading…</p>}

      {records && (
        <article className={card}>
          {records.length === 0 ? (
            <p>No records yet. Finish a workout with weights to set your first one.</p>
          ) : (
            <ul className="divide-y divide-surface-2">
              {records.map((record) => (
                <RecordRow key={record.exerciseId} record={record} />
              ))}
            </ul>
          )}
        </article>
      )}
    </section>
  )
}

function RecordRow({ record }: { record: PersonalRecord }) {
  return (
    <li className="flex items-center justify-between gap-3 py-3 text-sm">
      <div className="min-w-0">
        <p className="font-medium text-ink">{record.exerciseName}</p>
        <p>
          {record.muscleGroup} · {formatDateTime(record.achievedAt)}
        </p>
      </div>
      <span className="shrink-0 rounded-full bg-surface-2 px-3 py-1 text-[13px] font-semibold text-accent">
        {formatWeight(record.maxWeight)}
      </span>
    </li>
  )
}
