import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, errorMessage } from '../../api/client'
import type { Client } from '../../api/types'
import { card, linkPill, pageTitle } from '../../components/styles'

export function ClientsPage() {
  const [clients, setClients] = useState<Client[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false

    async function loadClients() {
      try {
        const result = await api<Client[]>('/api/trainers/me/clients')
        if (!ignore) setClients(result)
      } catch (reason) {
        if (!ignore) setError(errorMessage(reason))
      }
    }

    loadClients()

    return () => {
      ignore = true
    }
  }, [])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>My clients</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !clients && <p>Loading…</p>}

      {clients && (
        <article className={card}>
          {clients.length === 0 ? (
            <p>
              No clients yet. Pick someone on the{' '}
              <Link to="/members" className="font-medium text-accent">
                Members
              </Link>{' '}
              page and create a plan for them.
            </p>
          ) : (
            <ul className="divide-y divide-surface-2">
              {clients.map((client) => (
                <ClientRow key={client.id} client={client} />
              ))}
            </ul>
          )}
        </article>
      )}
    </section>
  )
}

function ClientRow({ client }: { client: Client }) {
  const memberName = `${client.firstName} ${client.lastName}`

  return (
    <li className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="font-medium text-ink">{memberName}</p>
        <p className="truncate text-sm">{client.email}</p>
      </div>
      <Link to={`/members/${client.id}/plan`} state={{ memberName }} className={linkPill}>
        Plan
      </Link>
    </li>
  )
}
