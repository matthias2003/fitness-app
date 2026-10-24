import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { Member, PageResponse } from '../../api/types'
import { formatDate } from '../../components/format'
import { Pagination } from '../../components/Pagination'
import { card, pageTitle } from '../../components/styles'

const PAGE_SIZE = 10

export function MembersPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<PageResponse<Member> | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadPage() {
      setError(null)

      try {
        const path = `/api/admin/members?page=${page}&size=${PAGE_SIZE}`
        const members = await api<PageResponse<Member>>(path)
        if (!ignore) setResult(members)
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
      <h2 className={`${pageTitle} mb-6`}>Members</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !result && <p>Loading…</p>}

      {result && (
        <article className={card}>
          <p className="mb-2 text-sm">
            {result.total} {result.total === 1 ? 'member' : 'members'} in total
          </p>

          {result.items.length === 0 ? (
            <p>No members yet.</p>
          ) : (
            <ul className="divide-y divide-surface-2">
              {result.items.map((member) => (
                <MemberRow key={member.id} member={member} />
              ))}
            </ul>
          )}

          <Pagination page={page} size={PAGE_SIZE} total={result.total} onChange={setPage} />
        </article>
      )}
    </section>
  )
}

function MemberRow({ member }: { member: Member }) {
  return (
    <li className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="font-medium text-ink">
          {member.firstName} {member.lastName}
        </p>
        <p className="truncate text-sm">{member.email}</p>
      </div>
      <MembershipStatus member={member} />
    </li>
  )
}

function MembershipStatus({ member }: { member: Member }) {
  const pill = 'self-start rounded-full px-3 py-1 text-[13px] font-semibold sm:self-auto'

  if (member.membershipActive && member.membershipEndDate) {
    return (
      <span className={`${pill} bg-accent text-on-accent`}>
        {member.membershipType} · until {formatDate(member.membershipEndDate)}
      </span>
    )
  }

  if (member.membershipEndDate) {
    return (
      <span className={`${pill} bg-surface-2 text-danger`}>
        Expired {formatDate(member.membershipEndDate)}
      </span>
    )
  }

  return <span className={`${pill} bg-surface-2 text-muted`}>No membership</span>
}
