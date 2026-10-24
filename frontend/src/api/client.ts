export class ApiError extends Error {
  status: number
  errors: Record<string, string>

  constructor(status: number, message: string, errors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

type RequestOptions = {
  method?: string
  body?: unknown
}

let accessToken: string | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
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

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? 'GET'
  const hasBody = options.body !== undefined

  const response = await fetch(path, {
    method,
    headers: buildHeaders(hasBody),
    body: hasBody ? JSON.stringify(options.body) : undefined,
  })

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
