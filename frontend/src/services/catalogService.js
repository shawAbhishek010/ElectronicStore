import apiClient from './apiClient.js'

export const getCategories = async () => {
  const response = await apiClient.get('/categories', {
    params: { pageNumber: 0, pageSize: 100, sortBy: 'title', sortDir: 'asc' },
  })
  return response.data.content || []
}

export const getProducts = async () => {
  const response = await apiClient.get('/products/live', {
    params: { pageNumber: 0, pageSize: 200, sortBy: 'addedDate', sortDir: 'desc' },
  })
  return response.data.content || []
}

export const searchProducts = async (query) => {
  if (!query.trim()) return getProducts()

  const response = await apiClient.get(`/products/search/${encodeURIComponent(query.trim())}`, {
    params: { pageNumber: 0, pageSize: 200, sortBy: 'title', sortDir: 'asc' },
  })
  return response.data.content || []
}
