import { getDiscountAmount, getDiscountPercent } from './productPricing.js'

export const getDiscountedProducts = (products, limit = 12) => {
  return products
    .filter((product) => product?.live !== false && product?.stock && getDiscountPercent(product) > 0)
    .sort((first, second) => {
      const discountDifference = getDiscountPercent(second) - getDiscountPercent(first)
      if (discountDifference !== 0) return discountDifference

      const savingsDifference = getDiscountAmount(second) - getDiscountAmount(first)
      if (savingsDifference !== 0) return savingsDifference

      return new Date(second.addedDate || 0) - new Date(first.addedDate || 0)
    })
    .slice(0, limit)
}
