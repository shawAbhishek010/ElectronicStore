import apiClient from './apiClient.js'

export const createOrder = async (payload) => {
  const response = await apiClient.post('/orders', payload)
  return response.data
}

export const createRazorpayOrder = async (payload) => {
  const response = await apiClient.post('/orders/razorpay', payload)
  return response.data
}

export const verifyRazorpayPayment = async (payload) => {
  const response = await apiClient.post('/orders/razorpay/verify', payload)
  return response.data
}

export const reportRazorpayPaymentFailure = async (payload) => {
  const response = await apiClient.post('/orders/razorpay/failure', payload)
  return response.data
}

export const getUserOrders = async (userId) => {
  const response = await apiClient.get(`/orders/users/${userId}`)
  return response.data || []
}
