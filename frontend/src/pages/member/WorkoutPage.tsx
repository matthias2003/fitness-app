import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { CompleteSummary, Exercise, WorkoutSession } from '../../api/types'
import {
  fetchCurrentWorkout,
  finishWorkout,
  logSet,
  startWorkout,
  type NewSet,
} from '../../api/workouts'
import { formatDateTime } from '../../components/format'
import { LoggedSets } from '../../components/LoggedSets'
import { LogSetForm } from '../../components/LogSetForm'
import { DayCard } from '../../components/PlanView'
import {
  card,
  formError,
  pageTitle,
  primaryButton,
  secondaryButton,
  tag,
} from '../../components/styles'
import { WorkoutSummaryCard } from '../../components/WorkoutSummaryCard'

export function WorkoutPage() {
  const [session, setSession] = useState<WorkoutSession | null>(null)
  const [exercises, setExercises] = useState<Exercise[]>([])
  const [summary, setSummary] = useState<CompleteSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let ignore = false

    async function loadWorkout() {
      try {
        const currentWorkout = await fetchCurrentWorkout()
        const library = await api<Exercise[]>('/api/exercises')

        if (!ignore) {
          setSession(currentWorkout)
          setExercises(library)
        }
      } catch (reason) {
        if (!ignore) setLoadError(errorMessage(reason))
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadWorkout()

    return () => {
      ignore = true
    }
  }, [])

  async function runAction(action: () => Promise<void>) {
    setActionError(null)
    setBusy(true)

    try {
      await action()
    } catch (reason) {
      setActionError(errorMessage(reason))
    } finally {
      setBusy(false)
    }
  }

  function handleStart() {
    runAction(async () => {
      const startedSession = await startWorkout()
      setSummary(null)
      setSession(startedSession)
    })
  }

  function handleLogSet(set: NewSet) {
    if (!session) return

    runAction(async () => {
      const updatedSession = await logSet(session.id, set)
      setSession(updatedSession)
    })
  }

  function handleFinish() {
    if (!session) return

    runAction(async () => {
      const completeSummary = await finishWorkout(session.id)
      setSummary(completeSummary)
      setSession(null)
    })
  }

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Workout</h2>

      {loadError && <p className="text-danger">{loadError}</p>}
      {!loadError && loading && <p>Loading…</p>}
      {actionError && <p className={`${formError} mb-4`}>{actionError}</p>}

      {summary && (
        <div className="mb-6">
          <WorkoutSummaryCard summary={summary} />
        </div>
      )}

      {!loadError && !loading && !session && <NoWorkoutCard busy={busy} onStart={handleStart} />}

      {session && (
        <ActiveWorkout
          session={session}
          exercises={exercises}
          busy={busy}
          onLogSet={handleLogSet}
          onFinish={handleFinish}
        />
      )}
    </section>
  )
}

type NoWorkoutCardProps = {
  busy: boolean
  onStart: () => void
}

function NoWorkoutCard({ busy, onStart }: NoWorkoutCardProps) {
  return (
    <article className={`${card} flex flex-col gap-1.5`}>
      <span className={`${tag} mb-3 bg-surface-2 text-muted`}>No active workout</span>
      <h3 className="text-lg font-semibold tracking-tight text-ink">Ready to train?</h3>
      <p className="mb-4 text-sm">Check in at the front desk or start a workout here.</p>
      <button
        type="button"
        className={`${primaryButton} sm:self-start`}
        disabled={busy}
        onClick={onStart}
      >
        {busy ? 'Starting…' : 'Start workout'}
      </button>
    </article>
  )
}

type ActiveWorkoutProps = {
  session: WorkoutSession
  exercises: Exercise[]
  busy: boolean
  onLogSet: (set: NewSet) => void
  onFinish: () => void
}

function ActiveWorkout({ session, exercises, busy, onLogSet, onFinish }: ActiveWorkoutProps) {
  return (
    <div className="flex flex-col gap-6">
      <p className="text-sm">Started {formatDateTime(session.startedAt)}</p>

      <div className="grid gap-4 md:grid-cols-2">
        {session.plannedDay ? (
          <DayCard day={session.plannedDay} />
        ) : (
          <article className={card}>
            <h3 className="text-lg font-semibold tracking-tight text-ink">Free workout</h3>
            <p className="mt-1.5 text-sm">
              No plan for today — pick any exercise from the library.
            </p>
          </article>
        )}
        <LogSetForm
          plannedDay={session.plannedDay}
          exercises={exercises}
          submitting={busy}
          onSubmit={onLogSet}
        />
      </div>

      <div>
        <h3 className="mb-3 text-lg font-semibold tracking-tight text-ink">Logged sets</h3>
        <LoggedSets sets={session.sets} />
      </div>

      <button
        type="button"
        className={`${secondaryButton} disabled:cursor-default disabled:opacity-40 sm:self-start`}
        disabled={busy || session.sets.length === 0}
        onClick={onFinish}
      >
        Finish workout
      </button>
    </div>
  )
}
