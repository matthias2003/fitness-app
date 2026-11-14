import { ApiError, api } from './client'
import type { TrainingPlan } from './types'

const NO_PLAN_MESSAGE = 'No training plan yet'

export async function fetchPlan(path: string): Promise<TrainingPlan | null> {
  try {
    return await api<TrainingPlan>(path)
  } catch (reason) {
    const noPlan =
      reason instanceof ApiError && reason.status === 404 && reason.message === NO_PLAN_MESSAGE
    if (noPlan) return null
    throw reason
  }
}
