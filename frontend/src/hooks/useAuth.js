import { useContext } from 'react'
import AuthContext from '../context/authContextObject.js'

/*
Purpose
Reusable hook for reading authentication state.
Responsibilities
Expose AuthContext safely and warn developers when used outside AuthProvider.
Props
None.
*/
export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
