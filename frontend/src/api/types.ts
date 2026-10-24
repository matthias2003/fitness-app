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
