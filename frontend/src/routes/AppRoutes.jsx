import { Navigate, Route, Routes } from 'react-router-dom'
import AuthLandingPage from '../pages/AuthLandingPage.jsx'
import MainStorePage from '../pages/MainStorePage.jsx'
import { useAuth } from '../hooks/useAuth.js'

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()

  return isAuthenticated ? children : <Navigate to="/login" replace />
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
          <ProtectedRoute>
            <MainStorePage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default AppRoutes
