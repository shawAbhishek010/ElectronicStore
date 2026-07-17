/*
Purpose:
Wraps product-view APIs used for recently viewed product tracking.
*/
import apiClient from './apiClient.js'

export const getRecentlyViewed = async (userId) => {
  const response = await apiClient.get(`/product-views/${userId}`)
  return response.data || []
}

export const trackProductView = async (userId, productId) => {
  const response = await apiClient.post(`/product-views/${userId}/products/${productId}`)
  return response.data
}
