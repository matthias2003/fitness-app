import type { PlanDay, PlanExercise, TrainingPlan } from '../api/types'
import { goalLabels, levelLabels } from './format'
import { card, tag } from './styles'

export function PlanView({ plan }: { plan: TrainingPlan }) {
  return (
    <div>
      <div className="mb-4 flex flex-wrap gap-2">
        <span className={`${tag} bg-accent text-on-accent`}>{plan.template}</span>
        <span className={`${tag} bg-surface-2 text-ink`}>{goalLabels[plan.goal]}</span>
        <span className={`${tag} bg-surface-2 text-ink`}>{levelLabels[plan.level]}</span>
      </div>

      <div className="grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-4">
        {plan.days.map((day) => (
          <DayCard key={day.dayNo} day={day} />
        ))}
      </div>
    </div>
  )
}

export function DayCard({ day }: { day: PlanDay }) {
  return (
    <article className={`${card} flex flex-col gap-3`}>
      <div className="flex items-center justify-between gap-2">
        <h3 className="text-lg font-semibold tracking-tight text-ink">{day.name}</h3>
        <span className={`${tag} self-auto bg-surface-2 text-muted`}>Day {day.dayNo}</span>
      </div>

      <ul className="divide-y divide-surface-2">
        {day.exercises.map((exercise) => (
          <ExerciseRow key={exercise.exerciseId} exercise={exercise} />
        ))}
      </ul>
    </article>
  )
}

function ExerciseRow({ exercise }: { exercise: PlanExercise }) {
  return (
    <li className="flex items-center justify-between gap-3 py-2.5">
      <div className="min-w-0">
        <p className="font-medium text-ink">{exercise.name}</p>
        <p className="text-sm">{exercise.muscleGroup}</p>
      </div>
      <span className="shrink-0 rounded-full bg-surface-2 px-3 py-1 text-[13px] font-semibold text-accent">
        {exercise.sets} × {exercise.reps}
      </span>
    </li>
  )
}
