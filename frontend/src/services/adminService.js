/*
Purpose:
Wraps admin API calls for analytics, products, categories, and order status updates.
*/
import apiClient from './apiClient.js'

export const getAdminAnalytics = async () => {
  const response = await apiClient.get('/admin/analytics')
  return response.data
}

export const getAllProductsForAdmin = async () => {
  const response = await apiClient.get('/products', {
    params: { pageNumber: 0, pageSize: 300, sortBy: 'addedDate', sortDir: 'desc' },
  })
  return response.data.content || []
}

export const getAllOrdersForAdmin = async () => {
  const response = await apiClient.get('/orders', {
    params: { pageNumber: 0, pageSize: 300, sortBy: 'orderedDate', sortDir: 'desc' },
  })
  return response.data.content || []
}

export const createCategory = async (payload) => {
  const response = await apiClient.post('/categories', payload)
  return response.data
}

export const updateCategory = async (categoryId, payload) => {
  const response = await apiClient.put(`/categories/${categoryId}`, payload)
  return response.data
}

export const deleteCategory = async (categoryId) => {
  const response = await apiClient.delete(`/categories/${categoryId}`)
  return response.data
}

export const createProduct = async (payload) => {
  const { categoryId, ...product } = payload
  const endpoint = categoryId ? `/categories/${categoryId}/products` : '/products'
  const response = await apiClient.post(endpoint, product)
  return response.data
}

export const updateProduct = async (productId, payload) => {
  const { categoryId, ...product } = payload
  const response = await apiClient.put(`/products/${productId}`, product)

  if (categoryId) {
    const categoryResponse = await apiClient.put(`/categories/${categoryId}/products/${productId}`)
    return categoryResponse.data
  }

  return response.data
}

export const deleteProduct = async (productId) => {
  const response = await apiClient.delete(`/products/${productId}`)
  return response.data
}

export const updateOrderStatus = async (orderId, status) => {
  const response = await apiClient.put(`/orders/${orderId}/status`, null, { params: { status } })
  return response.data
}
