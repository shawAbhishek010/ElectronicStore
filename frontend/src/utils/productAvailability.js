/*
Purpose:
Normalizes product inventory checks so catalog sections do not disagree about availability.
*/
export const getProductQuantity = (product) => {
  const quantity = Number(product?.quantity)
  return Number.isFinite(quantity) ? quantity : 0
}

export const isProductAvailable = (product) => {
  if (!product || product.live === false) return false

  const hasQuantity = getProductQuantity(product) > 0
  if (typeof product.stock === 'boolean') return product.stock && hasQuantity

  return hasQuantity
}
