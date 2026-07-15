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
      className={`min-h-12 w-full rounded-2xl bg-gradient-to-r from-blue-600 via-sky-500 to-blue-500 px-5 py-3 text-sm font-black text-white shadow-lg shadow-blue-600/25 transition hover:shadow-xl hover:shadow-blue-600/30 disabled:cursor-not-allowed disabled:opacity-70 ${className}`}
      {...buttonProps}
    >
      {loading ? 'Please wait...' : children}
    </motion.button>
  )
}

export default PrimaryButton
