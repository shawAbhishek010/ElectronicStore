/*
Purpose:
Exports the shared React auth context object used by the provider and auth hook.
*/
import { createContext } from 'react'

const AuthContext = createContext(null)

export default AuthContext
