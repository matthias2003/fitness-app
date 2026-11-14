import { useEffect, useState } from 'react'
import { api, errorMessage } from '../api/client'
import type { Exercise } from '../api/types'
import { card, choicePill, pageTitle, tag } from '../components/styles'

const ALL_GROUPS = 'All'

export function ExercisesPage() {
  const [exercises, setExercises] = useState<Exercise[] | null>(null)
  const [selectedGroup, setSelectedGroup] = useState(ALL_GROUPS)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadExercises() {
      try {
        const library = await api<Exercise[]>('/api/exercises')
        if (!ignore) setExercises(library)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      }
    }

    loadExercises()

    return () => {
      ignore = true
    }
  }, [])

  const muscleGroups = [...new Set(exercises?.map((exercise) => exercise.muscleGroup))]
  const groups = [ALL_GROUPS, ...muscleGroups]

  const visibleExercises = exercises?.filter(
    (exercise) => selectedGroup === ALL_GROUPS || exercise.muscleGroup === selectedGroup,
  )

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Exercise library</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !exercises && <p>Loading…</p>}

      {visibleExercises && (
        <>
          <div className="mb-5 flex flex-wrap gap-2">
            {groups.map((group) => (
              <button
                key={group}
                type="button"
                aria-pressed={group === selectedGroup}
                className={choicePill(group === selectedGroup)}
                onClick={() => setSelectedGroup(group)}
              >
                {group}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-4">
            {visibleExercises.map((exercise) => (
              <ExerciseCard key={exercise.id} exercise={exercise} />
            ))}
          </div>
        </>
      )}
    </section>
  )
}

function ExerciseCard({ exercise }: { exercise: Exercise }) {
  return (
    <article className={`${card} flex flex-col gap-1.5`}>
      <span className={`${tag} mb-2 bg-surface-2 text-accent`}>{exercise.muscleGroup}</span>
      <h3 className="text-lg font-semibold tracking-tight text-ink">{exercise.name}</h3>
      <p className="text-sm">{exercise.equipment}</p>
    </article>
  )
}
