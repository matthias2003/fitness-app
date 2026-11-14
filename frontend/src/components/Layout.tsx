import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth, type Role } from '../auth/useAuth'
import { secondaryButton } from './styles'

type NavItem = {
  label: string
  to?: string
}

const roleLinks: Record<Role, NavItem[]> = {
  MEMBER: [
    { label: 'My membership', to: '/membership' },
    { label: 'My QR', to: '/qr' },
    { label: 'Check-ins', to: '/check-ins' },
    { label: 'My plan', to: '/plan' },
    { label: 'Exercises', to: '/exercises' },
    { label: 'Class schedule' },
  ],
  TRAINER: [
    { label: 'Scanner', to: '/scanner' },
    { label: 'Members', to: '/members' },
    { label: 'Clients', to: '/trainer/clients' },
    { label: 'Exercises', to: '/exercises' },
    { label: 'My classes' },
  ],
  ADMIN: [
    { label: 'Members', to: '/members' },
    { label: 'Scanner', to: '/scanner' },
    { label: 'Exercises', to: '/exercises' },
    { label: 'Status', to: '/status' },
  ],
}

function navTabClass({ isActive }: { isActive: boolean }) {
  return `rounded-full px-4 py-2 text-sm font-medium transition ${
    isActive ? 'bg-ink text-on-accent' : 'text-ink hover:bg-surface-2'
  }`
}

function authLinkClass({ isActive }: { isActive: boolean }) {
  return `text-sm font-medium ${isActive ? 'text-accent' : 'text-ink'}`
}

function NavItemLink({ item }: { item: NavItem }) {
  if (!item.to) {
    return (
      <span
        title="Coming soon"
        className="cursor-default rounded-full px-4 py-2 text-sm font-medium text-muted opacity-60"
      >
        {item.label}
      </span>
    )
  }

  return (
    <NavLink to={item.to} className={navTabClass}>
      {item.label}
    </NavLink>
  )
}

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <>
      <header className="flex flex-wrap items-center gap-x-6 gap-y-3 px-4 py-3.5 md:px-8 md:py-4">
        <div className="md:flex-1">
          <Link to="/" className="flex w-fit items-center gap-2.5 text-lg font-bold text-ink">
            <span className="grid size-8 place-items-center rounded-full bg-accent text-[15px] text-on-accent">
              F
            </span>
            Fitness App
          </Link>
        </div>

        {user && (
          <nav className="order-3 flex w-full flex-wrap justify-center gap-1 rounded-card bg-surface p-1.5 md:order-none md:mx-auto md:w-auto md:rounded-full">
            <NavLink to="/" end className={navTabClass}>
              Home
            </NavLink>
            {roleLinks[user.role].map((item) => (
              <NavItemLink key={item.label} item={item} />
            ))}
          </nav>
        )}

        <div className="ml-auto flex items-center justify-end gap-3 text-sm text-ink md:flex-1">
          {user ? (
            <>
              <span className="hidden md:inline">
                {user.firstName} {user.lastName}
              </span>
              <button type="button" className={secondaryButton} onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={authLinkClass}>
                Log in
              </NavLink>
              <NavLink to="/register" className={authLinkClass}>
                Sign up
              </NavLink>
            </>
          )}
        </div>
      </header>
      <main className="mx-auto w-full max-w-[1100px] flex-1 px-4 pt-4 pb-10 md:px-8 md:pt-6 md:pb-12">
        <Outlet />
      </main>
    </>
  )
}
