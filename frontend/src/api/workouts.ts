import { ApiError, api } from './client'
import type { CompleteSummary, WorkoutSession } from './types'

const NO_WORKOUT_MESSAGE = 'No active workout'

export type NewSet = {
  exerciseId: number
  reps: number
  weight: number
}

export async function fetchCurrentWorkout(): Promise<WorkoutSession | null> {
  try {
    return await api<WorkoutSession>('/api/workouts/sessions/current')
  } catch (reason) {
    const noWorkout =
      reason instanceof ApiError && reason.status === 404 && reason.message === NO_WORKOUT_MESSAGE
    if (noWorkout) return null
    throw reason
  }
}

export function startWorkout(): Promise<WorkoutSession> {
  return api<WorkoutSession>('/api/workouts/sessions', { method: 'POST' })
}

export function logSet(sessionId: string, set: NewSet): Promise<WorkoutSession> {
  return api<WorkoutSession>(`/api/workouts/sessions/${sessionId}/sets`, {
    method: 'POST',
    body: set,
  })
}

export function finishWorkout(sessionId: string): Promise<CompleteSummary> {
  return api<CompleteSummary>(`/api/workouts/sessions/${sessionId}/complete`, { method: 'POST' })
}
