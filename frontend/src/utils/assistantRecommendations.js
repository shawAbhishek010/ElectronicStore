import { getDiscountPercent, getProductPrice } from './productPricing.js'
import { getProductQuantity, isProductAvailable } from './productAvailability.js'

const categoryKeywords = {
  audio: ['audio', 'speaker', 'headphone', 'earphone', 'earbuds', 'sound', 'music', 'bass'],
  gaming: ['game', 'gaming', 'console', 'controller', 'play', 'stream'],
  laptops: ['laptop', 'notebook', 'work', 'study', 'office', 'creator', 'coding'],
  smartphones: ['phone', 'mobile', 'smartphone', 'camera', 'selfie', 'android'],
  televisions: ['tv', 'television', 'display', 'screen', 'movie', 'streaming'],
  accessories: ['accessory', 'charger', 'keyboard', 'mouse', 'cable', 'watch'],
  'home appliances': ['home', 'appliance', 'kitchen', 'cleaning', 'cooling'],
}

const normalize = (value = '') => value.toString().toLowerCase().replace(/[^a-z0-9 ]/g, ' ').replace(/\s+/g, ' ').trim()

const extractBudget = (question) => {
  const cleaned = normalize(question.replace(/,/g, ''))
  const match = cleaned.match(/(?:under|below|less than|within|budget|upto|up to|around|near)\s*(?:rs|inr)?\s*(\d+(?:\.\d+)?)\s*(k|lakh|lac)?/)
    || cleaned.match(/(?:rs|inr)?\s*(\d+(?:\.\d+)?)\s*(k|lakh|lac)\s*(?:budget|range)?/)

  if (!match) return null

  const amount = Number(match[1])
  const suffix = match[2]
  if (Number.isNaN(amount)) return null
  if (suffix === 'k') return amount * 1000
  if (suffix === 'lakh' || suffix === 'lac') return amount * 100000
  return amount
}

const getProductText = (product) =>
  normalize([product.title, product.description, product.category?.title].filter(Boolean).join(' '))

const getSignals = ({ product, recommendedIds, discountedIds, wishlistIds, cartProductIds, recentlyViewedIds }) => {
  const signals = []
  if (recommendedIds.has(product.productId)) signals.push('recommended')
  if (discountedIds.has(product.productId)) signals.push('discounted')
  if (wishlistIds.has(product.productId)) signals.push('in wishlist')
  if (cartProductIds.has(product.productId)) signals.push('in cart')
  if (recentlyViewedIds.has(product.productId)) signals.push('recently viewed')
  return signals
}

const scoreProduct = ({ product, question, budget, signals }) => {
  const productText = getProductText(product)
  const terms = normalize(question).split(' ').filter((term) => term.length > 2)
  const categoryTitle = normalize(product.category?.title)
  let score = isProductAvailable(product) ? 3 : -4

  terms.forEach((term) => {
    if (productText.includes(term)) score += 2
  })

  Object.entries(categoryKeywords).forEach(([category, keywords]) => {
    if (categoryTitle === category && keywords.some((keyword) => normalize(question).includes(keyword))) {
      score += 8
    }
  })

  if (budget) {
    const price = getProductPrice(product)
    if (price <= budget) score += 8
    if (price > budget * 1.15) score -= 10
  }

  score += Math.min(getDiscountPercent(product), 30) / 5
  score += signals.length
  return score
}

export const buildAssistantProductContext = ({
  products,
  question,
  recommendedProducts,
  discountedProducts,
  recentlyViewed,
  wishlist,
  cartItems,
  limit = 30,
}) => {
  const budget = extractBudget(question)
  const recommendedIds = new Set(recommendedProducts.map((product) => product.productId))
  const discountedIds = new Set(discountedProducts.map((product) => product.productId))
  const wishlistIds = new Set(wishlist.map((product) => product.productId))
  const recentlyViewedIds = new Set(recentlyViewed.map((product) => product.productId))
  const cartProductIds = new Set(cartItems.map((item) => item.product?.productId).filter(Boolean))

  return products
    .map((product) => {
      const signals = getSignals({ product, recommendedIds, discountedIds, wishlistIds, cartProductIds, recentlyViewedIds })
      return {
        product,
        signals,
        score: scoreProduct({ product, question, budget, signals }),
      }
    })
    .sort((first, second) => second.score - first.score)
    .slice(0, limit)
    .map(({ product, signals }) => ({
      id: product.productId,
      title: product.title,
      category: product.category?.title || 'Uncategorized',
      description: product.description || '',
      price: getProductPrice(product),
      originalPrice: product.price || getProductPrice(product),
      discountPercent: getDiscountPercent(product),
      stock: isProductAvailable(product),
      quantity: getProductQuantity(product),
      signals,
    }))
}

export const buildAssistantFallbackAnswer = (question, productContext) => {
  const availableProducts = productContext.filter((product) => product.stock)

  if (!availableProducts.length) {
    return 'I could not find an in-stock match from the current catalog. Try changing category, budget, or search words in the catalog filters.'
  }

  const topProducts = availableProducts.slice(0, 3)
  const suggestions = topProducts
    .map((product) => {
      const discount = product.discountPercent > 0 ? `, ${product.discountPercent}% off` : ''
      return `${product.title} at INR ${product.price}${discount}`
    })
    .join('; ')

  return `Based on "${question}", I would start with: ${suggestions}. Compare these by category, stock, and final price before adding one to cart.`
}
