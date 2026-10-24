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
