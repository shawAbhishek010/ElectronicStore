import apiClient from './apiClient.js'

/*
Purpose
Authentication API functions.
Responsibilities
Keep login/register endpoint calls in one place instead of inside UI components.
Props
None.
*/
export const loginUser = async (credentials) => {
  const response = await apiClient.post('/auth/login', credentials)
  return response.data
}

export const registerUserAccount = async (payload) => {
  const response = await apiClient.post('/auth/register', payload)
  return response.data
}
