import { motion } from 'framer-motion'

/*
Purpose
Reusable primary action button.
Responsibilities
Show gradient styling, loading state, and button motion consistently.
Props
children, loading, className, and normal button props.
*/
function PrimaryButton({ children, loading = false, className = '', ...buttonProps }) {
  return (
    <motion.button
      whileHover={{ y: -2 }}
      whileTap={{ scale: 0.98 }}
      disabled={loading || buttonProps.disabled}
      className={`min-h-12 w-full rounded-lg bg-zinc-800 px-5 py-3 text-sm font-black text-white shadow-lg shadow-zinc-900/20 transition hover:bg-zinc-800 hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-70 ${className}`}
      {...buttonProps}
    >
      {loading ? 'Please wait...' : children}
    </motion.button>
  )
}

export default PrimaryButton
