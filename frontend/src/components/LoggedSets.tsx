import type { WorkoutSet } from '../api/types'
import { formatSets, formatWeight } from './format'
import { card, tag } from './styles'

type ExerciseSets = {
  exerciseId: number
  exerciseName: string
  sets: WorkoutSet[]
}

function groupByExercise(sets: WorkoutSet[]): ExerciseSets[] {
  const groups: ExerciseSets[] = []

  for (const set of sets) {
    const group = groups.find((candidate) => candidate.exerciseId === set.exerciseId)

    if (group) {
      group.sets.push(set)
    } else {
      groups.push({ exerciseId: set.exerciseId, exerciseName: set.exerciseName, sets: [set] })
    }
  }

  return groups
}

export function LoggedSets({ sets }: { sets: WorkoutSet[] }) {
  if (sets.length === 0) {
    return (
      <article className={card}>
        <p>No sets logged yet.</p>
      </article>
    )
  }

  const groups = groupByExercise(sets)

  return (
    <div className="flex flex-col gap-4">
      {groups.map((group) => (
        <ExerciseSetsCard key={group.exerciseId} group={group} />
      ))}
    </div>
  )
}

function ExerciseSetsCard({ group }: { group: ExerciseSets }) {
  return (
    <article className={`${card} flex flex-col gap-2`}>
      <div className="flex items-center justify-between gap-2">
        <h4 className="font-semibold text-ink">{group.exerciseName}</h4>
        <span className={`${tag} self-auto bg-surface-2 text-muted`}>
          {formatSets(group.sets.length)}
        </span>
      </div>

      <ol className="divide-y divide-surface-2 text-sm">
        {group.sets.map((set, index) => (
          <li key={set.id} className="flex items-center justify-between py-2">
            <span>Set {index + 1}</span>
            <span className="font-medium text-ink">
              {set.reps} × {formatWeight(set.weight)}
            </span>
          </li>
        ))}
      </ol>
    </article>
  )
}
