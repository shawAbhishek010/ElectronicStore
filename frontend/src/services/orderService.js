import apiClient from './apiClient.js'

export const createOrder = async (payload) => {
  const response = await apiClient.post('/orders', payload)
  return response.data
}

export const getUserOrders = async (userId) => {
  const response = await apiClient.get(`/orders/users/${userId}`)
  return response.data || []
}
