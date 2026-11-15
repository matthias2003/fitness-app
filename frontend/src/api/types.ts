export type PageResponse<T> = {
  items: T[]
  page: number
  size: number
  total: number
}

export type MembershipType = {
  id: number
  name: string
  durationDays: number
  price: number
}

export type Membership = {
  id: number
  typeName: string
  startDate: string
  endDate: string
}

export type QrToken = {
  token: string
}

export type CheckIn = {
  id: number
  checkedInAt: string
}

export type CheckInResult = {
  firstName: string
  lastName: string
  membershipType: string
  membershipEndDate: string
  checkedInAt: string
  trainingSessionId: string | null
}

export type Member = {
  id: string
  email: string
  firstName: string
  lastName: string
  membershipType: string | null
  membershipEndDate: string | null
  membershipActive: boolean
}

export type Goal = 'STRENGTH' | 'MUSCLE' | 'ENDURANCE'

export type Level = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

export type Exercise = {
  id: number
  name: string
  muscleGroup: string
  equipment: string
}

export type PlanExercise = {
  exerciseId: number
  name: string
  muscleGroup: string
  sets: number
  reps: number
}

export type PlanDay = {
  dayNo: number
  name: string
  exercises: PlanExercise[]
}

export type TrainingPlan = {
  id: string
  memberId: string
  authorId: string
  goal: Goal
  level: Level
  template: string
  createdAt: string
  days: PlanDay[]
  memberName: string
  authorName: string
  assignedAt: string
}

export type Client = {
  id: string
  email: string
  firstName: string
  lastName: string
}

export type WorkoutSet = {
  id: number
  exerciseId: number
  exerciseName: string
  reps: number
  weight: number
  createdAt: string
}

export type WorkoutSession = {
  id: string
  memberId: string
  startedAt: string
  completedAt: string | null
  plannedDay: PlanDay | null
  sets: WorkoutSet[]
}

export type SessionSummary = {
  id: string
  startedAt: string
  completedAt: string
  dayName: string | null
  setsCount: number
  totalVolume: number
}

export type NewRecord = {
  exerciseId: number
  exerciseName: string
  maxWeight: number
  previousMaxWeight: number | null
}

export type CompleteSummary = {
  sessionId: string
  completedAt: string
  setsCount: number
  totalVolume: number
  previousVolume: number | null
  newRecords: NewRecord[]
}

export type PersonalRecord = {
  exerciseId: number
  exerciseName: string
  muscleGroup: string
  maxWeight: number
  achievedAt: string
}
