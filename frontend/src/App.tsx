import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { Layout } from './components/Layout'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ScannerPage } from './pages/ScannerPage'
import { StatusPage } from './pages/StatusPage'
import { MembersPage } from './pages/admin/MembersPage'
import { CheckInsPage } from './pages/member/CheckInsPage'
import { MembershipPage } from './pages/member/MembershipPage'
import { MyQrPage } from './pages/member/MyQrPage'

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
            </Route>
            <Route element={<ProtectedRoute roles={['MEMBER']} />}>
              <Route path="/membership" element={<MembershipPage />} />
              <Route path="/qr" element={<MyQrPage />} />
              <Route path="/check-ins" element={<CheckInsPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="/status" element={<StatusPage />} />
              <Route path="/admin/members" element={<MembersPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ADMIN', 'TRAINER']} />}>
              <Route path="/scanner" element={<ScannerPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
