import type { User } from '../auth/useAuth'

export class ApiError extends Error {
  status: number
  errors: Record<string, string>

  constructor(status: number, message: string, errors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

export type Session = {
  token: string
  user: User
}

type RequestOptions = {
  method?: string
  body?: unknown
}

let accessToken: string | null = null

let onSessionExpired = () => {}

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function setSessionExpiredHandler(handler: () => void) {
  onSessionExpired = handler
}

export async function refreshSession(): Promise<Session | null> {
  const response = await fetch('/api/auth/refresh', { method: 'POST' })

  if (!response.ok) {
    return null
  }

  const session: Session = await response.json()
  setAccessToken(session.token)

  return session
}

export async function endSession() {
  setAccessToken(null)
  await fetch('/api/auth/logout', { method: 'POST' })
}

function buildHeaders(hasBody: boolean): Record<string, string> {
  const headers: Record<string, string> = {}

  if (hasBody) {
    headers['Content-Type'] = 'application/json'
  }

  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`
  }

  return headers
}

function send(path: string, options: RequestOptions): Promise<Response> {
  const method = options.method ?? 'GET'
  const hasBody = options.body !== undefined

  return fetch(path, {
    method,
    headers: buildHeaders(hasBody),
    body: hasBody ? JSON.stringify(options.body) : undefined,
  })
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  let response = await send(path, options)

  if (response.status === 401 && accessToken) {
    const session = await refreshSession()

    if (session) {
      response = await send(path, options)
    } else {
      setAccessToken(null)
      onSessionExpired()
    }
  }

  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    const message = data.message ?? `Server error (HTTP ${response.status})`
    throw new ApiError(response.status, message, data.errors)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export function errorMessage(reason: unknown): string {
  if (reason instanceof Error) return reason.message
  return String(reason)
}
