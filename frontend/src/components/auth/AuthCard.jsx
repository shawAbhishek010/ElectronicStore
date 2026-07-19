import { AnimatePresence, motion } from 'framer-motion'
import { useEffect, useState } from 'react'
import { FiGrid, FiLock, FiShoppingBag, FiTruck, FiZap } from 'react-icons/fi'
import LoginForm from './LoginForm.jsx'
import RegisterForm from './RegisterForm.jsx'

/*
Purpose
Glassmorphism authentication card for login and registration.
Responsibilities
Show branding, trust copy, and switch between login/register forms with animation.
Props
None.
*/
function AuthCard({ initialMode = 'login' }) {
  const [mode, setMode] = useState(initialMode)
  const isLogin = mode === 'login'

  useEffect(() => {
    setMode(initialMode)
  }, [initialMode])

  return (
    <div className="grid min-h-[690px] overflow-hidden rounded-lg border border-zinc-300/20 bg-white/10 shadow-2xl shadow-zinc-900/45 backdrop-blur-xl lg:grid-cols-[1fr_460px]">
      <aside className="relative hidden min-h-[690px] overflow-hidden bg-zinc-800 text-white lg:block">
        <img
          className="absolute inset-0 h-full w-full object-cover object-center"
          src="/images/electronics-auth-showroom.png"
          alt="Premium electronics displayed in a modern showroom"
        />
        <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(9,9,11,0.1)_18%,rgba(9,9,11,0.42)_58%,rgba(9,9,11,0.96)_100%)]" />
        <div className="absolute inset-x-0 bottom-0 h-64 bg-[linear-gradient(180deg,transparent,rgba(24,24,27,0.52))]" />

        <div className="relative flex min-h-[690px] flex-col justify-start p-10 xl:p-12">
          <h1 className="max-w-md text-5xl font-black leading-tight text-white">
            SparkGadget
          </h1>
          <p className="mt-4 max-w-lg text-lg leading-7 text-zinc-200">
            Premium electronics with a secure, reliable shopping experience.
          </p>

          <div className="mt-6 flex flex-wrap gap-x-6 gap-y-3 text-sm font-bold text-zinc-100">
            <HeroDetail icon={FiLock} label="Secure session" />
            <HeroDetail icon={FiZap} label="Fast checkout" />
            <HeroDetail icon={FiTruck} label="Tracked delivery" />
          </div>
        </div>
      </aside>

      <section className="relative flex bg-zinc-200/95 p-4 backdrop-blur-xl sm:p-6">
        <div className="w-full rounded-lg border border-zinc-300 bg-zinc-100 p-6 shadow-xl shadow-zinc-900/10 sm:p-8">
          <div className="mb-7 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="grid h-12 w-12 place-items-center rounded-lg bg-zinc-800 text-white shadow-lg shadow-zinc-900/20">
                <FiShoppingBag className="text-2xl" />
              </div>
              <div>
                <p className="text-sm font-bold uppercase text-zinc-950">Welcome</p>
                <h2 className="text-2xl font-black text-zinc-950">SparkGadget</h2>
              </div>
            </div>
            <span className="hidden rounded-lg border border-zinc-200 bg-zinc-200 px-3 py-2 text-xs font-black uppercase text-zinc-700 sm:inline-flex">
              Pro Suite
            </span>
          </div>

          <div className="mb-5 grid grid-cols-3 gap-2">
            <MiniTrust icon={FiGrid} label="Catalog" />
            <MiniTrust icon={FiLock} label="Secure" />
            <MiniTrust icon={FiTruck} label="Tracked" />
          </div>

          <div className="mb-7 grid grid-cols-2 rounded-lg bg-zinc-100 p-1">
            <button
              type="button"
              onClick={() => setMode('login')}
              className={`rounded-lg px-4 py-3 text-sm font-bold transition ${isLogin ? 'bg-zinc-200 text-zinc-950 shadow-sm' : 'text-zinc-500 hover:text-zinc-900'}`}
            >
              Login
            </button>
            <button
              type="button"
              onClick={() => setMode('register')}
              className={`rounded-lg px-4 py-3 text-sm font-bold transition ${!isLogin ? 'bg-zinc-200 text-zinc-950 shadow-sm' : 'text-zinc-500 hover:text-zinc-900'}`}
            >
              Register
            </button>
          </div>

          <AnimatePresence mode="wait">
            {isLogin ? (
              <motion.div
                key="login"
                initial={{ opacity: 0, x: -18 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 18 }}
                transition={{ duration: 0.28 }}
              >
                <LoginForm onCreateAccount={() => setMode('register')} />
              </motion.div>
            ) : (
              <motion.div
                key="register"
                initial={{ opacity: 0, x: 18 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -18 }}
                transition={{ duration: 0.28 }}
              >
                <RegisterForm onLogin={() => setMode('login')} />
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </section>
    </div>
  )
}

function HeroDetail({ icon: Icon, label }) {
  return (
    <span className="inline-flex items-center gap-2">
      <Icon className="text-lg text-white" />
      {label}
    </span>
  )
}

function MiniTrust({ icon: Icon, label }) {
  return (
    <div className="grid place-items-center rounded-lg border border-zinc-200 bg-zinc-200 px-2 py-3 text-center text-xs font-black text-zinc-600">
      <Icon className="mb-1 text-lg text-zinc-950" />
      {label}
    </div>
  )
}

export default AuthCard
