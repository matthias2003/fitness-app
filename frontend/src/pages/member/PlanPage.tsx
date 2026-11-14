import { useEffect, useState } from 'react'
import { errorMessage } from '../../api/client'
import { fetchPlan } from '../../api/plans'
import type { TrainingPlan } from '../../api/types'
import { formatDateTime } from '../../components/format'
import { PlanView } from '../../components/PlanView'
import { card, pageTitle, tag } from '../../components/styles'

export function PlanPage() {
  const [plan, setPlan] = useState<TrainingPlan | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadPlan() {
      try {
        const currentPlan = await fetchPlan('/api/members/me/training-plan')
        if (!ignore) setPlan(currentPlan)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadPlan()

    return () => {
      ignore = true
    }
  }, [])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>My training plan</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && loading && <p>Loading…</p>}
      {!error && !loading && !plan && <NoPlanCard />}

      {plan && (
        <>
          <p className="mb-5 text-sm">
            Prepared by <span className="font-medium text-ink">{plan.authorName}</span> on{' '}
            {formatDateTime(plan.assignedAt)}
          </p>
          <PlanView plan={plan} />
        </>
      )}
    </section>
  )
}

function NoPlanCard() {
  return (
    <article className={`${card} flex flex-col gap-1.5`}>
      <span className={`${tag} mb-3 bg-surface-2 text-muted`}>No plan</span>
      <h3 className="text-lg font-semibold tracking-tight text-ink">No training plan yet</h3>
      <p className="text-sm">Ask a trainer at the gym to prepare a plan for you.</p>
    </article>
  )
}
