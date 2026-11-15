import { useState, type SubmitEvent } from 'react'
import type { Exercise, PlanDay } from '../api/types'
import type { NewSet } from '../api/workouts'
import { card, field, fieldLabel, primaryButton } from './styles'

type LogSetFormProps = {
  plannedDay: PlanDay | null
  exercises: Exercise[]
  submitting: boolean
  onSubmit: (set: NewSet) => void
}

export function LogSetForm({ plannedDay, exercises, submitting, onSubmit }: LogSetFormProps) {
  const plannedExercises = plannedDay?.exercises ?? []
  const plannedIds = plannedExercises.map((exercise) => exercise.exerciseId)
  const otherExercises = exercises.filter((exercise) => !plannedIds.includes(exercise.id))
  const firstExerciseId = plannedIds[0] ?? exercises[0]?.id ?? ''

  const [exerciseId, setExerciseId] = useState(String(firstExerciseId))
  const [reps, setReps] = useState('')
  const [weight, setWeight] = useState('')

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()

    onSubmit({
      exerciseId: Number(exerciseId),
      reps: Number(reps),
      weight: Number(weight),
    })
  }

  return (
    <form className={`${card} flex flex-col gap-4`} onSubmit={handleSubmit}>
      <h3 className="text-lg font-semibold tracking-tight text-ink">Log a set</h3>

      <label className={fieldLabel}>
        Exercise
        <select
          className={field}
          value={exerciseId}
          onChange={(e) => setExerciseId(e.target.value)}
          required
        >
          {plannedExercises.length > 0 && (
            <optgroup label="Today's plan">
              {plannedExercises.map((exercise) => (
                <option key={exercise.exerciseId} value={exercise.exerciseId}>
                  {exercise.name}
                </option>
              ))}
            </optgroup>
          )}
          <optgroup label="All exercises">
            {otherExercises.map((exercise) => (
              <option key={exercise.id} value={exercise.id}>
                {exercise.name}
              </option>
            ))}
          </optgroup>
        </select>
      </label>

      <div className="grid grid-cols-2 gap-3">
        <label className={fieldLabel}>
          Reps
          <input
            className={field}
            type="number"
            inputMode="numeric"
            min={1}
            max={100}
            step={1}
            value={reps}
            onChange={(e) => setReps(e.target.value)}
            required
          />
        </label>
        <label className={fieldLabel}>
          Weight (kg)
          <input
            className={field}
            type="number"
            inputMode="decimal"
            min={0}
            max={500}
            step={0.01}
            value={weight}
            onChange={(e) => setWeight(e.target.value)}
            required
          />
        </label>
      </div>

      <button type="submit" className={primaryButton} disabled={submitting}>
        {submitting ? 'Saving…' : 'Log set'}
      </button>
    </form>
  )
}
