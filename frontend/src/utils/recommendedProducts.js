import { getDiscountPercent, getProductPrice } from './productPricing.js'

const getProductId = (product) => product?.productId
const getCategoryId = (product) => product?.category?.categoryId || product?.categoryId

const uniqueProducts = (products) => {
  const seen = new Set()

  return products.filter((product) => {
    const productId = getProductId(product)
    if (!productId || seen.has(productId)) return false
    seen.add(productId)
    return true
  })
}

const scoreProduct = (product, signals) => {
  const productPrice = getProductPrice(product)
  const productCategoryId = getCategoryId(product)
  let score = Math.min(getDiscountPercent(product), 40) / 20
  score += Math.min(product.quantity || 0, 30) / 30

  signals.forEach((signal, index) => {
    const recencyWeight = Math.max(1, 4 - index * 0.3)

    if (productCategoryId && productCategoryId === getCategoryId(signal)) {
      score += 3 * recencyWeight
    }

    const signalPrice = getProductPrice(signal)
    if (productPrice > 0 && signalPrice > 0) {
      const distance = Math.abs(productPrice - signalPrice) / Math.max(productPrice, signalPrice)
      score += Math.max(0, 1 - distance) * recencyWeight
    }
  })

  return score
}

const rankProducts = (products, signals) => {
  return [...products].sort((first, second) => {
    const scoreDifference = scoreProduct(second, signals) - scoreProduct(first, signals)
    if (scoreDifference !== 0) return scoreDifference

    const dateDifference = new Date(second.addedDate || 0) - new Date(first.addedDate || 0)
    if (dateDifference !== 0) return dateDifference

    return (first.title || '').localeCompare(second.title || '')
  })
}

export const getRecommendedProducts = ({
  products,
  cartItems = [],
  wishlist = [],
  recentlyViewed = [],
  activityProductIds = [],
  limit = 10,
}) => {
  if (!limit || !products.length) return []

  const productsById = new Map(products.map((product) => [getProductId(product), product]))
  const cartProducts = cartItems.map((item) => item.product).filter(Boolean)
  const sessionSignals = uniqueProducts(activityProductIds.map((productId) => productsById.get(productId)).filter(Boolean))
  const currentSignals = sessionSignals.length ? sessionSignals : uniqueProducts(recentlyViewed).slice(0, 4)
  const currentSignalIds = new Set(currentSignals.map(getProductId))
  const previousSignals = uniqueProducts([
    ...recentlyViewed.filter((product) => !currentSignalIds.has(getProductId(product))),
    ...wishlist,
    ...cartProducts,
  ])

  const excludedIds = new Set(uniqueProducts(cartProducts).map(getProductId))
  const candidates = products.filter(
    (product) =>
      product?.live !== false &&
      product?.stock &&
      product?.quantity > 0 &&
      !excludedIds.has(getProductId(product)),
  )

  const currentLimit = Math.min(4, limit)
  const currentRecommendations = rankProducts(candidates, currentSignals).slice(0, currentLimit)
  const selectedIds = new Set(currentRecommendations.map(getProductId))
  const remainingCandidates = candidates.filter((product) => !selectedIds.has(getProductId(product)))
  const previousRecommendations = rankProducts(remainingCandidates, previousSignals).slice(
    0,
    limit - currentRecommendations.length,
  )

  return [...currentRecommendations, ...previousRecommendations]
}
