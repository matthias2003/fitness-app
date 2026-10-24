import { useState, type SubmitEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth, type RegisterData } from '../auth/useAuth'
import { AuthLayout } from '../components/AuthLayout'
import {
  field,
  fieldError,
  fieldLabel,
  formError,
  pageTitle,
  primaryButton,
} from '../components/styles'

const emptyForm: RegisterData = { email: '', password: '', firstName: '', lastName: '' }

export function RegisterPage() {
  const { user, register, login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState<RegisterData>(emptyForm)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/" replace />

  function update(field: keyof RegisterData, value: string) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    const email = form.email.trim()
    try {
      await register({ ...form, email })
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : String(reason))
      if (reason instanceof ApiError) setFieldErrors(reason.errors)
      setSubmitting(false)
      return
    }
    try {
      await login(email, form.password)
      navigate('/', { replace: true })
    } catch {
      navigate('/login', { replace: true })
    }
  }

  return (
    <AuthLayout>
      <form
        className="flex flex-col justify-center gap-4 rounded-card bg-surface p-7 md:p-10"
        onSubmit={handleSubmit}
      >
        <h2 className={`${pageTitle} mb-2`}>Create your account</h2>
        {error && <p className={formError}>{error}</p>}

        <label className={fieldLabel}>
          First name
          <input
            className={field}
            value={form.firstName}
            onChange={(e) => update('firstName', e.target.value)}
            autoComplete="given-name"
            required
          />
          {fieldErrors.firstName && <small className={fieldError}>{fieldErrors.firstName}</small>}
        </label>

        <label className={fieldLabel}>
          Last name
          <input
            className={field}
            value={form.lastName}
            onChange={(e) => update('lastName', e.target.value)}
            autoComplete="family-name"
            required
          />
          {fieldErrors.lastName && <small className={fieldError}>{fieldErrors.lastName}</small>}
        </label>

        <label className={fieldLabel}>
          Email
          <input
            className={field}
            type="email"
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            autoComplete="email"
            maxLength={255}
            required
          />
          {fieldErrors.email && <small className={fieldError}>{fieldErrors.email}</small>}
        </label>

        <label className={fieldLabel}>
          Password
          <input
            className={field}
            type="password"
            value={form.password}
            onChange={(e) => update('password', e.target.value)}
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
          />
          {fieldErrors.password && <small className={fieldError}>{fieldErrors.password}</small>}
        </label>

        <button type="submit" className={`${primaryButton} mt-2`} disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>

        <p className="text-center text-sm">
          Already have an account?{' '}
          <Link to="/login" className="text-accent underline">
            Log in
          </Link>
        </p>
      </form>
    </AuthLayout>
  )
}
