/*
Purpose:
Wraps wishlist API calls for loading, saving, and removing favorite products.
*/
import apiClient from './apiClient.js'

export const getWishlist = async (userId) => {
  const response = await apiClient.get(`/wishlist/${userId}`)
  return response.data || []
}

export const addWishlistProduct = async (userId, productId) => {
  const response = await apiClient.post(`/wishlist/${userId}/products/${productId}`)
  return response.data || []
}

export const removeWishlistProduct = async (userId, productId) => {
  await apiClient.delete(`/wishlist/${userId}/products/${productId}`)
  return getWishlist(userId)
}
