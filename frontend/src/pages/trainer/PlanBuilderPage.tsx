import { useEffect, useState, type SubmitEvent } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { api, errorMessage } from '../../api/client'
import { fetchPlan } from '../../api/plans'
import type { Goal, Level, TrainingPlan } from '../../api/types'
import { formatDateTime, goalLabels, levelLabels } from '../../components/format'
import { PlanView } from '../../components/PlanView'
import { card, choicePill, formError, pageTitle, primaryButton } from '../../components/styles'

const goals: Goal[] = ['STRENGTH', 'MUSCLE', 'ENDURANCE']
const levels: Level[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED']

type LocationState = { memberName?: string } | null

export function PlanBuilderPage() {
  const { memberId } = useParams()
  const location = useLocation()
  const state = location.state as LocationState

  const [plan, setPlan] = useState<TrainingPlan | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [goal, setGoal] = useState<Goal>('STRENGTH')
  const [level, setLevel] = useState<Level>('BEGINNER')
  const [submitting, setSubmitting] = useState(false)

  const planPath = `/api/members/${memberId}/training-plan`
  const memberName = plan?.memberName ?? state?.memberName ?? 'This member'

  useEffect(() => {
    let ignore = false

    async function loadPlan() {
      try {
        const currentPlan = await fetchPlan(planPath)
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
  }, [planPath])

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    setSubmitting(true)

    try {
      const createdPlan = await api<TrainingPlan>(planPath, {
        method: 'POST',
        body: { goal, level },
      })

      setPlan(createdPlan)
      setNotice(`New ${createdPlan.template} plan created for ${createdPlan.memberName}.`)
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h2 className={`${pageTitle} mb-1`}>Training plan</h2>
      <p className="mb-6 text-sm">
        For <span className="font-medium text-ink">{memberName}</span>
      </p>

      {error && <p className={`${formError} mb-4`}>{error}</p>}
      {notice && (
        <p className="mb-4 rounded-field bg-accent/10 px-4 py-3 text-sm text-accent">{notice}</p>
      )}

      <form className={`${card} mb-8 flex flex-col gap-5`} onSubmit={handleSubmit}>
        <ChoiceGroup
          label="Goal"
          options={goals}
          labels={goalLabels}
          value={goal}
          onChange={setGoal}
        />
        <ChoiceGroup
          label="Level"
          options={levels}
          labels={levelLabels}
          value={level}
          onChange={setLevel}
        />
        <button type="submit" className={`${primaryButton} sm:self-start`} disabled={submitting}>
          {submitting ? 'Creating…' : 'Create plan'}
        </button>
      </form>

      {loading && <p>Loading…</p>}
      {!loading && !plan && !error && <p>{memberName} has no training plan yet.</p>}

      {plan && (
        <>
          <h3 className="mb-1 text-lg font-semibold tracking-tight text-ink">Current plan</h3>
          <p className="mb-4 text-sm">
            Prepared by {plan.authorName} on {formatDateTime(plan.assignedAt)}
          </p>
          <PlanView plan={plan} />
        </>
      )}
    </section>
  )
}

type ChoiceGroupProps<T extends string> = {
  label: string
  options: T[]
  labels: Record<T, string>
  value: T
  onChange: (value: T) => void
}

function ChoiceGroup<T extends string>({
  label,
  options,
  labels,
  value,
  onChange,
}: ChoiceGroupProps<T>) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="mb-2 text-sm font-medium text-ink">{label}</legend>
      <div className="flex flex-wrap gap-2">
        {options.map((option) => (
          <button
            key={option}
            type="button"
            aria-pressed={option === value}
            className={choicePill(option === value)}
            onClick={() => onChange(option)}
          >
            {labels[option]}
          </button>
        ))}
      </div>
    </fieldset>
  )
}
