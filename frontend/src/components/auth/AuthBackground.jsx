import { motion } from 'framer-motion'

const particleClasses = [
  'particle particle-one',
  'particle particle-two',
  'particle particle-three',
  'particle particle-four',
  'particle particle-five',
  'particle particle-six',
  'particle particle-seven',
  'particle particle-eight',
]

/*
Purpose
Animated visual layer for the auth landing page.
Responsibilities
Render abstract motion, particles, glass reflections, and depth behind the auth card.
Props
None.
*/
function AuthBackground() {
  return (
    <div className="absolute inset-0 overflow-hidden" aria-hidden="true">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,#2563eb_0%,transparent_30%),linear-gradient(135deg,#020617_0%,#0f172a_48%,#1e293b_100%)]" />
      <motion.div
        className="aurora-field aurora-field-primary"
        animate={{ x: [0, 26, -18, 0], y: [0, -20, 18, 0], rotate: [0, 4, -3, 0] }}
        transition={{ duration: 16, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.div
        className="aurora-field aurora-field-secondary"
        animate={{ x: [0, -30, 22, 0], y: [0, 24, -16, 0], rotate: [0, -5, 4, 0] }}
        transition={{ duration: 18, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.div
        className="aurora-field aurora-field-accent"
        animate={{ x: [0, 18, -26, 0], y: [0, 14, -22, 0], rotate: [0, 6, -4, 0] }}
        transition={{ duration: 20, repeat: Infinity, ease: 'easeInOut' }}
      />

      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.035)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.035)_1px,transparent_1px)] bg-[size:44px_44px] opacity-50" />
      <div className="absolute inset-0 backdrop-blur-[1px]" />

      {particleClasses.map((className) => (
        <motion.span
          key={className}
          className={className}
          animate={{ y: [0, -28, 0], opacity: [0.25, 0.9, 0.25] }}
          transition={{ duration: 5.5, repeat: Infinity, ease: 'easeInOut' }}
        />
      ))}
    </div>
  )
}

export default AuthBackground
