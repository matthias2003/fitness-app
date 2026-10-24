import { useEffect, useState } from 'react'
import './App.css'

type ServiceStatus = {
  service: string
  status: string
  database: string
}

type SystemStatus = {
  core: ServiceStatus
  training: ServiceStatus
}

function App() {
  const [status, setStatus] = useState<SystemStatus | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/status')
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        return response.json() as Promise<SystemStatus>
      })
      .then(setStatus)
      .catch((reason) => setError(String(reason)))
  }, [])

  return (
    <main className="status-page">
      <h1>Fitness App</h1>
      {error && <p className="status-error">Brak połączenia ze Spring API ({error})</p>}
      {!error && !status && <p>Sprawdzanie połączenia…</p>}
      {status && (
        <table className="status-table">
          <thead>
            <tr>
              <th>Usługa</th>
              <th>Status</th>
              <th>Baza danych</th>
            </tr>
          </thead>
          <tbody>
            {[status.core, status.training].map((service) => (
              <tr key={service.service}>
                <td>
                  <code>{service.service}</code>
                </td>
                <td data-state={service.status}>{service.status}</td>
                <td data-state={service.database}>{service.database}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}

export default App
