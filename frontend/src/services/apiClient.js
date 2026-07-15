import axios from 'axios'

/*
Purpose
Shared Axios client for backend communication.
Responsibilities
Set base URL, attach JWT token, and normalize common API errors.
Props
None.
*/
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  // Step 1: Read token saved after login.
  const token = localStorage.getItem('electronic_store_token')

  // Step 2: Attach Authorization header only when token is available.
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  // Step 3: Return config so Axios can continue the request.
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('electronic_store_token')
      localStorage.removeItem('electronic_store_user')

      const authPaths = ['/auth', '/login', '/signup']
      if (!authPaths.includes(window.location.pathname)) {
        window.location.assign('/login')
      }
    }

    const message =
      error.response?.data?.message ||
      error.response?.data?.email ||
      error.response?.data?.password ||
      'Something went wrong. Please try again.'

    return Promise.reject(new Error(message))
  },
)

export default apiClient
