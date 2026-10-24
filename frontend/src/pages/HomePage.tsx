import { Link } from 'react-router-dom'
import { roleLabels } from '../auth/roles'
import { useAuth, type Role } from '../auth/useAuth'
import { pageTitle, tag } from '../components/styles'

type Tile = {
  title: string
  description: string
  to?: string
}

const roleTiles: Record<Role, Tile[]> = {
  MEMBER: [
    { title: 'My membership', description: 'Buy and track your pass', to: '/membership' },
    { title: 'My QR code', description: 'Show it at the front desk', to: '/qr' },
    { title: 'Check-in history', description: 'Your gym visits', to: '/check-ins' },
    { title: 'Class schedule', description: 'Book group classes' },
    { title: 'Workout log', description: 'Log sets and see progress' },
  ],
  TRAINER: [
    { title: 'Check-in scanner', description: 'Scan member QR codes', to: '/scanner' },
    { title: 'My classes', description: 'Your upcoming sessions' },
    { title: 'Clients', description: 'People you train' },
    { title: 'Training plans', description: 'Build plans for clients' },
  ],
  ADMIN: [
    { title: 'Members', description: 'Members and their passes', to: '/admin/members' },
    { title: 'Check-in scanner', description: 'Scan member QR codes', to: '/scanner' },
    { title: 'Class schedule', description: 'Plan group classes' },
  ],
}

const tileClass = 'flex min-h-[150px] flex-col gap-1.5 rounded-card p-[22px]'

function greeting() {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 18) return 'Good afternoon'
  return 'Good evening'
}

export function HomePage() {
  const { user } = useAuth()
  if (!user) return null

  return (
    <section>
      <p className="text-[15px]">{greeting()}</p>
      <h2 className={`${pageTitle} mt-1`}>
        {user.firstName} {user.lastName}
      </h2>
      <div className="mt-7 grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-4">
        <article className={`${tileClass} bg-accent`}>
          <span className={`${tag} mb-auto bg-on-accent text-accent`}>{roleLabels[user.role]}</span>
          <h3 className="text-lg font-semibold tracking-tight text-on-accent">Your account</h3>
          <p className="text-sm text-on-accent">{user.email}</p>
        </article>
        {roleTiles[user.role].map((tile) => (
          <HomeTile key={tile.title} tile={tile} />
        ))}
      </div>
    </section>
  )
}

function HomeTile({ tile }: { tile: Tile }) {
  if (!tile.to) {
    return (
      <article className={`${tileClass} bg-surface`}>
        <span className={`${tag} mb-auto bg-surface-2 text-muted`}>Coming soon</span>
        <h3 className="text-lg font-semibold tracking-tight text-ink">{tile.title}</h3>
        <p className="text-sm">{tile.description}</p>
      </article>
    )
  }

  return (
    <Link to={tile.to} className={`${tileClass} bg-surface transition hover:bg-surface-2`}>
      <span className={`${tag} mb-auto bg-accent text-on-accent`}>Open →</span>
      <h3 className="text-lg font-semibold tracking-tight text-ink">{tile.title}</h3>
      <p className="text-sm">{tile.description}</p>
    </Link>
  )
}
