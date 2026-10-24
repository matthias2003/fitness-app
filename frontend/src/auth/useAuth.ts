import { createContext, useContext } from 'react'

export type Role = 'MEMBER' | 'TRAINER' | 'ADMIN'

export type User = {
  id: string
  email: string
  firstName: string
  lastName: string
  role: Role
}

export type RegisterData = {
  email: string
  password: string
  firstName: string
  lastName: string
}

export type AuthContextValue = {
  user: User | null
  login: (email: string, password: string) => Promise<void>
  register: (data: RegisterData) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used inside AuthProvider')
  return value
}
