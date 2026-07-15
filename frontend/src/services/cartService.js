import apiClient from './apiClient.js'

export const getCart = async (userId) => {
  const response = await apiClient.get(`/carts/${userId}`)
  return response.data
}

export const addCartItem = async (userId, productId, quantity = 1) => {
  const response = await apiClient.post(`/carts/${userId}`, { productId, quantity })
  return response.data
}

export const updateCartItemQuantity = async (userId, itemId, quantity) => {
  const response = await apiClient.patch(`/carts/${userId}/items/${itemId}`, { quantity })
  return response.data
}

export const removeCartItem = async (userId, itemId) => {
  await apiClient.delete(`/carts/${userId}/items/${itemId}`)
  return getCart(userId)
}

export const clearCart = async (userId) => {
  await apiClient.delete(`/carts/${userId}`)
  return getCart(userId)
}
