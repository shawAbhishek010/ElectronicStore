/*
Purpose:
Provides product pricing helpers for display price and discount percentage calculations.
*/
export const getProductPrice = (product) => product?.discountedPrice || product?.price || 0

export const getDiscountPercent = (product) => {
  if (!product?.price || !product?.discountedPrice || product.discountedPrice >= product.price) return 0
  return Math.round(((product.price - product.discountedPrice) / product.price) * 100)
}

export const getDiscountAmount = (product) => {
  if (!product?.price || !product?.discountedPrice || product.discountedPrice >= product.price) return 0
  return product.price - product.discountedPrice
}
