import { useState, type ReactNode } from 'react'
import { api, setAccessToken } from '../api/client'
import { AuthContext, type RegisterData, type User } from './useAuth'

type LoginResponse = {
  token: string
  user: User
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)

  async function login(email: string, password: string) {
    const response = await api<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: { email, password },
    })

    setAccessToken(response.token)
    setUser(response.user)
  }

  async function register(data: RegisterData) {
    await api<User>('/api/auth/register', { method: 'POST', body: data })
  }

  function logout() {
    setAccessToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
