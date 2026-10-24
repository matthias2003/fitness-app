import type { ReactNode } from 'react'

const heroBackground = [
  'radial-gradient(circle at 80% 15%, rgba(227, 242, 74, 0.35), transparent 45%)',
  'radial-gradient(circle at 10% 90%, rgba(255, 90, 54, 0.25), transparent 50%)',
  'repeating-linear-gradient(135deg, #1c1c1c 0 2px, transparent 2px 14px)',
].join(', ')

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="grid gap-6 md:min-h-[560px] md:grid-cols-[1.1fr_1fr]">
      <section
        className="flex min-h-[220px] flex-col justify-end overflow-hidden rounded-card bg-[#141414] p-7 md:p-10"
        style={{ backgroundImage: heroBackground }}
      >
        <h1 className="text-[clamp(34px,4.5vw,54px)] leading-none font-bold tracking-tight text-ink uppercase">
          Be healthy
          <br />
          Be stronger
          <br />
          Be yourself
        </h1>
        <p className="mt-4 max-w-[340px]">
          Memberships, classes and your training log in one place.
        </p>
      </section>
      {children}
    </div>
  )
}
