import { Link } from 'react-router-dom'
import type { CompleteSummary, NewRecord } from '../api/types'
import { formatWeight } from './format'
import { card, linkPill } from './styles'

function volumeComparison(summary: CompleteSummary): string {
  if (summary.previousVolume === null) {
    return 'Your first completed workout'
  }

  const difference = summary.totalVolume - summary.previousVolume
  const sign = difference >= 0 ? '+' : '−'
  const absolute = formatWeight(Math.abs(difference))

  return `${sign}${absolute} vs previous workout (${formatWeight(summary.previousVolume)})`
}

export function WorkoutSummaryCard({ summary }: { summary: CompleteSummary }) {
  return (
    <article className={`${card} flex flex-col gap-5`}>
      <div>
        <p className="text-sm font-semibold text-accent uppercase">Workout complete</p>
        <h3 className="mt-1 text-2xl font-semibold tracking-tight text-ink">Great job!</h3>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Stat label="Sets" value={String(summary.setsCount)} />
        <Stat label="Volume" value={formatWeight(summary.totalVolume)} />
      </div>
      <p className="text-sm">{volumeComparison(summary)}</p>

      {summary.newRecords.length > 0 && (
        <div className="flex flex-col gap-2">
          <h4 className="font-semibold text-ink">New personal records</h4>
          <ul className="flex flex-col gap-2">
            {summary.newRecords.map((record) => (
              <RecordRow key={record.exerciseId} record={record} />
            ))}
          </ul>
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        <Link to="/records" className={linkPill}>
          My records
        </Link>
        <Link to="/workouts" className={linkPill}>
          Workout history
        </Link>
      </div>
    </article>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-field bg-surface-2 px-4 py-3">
      <p className="text-xs uppercase">{label}</p>
      <p className="text-xl font-semibold text-ink">{value}</p>
    </div>
  )
}

function RecordRow({ record }: { record: NewRecord }) {
  const previous =
    record.previousMaxWeight === null
      ? 'First record'
      : `Previously ${formatWeight(record.previousMaxWeight)}`

  return (
    <li className="flex items-center justify-between gap-3 rounded-field bg-accent px-4 py-3 text-on-accent">
      <div className="min-w-0">
        <p className="font-semibold">{record.exerciseName}</p>
        <p className="text-sm">{previous}</p>
      </div>
      <span className="shrink-0 text-lg font-bold">{formatWeight(record.maxWeight)}</span>
    </li>
  )
}
