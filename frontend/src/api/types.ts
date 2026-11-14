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
