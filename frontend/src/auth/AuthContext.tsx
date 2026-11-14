import { useEffect, useState, type ReactNode } from 'react'
import {
  api,
  endSession,
  refreshSession,
  setAccessToken,
  setSessionExpiredHandler,
  type Session,
} from '../api/client'
import { AuthContext, type RegisterData, type User } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [restoring, setRestoring] = useState(true)

  useEffect(() => {
    setSessionExpiredHandler(() => setUser(null))

    async function restoreSession() {
      const session = await refreshSession().catch(() => null)

      if (session) {
        setUser(session.user)
      }

      setRestoring(false)
    }

    restoreSession()
  }, [])

  async function login(email: string, password: string) {
    const session = await api<Session>('/api/auth/login', {
      method: 'POST',
      body: { email, password },
    })

    setAccessToken(session.token)
    setUser(session.user)
  }

  async function register(data: RegisterData) {
    await api<User>('/api/auth/register', { method: 'POST', body: data })
  }

  async function logout() {
    await endSession().catch(() => undefined)
    setUser(null)
  }

  if (restoring) {
    return null
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
