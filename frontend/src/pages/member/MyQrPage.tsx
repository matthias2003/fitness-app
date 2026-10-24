import { QRCodeSVG } from 'qrcode.react'
import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import type { QrToken } from '../../api/types'
import { card, pageTitle } from '../../components/styles'

export function MyQrPage() {
  const [token, setToken] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadToken() {
      try {
        const qrToken = await api<QrToken>('/api/members/me/qr')
        setToken(qrToken.token)
      } catch (reason) {
        setError(errorMessage(reason))
      }
    }

    loadToken()
  }, [])

  return (
    <section>
      <h2 className={`${pageTitle} mb-6`}>My QR code</h2>

      {error && <p className="text-danger">{error}</p>}
      {!error && !token && <p>Loading…</p>}

      {token && (
        <article className={`${card} mx-auto flex max-w-[360px] flex-col items-center gap-4`}>
          <div className="rounded-field bg-white p-4">
            <QRCodeSVG value={token} size={240} className="h-auto w-full max-w-[240px]" />
          </div>
          <p className="text-center text-sm">Show this code at the front desk to enter the gym.</p>
          <code className="w-full rounded-field bg-surface-2 px-3 py-2 text-center font-mono text-xs break-all text-ink">
            {token}
          </code>
        </article>
      )}
    </section>
  )
}
