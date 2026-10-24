import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { CheckIn, PageResponse } from '../../api/types'
import { formatDateTime } from '../../components/format'
import { Pagination } from '../../components/Pagination'
import { card, pageTitle } from '../../components/styles'

const PAGE_SIZE = 10

export function CheckInsPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<PageResponse<CheckIn> | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadPage() {
      setError(null)

      try {
        const path = `/api/members/me/check-ins?page=${page}&size=${PAGE_SIZE}`
        const checkIns = await api<PageResponse<CheckIn>>(path)
        if (!ignore) setResult(checkIns)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      }
    }

    loadPage()

    return () => {
      ignore = true
    }
  }, [page])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Check-in history</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !result && <p>Loading…</p>}

      {result && (
        <article className={card}>
          {result.items.length === 0 ? (
            <p>No visits yet. Show your QR code at the front desk to check in.</p>
          ) : (
            <ul className="divide-y divide-surface-2">
              {result.items.map((checkIn) => (
                <li key={checkIn.id} className="flex items-center justify-between py-3 text-sm">
                  <span className="text-ink">{formatDateTime(checkIn.checkedInAt)}</span>
                  <span className="rounded-full bg-surface-2 px-3 py-1 text-[13px]">Gym entry</span>
                </li>
              ))}
            </ul>
          )}

          <Pagination page={page} size={PAGE_SIZE} total={result.total} onChange={setPage} />
        </article>
      )}
    </section>
  )
}
