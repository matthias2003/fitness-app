import type { Goal, Level } from '../api/types'

const dateFormat: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' }

export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  const date = new Date(year, month - 1, day)

  return date.toLocaleDateString('en-GB', dateFormat)
}

export function formatDateTime(isoInstant: string): string {
  const date = new Date(isoInstant)
  const datePart = date.toLocaleDateString('en-GB', dateFormat)
  const timePart = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })

  return `${datePart}, ${timePart}`
}

export function formatPrice(price: number): string {
  return `${price.toFixed(2)} PLN`
}

export const goalLabels: Record<Goal, string> = {
  STRENGTH: 'Strength',
  MUSCLE: 'Muscle',
  ENDURANCE: 'Endurance',
}

export const levelLabels: Record<Level, string> = {
  BEGINNER: 'Beginner',
  INTERMEDIATE: 'Intermediate',
  ADVANCED: 'Advanced',
}

export function formatWeight(kilograms: number): string {
  const rounded = Math.round(kilograms * 100) / 100

  return `${rounded.toLocaleString('en-GB')} kg`
}

export function formatSets(count: number): string {
  return count === 1 ? '1 set' : `${count} sets`
}
