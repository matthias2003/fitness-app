import { useEffect, useState } from 'react'
import { ApiError, api, errorMessage } from '../../api/client'
import type { Membership, MembershipType } from '../../api/types'
import { formatDate, formatPrice } from '../../components/format'
import { card, formError, pageTitle, primaryButton, tag } from '../../components/styles'

async function fetchCurrentMembership(): Promise<Membership | null> {
  try {
    return await api<Membership>('/api/members/me/membership')
  } catch (reason) {
    const noMembership = reason instanceof ApiError && reason.status === 404
    if (noMembership) return null
    throw reason
  }
}

export function MembershipPage() {
  const [types, setTypes] = useState<MembershipType[]>([])
  const [membership, setMembership] = useState<Membership | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [buyingId, setBuyingId] = useState<number | null>(null)

  useEffect(() => {
    async function loadData() {
      try {
        const membershipTypes = await api<MembershipType[]>('/api/membership-types')
        const currentMembership = await fetchCurrentMembership()

        setTypes(membershipTypes)
        setMembership(currentMembership)
      } catch (reason) {
        setError(errorMessage(reason))
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  async function handleBuy(type: MembershipType) {
    setError(null)
    setNotice(null)
    setBuyingId(type.id)

    try {
      const updatedMembership = await api<Membership>('/api/members/me/memberships', {
        method: 'POST',
        body: { typeId: type.id },
      })

      setMembership(updatedMembership)
      setNotice(
        `Payment accepted. Your pass is valid until ${formatDate(updatedMembership.endDate)}.`,
      )
    } catch (reason) {
      setError(errorMessage(reason))
    } finally {
      setBuyingId(null)
    }
  }

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>My membership</h2>

      {error && <p className={`${formError} mb-4`}>{error}</p>}
      {notice && (
        <p className="mb-4 rounded-field bg-accent/10 px-4 py-3 text-sm text-accent">{notice}</p>
      )}

      {loading ? (
        <p>Loading…</p>
      ) : (
        <>
          <CurrentMembershipCard membership={membership} />

          <h3 className="mt-8 mb-1 text-lg font-semibold tracking-tight text-ink">
            {membership ? 'Extend your pass' : 'Choose a pass'}
          </h3>
          <p className="mb-4 text-sm">
            Buying while your pass is active adds the new days after its current end date.
          </p>

          <div className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-4">
            {types.map((type) => (
              <MembershipTypeCard
                key={type.id}
                type={type}
                buying={buyingId === type.id}
                disabled={buyingId !== null}
                onBuy={() => handleBuy(type)}
              />
            ))}
          </div>
        </>
      )}
    </section>
  )
}

function CurrentMembershipCard({ membership }: { membership: Membership | null }) {
  if (!membership) {
    return (
      <article className={`${card} flex flex-col gap-1.5`}>
        <span className={`${tag} mb-3 bg-surface-2 text-muted`}>Inactive</span>
        <h3 className="text-lg font-semibold tracking-tight text-ink">No active membership</h3>
        <p className="text-sm">Pick a pass below to get access to the gym.</p>
      </article>
    )
  }

  return (
    <article className="flex flex-col gap-1.5 rounded-card bg-accent p-[22px]">
      <span className={`${tag} mb-3 bg-on-accent text-accent`}>Active</span>
      <h3 className="text-lg font-semibold tracking-tight text-on-accent">
        {membership.typeName} pass
      </h3>
      <p className="text-sm text-on-accent">
        {formatDate(membership.startDate)} – {formatDate(membership.endDate)}
      </p>
    </article>
  )
}

type MembershipTypeCardProps = {
  type: MembershipType
  buying: boolean
  disabled: boolean
  onBuy: () => void
}

function MembershipTypeCard({ type, buying, disabled, onBuy }: MembershipTypeCardProps) {
  return (
    <article className={`${card} flex flex-col gap-1.5`}>
      <h4 className="text-lg font-semibold tracking-tight text-ink">{type.name}</h4>
      <p className="text-sm">{type.durationDays} days of access</p>
      <p className="mt-3 mb-5 text-2xl font-semibold text-ink">{formatPrice(type.price)}</p>
      <button
        type="button"
        className={`${primaryButton} mt-auto`}
        disabled={disabled}
        onClick={onBuy}
      >
        {buying ? 'Processing…' : 'Pay'}
      </button>
    </article>
  )
}
