import { Navigate, Route, Routes } from 'react-router-dom'
import AdminDashboardPage from '../pages/AdminDashboardPage.jsx'
import AuthLandingPage from '../pages/AuthLandingPage.jsx'
import MainStorePage from '../pages/MainStorePage.jsx'
import { useAuth } from '../hooks/useAuth.js'

const normalizeRole = (role = '') => (role.startsWith('ROLE_') ? role : `ROLE_${role}`).toUpperCase()

function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />

  if (roles?.length && !roles.includes(normalizeRole(user?.role || 'ROLE_USER'))) {
    return <Navigate to={normalizeRole(user?.role || '') === 'ROLE_ADMIN' ? '/admin' : '/store'} replace />
  }

  return children
}

/*
Purpose
Central place for application routes.
Responsibilities
Map browser paths to page components and keep routing away from UI components.
Props
None.
*/
function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/auth" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<AuthLandingPage initialMode="login" />} />
      <Route path="/signup" element={<AuthLandingPage initialMode="register" />} />
      <Route
        path="/store"
        element={
          <ProtectedRoute roles={['ROLE_USER']}>
            <MainStorePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute roles={['ROLE_ADMIN']}>
            <AdminDashboardPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default AppRoutes
