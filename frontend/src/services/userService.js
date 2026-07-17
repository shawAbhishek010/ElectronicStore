/*
Purpose:
Wraps user profile API calls for loading and updating the current user.
*/
import apiClient from './apiClient.js'

export const getUserProfile = async (userId) => {
  const response = await apiClient.get(`/users/getSingle/${userId}`)
  return response.data
}

export const updateUserProfile = async (userId, payload) => {
  const response = await apiClient.put(`/users/update/${userId}`, payload)
  return response.data
}
