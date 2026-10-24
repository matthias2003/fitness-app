import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { card, pageTitle } from '../components/styles'

type ServiceStatus = {
  service: string
  status: string
  database: string
}

type SystemStatus = {
  core: ServiceStatus
  training: ServiceStatus
}

export function StatusPage() {
  const [status, setStatus] = useState<SystemStatus | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadStatus() {
      try {
        const systemStatus = await api<SystemStatus>('/api/status')
        setStatus(systemStatus)
      } catch (reason) {
        const message = reason instanceof Error ? reason.message : String(reason)
        setError(message)
      }
    }

    loadStatus()
  }, [])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>Service status</h2>

      {error && <p className="text-danger">Cannot reach the Spring API ({error})</p>}

      {!error && !status && <p>Checking connection…</p>}

      {status && (
        <div className="grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-4">
          <StatusCard service={status.core} />
          <StatusCard service={status.training} />
        </div>
      )}
    </section>
  )
}

function StatusCard({ service }: { service: ServiceStatus }) {
  return (
    <article className={`${card} flex flex-col gap-4`}>
      <h3 className="font-mono text-sm text-ink">{service.service}</h3>

      <StatusRow label="Service" state={service.status} />
      <StatusRow label="Database" state={service.database} />
    </article>
  )
}

function StatusRow({ label, state }: { label: string; state: string }) {
  const isUp = state === 'UP'
  const pillColors = isUp ? 'bg-accent text-on-accent' : 'bg-surface-2 text-danger'

  return (
    <div className="flex items-center justify-between text-sm">
      <span>{label}</span>
      <span className={`rounded-full px-3 py-1 text-[13px] font-semibold ${pillColors}`}>
        {state}
      </span>
    </div>
  )
}
