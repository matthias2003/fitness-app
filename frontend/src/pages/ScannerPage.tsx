import { useState, type SubmitEvent } from 'react'
import { ApiError, api, errorMessage } from '../api/client'
import type { CheckInResult } from '../api/types'
import { formatDate, formatDateTime } from '../components/format'
import { card, field, fieldLabel, formError, pageTitle, primaryButton } from '../components/styles'

export function ScannerPage() {
  const [token, setToken] = useState('')
  const [result, setResult] = useState<CheckInResult | null>(null)
  const [denied, setDenied] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setResult(null)
    setDenied(null)
    setError(null)
    setSubmitting(true)

    try {
      const checkIn = await api<CheckInResult>('/api/check-ins', {
        method: 'POST',
        body: { token: token.trim() },
      })

      setResult(checkIn)
      setToken('')
    } catch (reason) {
      const isDenial =
        reason instanceof ApiError && (reason.status === 403 || reason.status === 404)

      if (isDenial) {
        setDenied(reason.message)
      } else {
        setError(errorMessage(reason))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="mx-auto max-w-[560px]">
      <h2 className={`${pageTitle} mb-6`}>Check-in scanner</h2>

      <form className={`${card} flex flex-col gap-4`} onSubmit={handleSubmit}>
        {error && <p className={formError}>{error}</p>}
        <label className={fieldLabel}>
          QR token
          <input
            className={`${field} font-mono`}
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Scan or paste the member's code"
            maxLength={100}
            autoFocus
            required
          />
        </label>
        <button type="submit" className={primaryButton} disabled={submitting}>
          {submitting ? 'Checking…' : 'Check in'}
        </button>
      </form>

      {result && <SuccessCard result={result} />}
      {denied && <DeniedCard message={denied} />}
    </section>
  )
}

function SuccessCard({ result }: { result: CheckInResult }) {
  return (
    <article className="mt-4 flex flex-col gap-1.5 rounded-card bg-accent p-[22px] text-on-accent">
      <p className="text-sm font-semibold uppercase">Entry granted</p>
      <h3 className="text-2xl font-semibold tracking-tight">
        {result.firstName} {result.lastName}
      </h3>
      <p className="text-sm">
        {result.membershipType} pass valid until {formatDate(result.membershipEndDate)}
      </p>
      <p className="text-sm">Checked in at {formatDateTime(result.checkedInAt)}</p>
    </article>
  )
}

function DeniedCard({ message }: { message: string }) {
  return (
    <article className="mt-4 flex flex-col gap-1.5 rounded-card bg-danger/15 p-[22px] text-danger">
      <p className="text-sm font-semibold uppercase">Entry denied</p>
      <p className="text-lg font-medium">{message}</p>
    </article>
  )
}
