import { motion } from 'framer-motion'
import { Navigate } from 'react-router-dom'
import AuthBackground from '../components/auth/AuthBackground.jsx'
import AuthCard from '../components/auth/AuthCard.jsx'
import { useAuth } from '../hooks/useAuth.js'

/*
Purpose
First landing screen for customers before they enter the store.
Responsibilities
Show the animated premium background and place the login/register card above it.
Props
None.
*/
function AuthLandingPage({ initialMode = 'login' }) {
  const { isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return <Navigate to="/store" replace />
  }

  return (
    <main className="relative min-h-screen overflow-hidden bg-slate-950 text-slate-950">
      <AuthBackground />

      <section className="relative z-10 flex min-h-screen items-center justify-center px-4 py-8 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 24 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.65, ease: 'easeOut' }}
          className="w-full max-w-[1080px]"
        >
          <AuthCard initialMode={initialMode} />
        </motion.div>
      </section>
    </main>
  )
}

export default AuthLandingPage
