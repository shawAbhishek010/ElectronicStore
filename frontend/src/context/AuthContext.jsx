import { useCallback, useMemo, useState } from 'react'
import { loginUser, registerUserAccount } from '../services/authService.js'
import AuthContext from './authContextObject.js'

const storedUser = localStorage.getItem('electronic_store_user')

/*
Purpose
Global authentication state for the React app.
Responsibilities
Store logged-in user, persist JWT token, and expose login/register/logout actions.
Props
children renders the application below the provider.
*/
export function AuthProvider({ children }) {
  const [user, setUser] = useState(storedUser ? JSON.parse(storedUser) : null)
  const [token, setToken] = useState(localStorage.getItem('electronic_store_token'))

  const login = async (credentials) => {
    // Step 1: Call backend login API.
    const response = await loginUser(credentials)

    // Step 2: Save token and user details for later secured API requests.
    localStorage.setItem('electronic_store_token', response.token)
    localStorage.setItem('electronic_store_user', JSON.stringify(response))
    setToken(response.token)
    setUser(response)

    // Step 3: Return response so pages can navigate or show success later.
    return response
  }

  const registerUser = async (payload) => {
    return registerUserAccount(payload)
  }

  const updateStoredUser = useCallback((profile) => {
    setUser((currentUser) => {
      const nextUser = {
        ...(currentUser || {}),
        ...profile,
        token,
      }

      localStorage.setItem('electronic_store_user', JSON.stringify(nextUser))
      return nextUser
    })
  }, [token])

  const logout = () => {
    localStorage.removeItem('electronic_store_token')
    localStorage.removeItem('electronic_store_user')
    setToken(null)
    setUser(null)
  }

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token),
      login,
      registerUser,
      updateStoredUser,
      logout,
    }),
    [user, token, updateStoredUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
