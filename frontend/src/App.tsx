import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { Layout } from './components/Layout'
import { ExercisesPage } from './pages/ExercisesPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ScannerPage } from './pages/ScannerPage'
import { StatusPage } from './pages/StatusPage'
import { MembersPage } from './pages/MembersPage'
import { CheckInsPage } from './pages/member/CheckInsPage'
import { MembershipPage } from './pages/member/MembershipPage'
import { MyQrPage } from './pages/member/MyQrPage'
import { PlanPage } from './pages/member/PlanPage'
import { ClientsPage } from './pages/trainer/ClientsPage'
import { PlanBuilderPage } from './pages/trainer/PlanBuilderPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route index element={<HomePage />} />
              <Route path="/exercises" element={<ExercisesPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['MEMBER']} />}>
              <Route path="/membership" element={<MembershipPage />} />
              <Route path="/qr" element={<MyQrPage />} />
              <Route path="/check-ins" element={<CheckInsPage />} />
              <Route path="/plan" element={<PlanPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="/status" element={<StatusPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ADMIN', 'TRAINER']} />}>
              <Route path="/scanner" element={<ScannerPage />} />
              <Route path="/members" element={<MembersPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['TRAINER']} />}>
              <Route path="/members/:memberId/plan" element={<PlanBuilderPage />} />
              <Route path="/trainer/clients" element={<ClientsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
