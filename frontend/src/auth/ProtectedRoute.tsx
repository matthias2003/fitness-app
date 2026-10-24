import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth, type Role } from './useAuth'

export function ProtectedRoute({ role }: { role?: Role }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (role && user.role !== role) return <Navigate to="/" replace />
  return <Outlet />
}
