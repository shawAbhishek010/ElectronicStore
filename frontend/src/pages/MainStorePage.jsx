/*
Purpose:
Renders the customer storefront with catalog browsing, cart, wishlist, checkout, orders, and profile history.
*/
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { useNavigate } from 'react-router-dom'
import {
  FiBriefcase,
  FiCheckCircle,
  FiCreditCard,
  FiGrid,
  FiHeart,
  FiHelpCircle,
  FiLogOut,
  FiMail,
  FiMapPin,
  FiMinus,
  FiPackage,
  FiPercent,
  FiPhone,
  FiPlus,
  FiSave,
  FiSearch,
  FiSend,
  FiShield,
  FiShoppingCart,
  FiTruck,
  FiTrash2,
  FiUser,
  FiX,
} from 'react-icons/fi'
import { useAuth } from '../hooks/useAuth.js'
import { getCategories, getProducts } from '../services/catalogService.js'
import { addCartItem, getCart, removeCartItem, updateCartItemQuantity } from '../services/cartService.js'
import {
  confirmDelivery,
  createRazorpayOrder,
  getUserOrders,
  reportRazorpayPaymentFailure,
  verifyRazorpayPayment,
} from '../services/orderService.js'
import { getRecentlyViewed, trackProductView } from '../services/productViewService.js'
import { getUserProfile, updateUserProfile } from '../services/userService.js'
import { addWishlistProduct, getWishlist, removeWishlistProduct } from '../services/wishlistService.js'
import { askAssistant } from '../services/assistantService.js'
import { buildAssistantFallbackAnswer, buildAssistantProductContext } from '../utils/assistantRecommendations.js'
import { getDiscountedProducts } from '../utils/discountedProducts.js'
import { getDiscountPercent, getProductPrice } from '../utils/productPricing.js'
import { getRecommendedProducts } from '../utils/recommendedProducts.js'

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
const razorpayCheckoutUrl = 'https://checkout.razorpay.com/v1/checkout.js'
const highValueGuidancePaise = 5_000_000
const standardUpiLimitPaise = 10_000_000
const catalogPageSize = 8
let razorpayScriptPromise = null

const getRazorpayDisplayConfig = (amountInPaise) => {
  if (amountInPaise <= highValueGuidancePaise) return null

  return {
    blocks: {
      high_value: {
        name: amountInPaise > standardUpiLimitPaise ? 'Available for this amount' : 'Recommended for this amount',
        instruments: [{ method: 'card' }, { method: 'netbanking' }],
      },
    },
    ...(amountInPaise > standardUpiLimitPaise ? { hide: [{ method: 'upi' }] } : {}),
    sequence: ['block.high_value'],
    preferences: {
      show_default_blocks: amountInPaise <= standardUpiLimitPaise,
    },
  }
}

const getRazorpayFailureMessage = (paymentError = {}) => {
  const description = paymentError.description || 'Payment failed. Please try again.'
  if (/maximum amount|amount exceeds|transaction limit/i.test(description)) {
    return `${description} Increase the domestic transaction limit in Razorpay Dashboard or use an eligible Card/Netbanking method.`
  }
  return description
}

const loadRazorpayCheckout = () =>
  new Promise((resolve, reject) => {
    if (window.Razorpay) {
      resolve(window.Razorpay)
      return
    }

    if (!razorpayScriptPromise) {
      razorpayScriptPromise = new Promise((scriptResolve, scriptReject) => {
        const existingScript = document.querySelector(`script[src="${razorpayCheckoutUrl}"]`)

        if (existingScript?.dataset.loaded === 'true') {
          scriptResolve()
          return
        }

        const script = existingScript || document.createElement('script')
        script.src = razorpayCheckoutUrl
        script.async = true
        script.onload = () => {
          script.dataset.loaded = 'true'
          scriptResolve()
        }
        script.onerror = () => {
          razorpayScriptPromise = null
          scriptReject(new Error('Unable to load Razorpay Checkout.'))
        }

        if (!existingScript) {
          document.body.appendChild(script)
        }
      })
    }

    razorpayScriptPromise
      .then(() => {
        if (window.Razorpay) {
          resolve(window.Razorpay)
          return
        }

        razorpayScriptPromise = null
        reject(new Error('Razorpay Checkout is not ready. Please try again.'))
      })
      .catch(reject)
  })

const normalizeCategoryTitle = (title = '') => title.trim().replace(/\s+/g, ' ').toLowerCase()
const withImageParams = (url, width = 900) => {
  if (!url) return ''
  if ((!url.includes('images.unsplash.com') && !url.includes('plus.unsplash.com')) || url.includes('?')) return url
  return `${url}?auto=format&fit=crop&w=${width}&q=80`
}

const categoryImages = {
  accessories: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30',
  audio: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e',
  gaming: 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3',
  'home appliances': 'https://images.unsplash.com/photo-1556911220-bff31c812dba',
  laptops: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853',
  smartphones: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9',
  televisions: 'https://images.unsplash.com/photo-1593784991095-a205069470b6',
}

const productFallbackImages = {
  accessories: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3',
  audio: 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb',
  gaming: 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db',
  'home appliances': 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1',
  laptops: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8',
  smartphones: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97',
  televisions: 'https://images.unsplash.com/photo-1593784991095-a205069470b6',
}

const getCategoryImage = (category) =>
  withImageParams(category?.coverImage || categoryImages[normalizeCategoryTitle(category?.title)] || categoryImages.accessories)

const getProductFallbackImage = (product) =>
  withImageParams(productFallbackImages[normalizeCategoryTitle(product?.category?.title)] || productFallbackImages.accessories)

const getProductImage = (product) =>
  product.productImageName?.startsWith('http')
    ? withImageParams(product.productImageName)
    : product.productImageName
      ? `${apiBaseUrl}/products/image/${product.productId}`
      : getProductFallbackImage(product)

const formatOrderStatus = (status) => (status === 'DISPATCHED' ? 'SHIPPED' : status)
const getProfileAvatar = (user) => {
  const seed = encodeURIComponent(user?.email || user?.userId || user?.name || 'sparkgadget-user')
  return `https://api.dicebear.com/9.x/thumbs/svg?seed=${seed}&backgroundColor=0ea5e9,22d3ee,6366f1`
}
const isCompletedOrder = (order) => order.orderStatus === 'COMPLETED'
const isCurrentOrder = (order) => !isCompletedOrder(order) && order.paymentStatus !== 'PAYMENT_FAILED'

const heroSlides = [
  {
    id: 'phones',
    title: 'SparkGadget',
    subtitle: 'Fresh phones, laptops, audio, gaming gear, and home tech curated for faster buying.',
    eyebrow: 'New season picks',
    image:
      'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1600&q=80',
  },
  {
    id: 'desk',
    title: 'Upgrade your daily setup',
    subtitle: 'Build a cleaner desk with creator laptops, keyboards, portable power, and smart accessories.',
    eyebrow: 'Work smarter',
    image:
      'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1600&q=80',
  },
  {
    id: 'audio',
    title: 'Sound, play, and stream better',
    subtitle: 'Find wireless audio, consoles, displays, and home essentials without endless scrolling.',
    eyebrow: 'Entertainment ready',
    image:
      'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1600&q=80',
  },
]

const footerColumns = [
  {
    title: 'About SparkGadget',
    links: ['Enterprise Edition', 'About us', 'Careers', 'Partner network', 'Spark stories'],
  },
  {
    title: 'Business',
    links: ['Become a seller', 'Bulk purchase', 'Corporate gifting', 'Procurement desk', 'Affiliate program'],
  },
  {
    title: 'Help',
    links: ['Payments', 'Shipping', 'Cancellation & returns', 'FAQ', 'Service center'],
  },
  {
    title: 'Consumer policy',
    links: ['Privacy', 'Security', 'Terms of use', 'Warranty support', 'Grievance redressal'],
  },
]

const enterpriseHighlights = [
  { icon: FiShield, label: 'Secure checkout' },
  { icon: FiTruck, label: 'Priority dispatch' },
  { icon: FiCreditCard, label: 'UPI & cards' },
  { icon: FiHelpCircle, label: 'Help center' },
]

const assistantQuickPrompts = [
  'Best phone under 30000',
  'Laptop for office work',
  'Good audio deals',
]

const assistantIntroMessage = {
  role: 'assistant',
  text: 'Tell me your budget, use, or preferred category and I will help narrow the catalog.',
}

const assistantAvatarUrl = '/images/assistant-avatar.png'

function MainStorePage() {
  const { user, logout, updateStoredUser } = useAuth()
  const navigate = useNavigate()
  const productSectionRef = useRef(null)
  const trackedViews = useRef(new Set())
  const assistantScrollRef = useRef(null)

  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [cart, setCart] = useState({ items: [] })
  const [wishlist, setWishlist] = useState([])
  const [recentlyViewed, setRecentlyViewed] = useState([])
  const [activityProductIds, setActivityProductIds] = useState([])
  const [orders, setOrders] = useState([])
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [priceRange, setPriceRange] = useState('all')
  const [stockFilter, setStockFilter] = useState('available')
  const [sortBy, setSortBy] = useState('newest')
  const [search, setSearch] = useState('')
  const [catalogVisibleCount, setCatalogVisibleCount] = useState(catalogPageSize)
  const [activePanel, setActivePanel] = useState(null)
  const [pageStatus, setPageStatus] = useState({ loading: true, error: '' })
  const [actionStatus, setActionStatus] = useState('')
  const [checkoutOpen, setCheckoutOpen] = useState(false)
  const [checkoutProcessing, setCheckoutProcessing] = useState(false)
  const [checkoutForm, setCheckoutForm] = useState({
    billingName: user?.name || '',
    billingPhone: '',
    billingAddress: '',
  })
  const [profileForm, setProfileForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
    gender: 'Other',
    about: '',
    currentPassword: '',
    newPassword: '',
  })
  const [profileStatus, setProfileStatus] = useState({ type: '', message: '' })
  const [profileLoading, setProfileLoading] = useState(false)
  const [activeHero, setActiveHero] = useState(0)
  const [assistantOpen, setAssistantOpen] = useState(false)
  const [assistantInput, setAssistantInput] = useState('')
  const [assistantLoading, setAssistantLoading] = useState(false)
  const [assistantMessages, setAssistantMessages] = useState([assistantIntroMessage])

  const refreshStore = useCallback(async () => {
    if (!user?.userId) return

    setPageStatus({ loading: true, error: '' })
    try {
      const [categoryData, productData, cartData, wishlistData, viewedData, orderData] = await Promise.all([
        getCategories(),
        getProducts(),
        getCart(user.userId),
        getWishlist(user.userId),
        getRecentlyViewed(user.userId),
        getUserOrders(user.userId),
      ])

      setCategories(categoryData)
      setProducts(productData)
      setCart(cartData)
      setWishlist(wishlistData)
      setRecentlyViewed(viewedData)
      setOrders(orderData)
    } catch (error) {
      setPageStatus({ loading: false, error: error.message })
      return
    }

    setPageStatus({ loading: false, error: '' })
  }, [user?.userId])

  useEffect(() => {
    refreshStore()
  }, [refreshStore])

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActiveHero((current) => (current + 1) % heroSlides.length)
    }, 3000)

    return () => window.clearInterval(timer)
  }, [])

  useEffect(() => {
    if (activePanel !== 'profile' || !user?.userId) return

    let ignoreResult = false

    const loadProfile = async () => {
      setProfileLoading(true)
      setProfileStatus({ type: '', message: '' })

      try {
        const profile = await getUserProfile(user.userId)
        if (!ignoreResult) {
          setProfileForm({
            name: profile.name || '',
            email: profile.email || '',
            gender: profile.gender || 'Other',
            about: profile.about || '',
            currentPassword: profile.password || '',
            newPassword: '',
          })
        }
      } catch (error) {
        if (!ignoreResult) setProfileStatus({ type: 'error', message: error.message })
      } finally {
        if (!ignoreResult) setProfileLoading(false)
      }
    }

    loadProfile()

    return () => {
      ignoreResult = true
    }
  }, [activePanel, user?.userId])

  useEffect(() => {
    if (!assistantOpen) return
    assistantScrollRef.current?.scrollTo({
      top: assistantScrollRef.current.scrollHeight,
      behavior: 'smooth',
    })
  }, [assistantMessages, assistantLoading, assistantOpen])

  const cartItems = useMemo(() => cart.items || [], [cart.items])
  const wishlistIds = useMemo(() => new Set(wishlist.map((product) => product.productId)), [wishlist])
  const currentOrders = useMemo(() => orders.filter(isCurrentOrder), [orders])
  const completedOrders = useMemo(() => orders.filter(isCompletedOrder), [orders])
  const cartCount = cartItems.reduce((total, item) => total + item.quantity, 0)
  const cartTotal = cartItems.reduce((total, item) => total + item.totalPrice, 0)
  const uniqueCategories = useMemo(() => {
    const categoryMap = new Map()
    categories.forEach((category) => {
      const key = normalizeCategoryTitle(category.title)
      const existing = categoryMap.get(key)
      if (!existing || category.categoryId === key.replace(/\s+/g, '-')) {
        categoryMap.set(key, category)
      }
    })

    return [...categoryMap.values()].sort((a, b) => a.title.localeCompare(b.title))
  }, [categories])
  const selectedCategoryKey = useMemo(() => {
    if (selectedCategory === 'all') return 'all'
    return normalizeCategoryTitle(categories.find((category) => category.categoryId === selectedCategory)?.title)
  }, [categories, selectedCategory])
  const productCountsByCategory = useMemo(() => {
    const counts = new Map()
    products.forEach((product) => {
      const key = normalizeCategoryTitle(product.category?.title)
      counts.set(key, (counts.get(key) || 0) + 1)
    })
    return counts
  }, [products])

  const { filteredProducts, searchUnavailable } = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()
    const categoryProducts = products.filter((product) => {
      const price = getProductPrice(product)
      const matchesCategory =
        selectedCategory === 'all' ||
        product.category?.categoryId === selectedCategory ||
        normalizeCategoryTitle(product.category?.title) === selectedCategoryKey
      const matchesStock = stockFilter === 'all' || (stockFilter === 'available' ? product.stock : !product.stock)
      const matchesPrice =
        priceRange === 'all' ||
        (priceRange === 'budget' && price < 10000) ||
        (priceRange === 'mid' && price >= 10000 && price <= 50000) ||
        (priceRange === 'premium' && price > 50000)

      return matchesCategory && matchesStock && matchesPrice
    })

    const matchingProducts = normalizedSearch
      ? categoryProducts.filter((product) =>
          [product.title, product.description, product.category?.title]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()
            .includes(normalizedSearch),
        )
      : categoryProducts
    const unavailable = Boolean(normalizedSearch && matchingProducts.length === 0)
    const visibleProducts = unavailable ? categoryProducts : matchingProducts

    const sortedProducts = [...visibleProducts].sort((a, b) => {
      if (sortBy === 'price-low') return getProductPrice(a) - getProductPrice(b)
      if (sortBy === 'price-high') return getProductPrice(b) - getProductPrice(a)
      if (sortBy === 'stock') return b.quantity - a.quantity
      return new Date(b.addedDate || 0) - new Date(a.addedDate || 0)
    })

    return { filteredProducts: sortedProducts, searchUnavailable: unavailable }
  }, [priceRange, products, search, selectedCategory, selectedCategoryKey, sortBy, stockFilter])

  useEffect(() => {
    setCatalogVisibleCount(catalogPageSize)
  }, [priceRange, search, selectedCategory, sortBy, stockFilter])

  const discountedProducts = useMemo(() => getDiscountedProducts(products, 12), [products])

  const recommendedProducts = useMemo(
    () => getRecommendedProducts({
      products,
      cartItems,
      wishlist,
      recentlyViewed,
      activityProductIds,
      limit: 10,
    }),
    [activityProductIds, cartItems, products, recentlyViewed, wishlist],
  )

  const navigateToCategory = (categoryId) => {
    setSelectedCategory(categoryId)
    setPriceRange('all')
    setStockFilter('available')
    window.requestAnimationFrame(() => productSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
  }

  const runCartAction = async (action) => {
    if (!user?.userId) return
    setActionStatus('')
    try {
      const updatedCart = await action()
      setCart(updatedCart)
    } catch (error) {
      setActionStatus(error.message)
    }
  }

  const recordActivity = useCallback((productId) => {
    setActivityProductIds((current) => [productId, ...current.filter((id) => id !== productId)].slice(0, 4))
  }, [])

  const addToCart = (productId) => {
    recordActivity(productId)
    runCartAction(() => addCartItem(user.userId, productId, 1))
  }

  const decreaseCart = (item) => {
    runCartAction(() => updateCartItemQuantity(user.userId, item.cartItemId, item.quantity - 1))
  }

  const increaseCart = (item) => {
    runCartAction(() => updateCartItemQuantity(user.userId, item.cartItemId, item.quantity + 1))
  }

  const removeFromCart = (itemId) => {
    runCartAction(() => removeCartItem(user.userId, itemId))
  }

  const toggleWishlist = async (productId) => {
    if (!user?.userId) return
    recordActivity(productId)
    setActionStatus('')
    const previousWishlist = wishlist
    const wasLiked = wishlistIds.has(productId)
    const selectedProduct = products.find((product) => product.productId === productId)
    setWishlist((current) =>
      wasLiked
        ? current.filter((product) => product.productId !== productId)
        : selectedProduct
          ? [selectedProduct, ...current]
          : current,
    )
    try {
      const updatedWishlist = wasLiked
        ? await removeWishlistProduct(user.userId, productId)
        : await addWishlistProduct(user.userId, productId)
      setWishlist(updatedWishlist)
    } catch (error) {
      setWishlist(previousWishlist)
      setActionStatus(error.message)
    }
  }

  const trackView = async (productId) => {
    if (!user?.userId) return
    recordActivity(productId)
    if (trackedViews.current.has(productId)) return
    trackedViews.current.add(productId)
    try {
      await trackProductView(user.userId, productId)
      setRecentlyViewed(await getRecentlyViewed(user.userId))
    } catch {
      trackedViews.current.delete(productId)
    }
  }

  const saveProfile = async (event) => {
    event.preventDefault()
    if (!user?.userId) return

    setProfileLoading(true)
    setProfileStatus({ type: '', message: '' })

    try {
      const updatedProfile = await updateUserProfile(user.userId, {
        userId: user.userId,
        name: profileForm.name,
        email: profileForm.email,
        gender: profileForm.gender,
        about: profileForm.about,
        password: profileForm.newPassword || profileForm.currentPassword,
      })

      updateStoredUser({
        userId: updatedProfile.userId,
        name: updatedProfile.name,
        email: updatedProfile.email,
        gender: updatedProfile.gender,
        about: updatedProfile.about,
      })
      setProfileForm((current) => ({ ...current, ...updatedProfile, newPassword: '' }))
      setProfileStatus({ type: 'success', message: 'Profile saved successfully.' })
    } catch (error) {
      setProfileStatus({ type: 'error', message: error.message })
    } finally {
      setProfileLoading(false)
    }
  }

  const placeOrder = async (event) => {
    event.preventDefault()
    if (!user?.userId || !cart.cartId || checkoutProcessing) return

    setActionStatus('')
    setCheckoutProcessing(true)
    try {
      const Razorpay = await loadRazorpayCheckout()

      const payment = await createRazorpayOrder({
        cartId: cart.cartId,
        userId: user.userId,
        orderStatus: 'PENDING',
        paymentStatus: 'PAYMENT_PENDING',
        billingName: checkoutForm.billingName,
        billingPhone: checkoutForm.billingPhone,
        billingAddress: checkoutForm.billingAddress,
      })

      setOrders((current) => [payment.order, ...current.filter((order) => order.orderId !== payment.order.orderId)])

      const checkoutDisplay = getRazorpayDisplayConfig(payment.amount)

      const checkout = new Razorpay({
        key: payment.keyId,
        amount: payment.amount,
        currency: payment.currency,
        name: 'SparkGadget',
        description: `Order ${payment.order.orderId}`,
        order_id: payment.razorpayOrderId,
        prefill: {
          name: checkoutForm.billingName || user.name,
          email: user.email,
          contact: checkoutForm.billingPhone,
        },
        notes: {
          localOrderId: payment.order.orderId,
        },
        theme: {
          color: '#3f3f46',
        },
        ...(checkoutDisplay ? { config: { display: checkoutDisplay } } : {}),
        handler: async (response) => {
          try {
            const verification = await verifyRazorpayPayment({
              orderId: payment.order.orderId,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })

            setOrders((current) =>
              current.map((order) => (order.orderId === verification.order.orderId ? verification.order : order)),
            )
            setCart(await getCart(user.userId))
            setCheckoutOpen(false)
            setActivePanel('orders')
            setActionStatus('Payment verified and order placed successfully.')
          } catch (error) {
            setActionStatus(error.message)
          } finally {
            setCheckoutProcessing(false)
          }
        },
        modal: {
          ondismiss: () => {
            setCheckoutProcessing(false)
            setActionStatus('Payment window closed. Your order is still pending payment.')
          },
        },
      })

      checkout.on('payment.failed', (response) => {
        const paymentError = response.error || {}
        console.error('Razorpay payment failed', paymentError)
        setCheckoutProcessing(false)
        setActionStatus(getRazorpayFailureMessage(paymentError))

        reportRazorpayPaymentFailure({
          orderId: payment.order.orderId,
          razorpayOrderId: paymentError.metadata?.order_id || payment.razorpayOrderId,
          razorpayPaymentId: paymentError.metadata?.payment_id || '',
          code: paymentError.code || '',
          reason: paymentError.reason || '',
          description: paymentError.description || '',
        })
          .then((failedOrder) => {
            setOrders((current) =>
              current.map((order) => (order.orderId === failedOrder.orderId ? failedOrder : order)),
            )
          })
          .catch((error) => console.error('Unable to record Razorpay payment failure', error))
      })

      checkout.open()
    } catch (error) {
      setActionStatus(error.message)
      setCheckoutProcessing(false)
    }
  }

  const closeCheckout = () => {
    setCheckoutOpen(false)
    setCheckoutProcessing(false)
  }

  const confirmOrderDelivery = async (orderId) => {
    setActionStatus('')
    try {
      const updatedOrder = await confirmDelivery(orderId)
      setOrders((current) =>
        current.map((order) => (order.orderId === updatedOrder.orderId ? updatedOrder : order)),
      )
      setActionStatus('Delivery confirmed. Order completed.')
    } catch (error) {
      setActionStatus(error.message)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/auth')
  }

  const submitAssistantQuestion = async (event, quickQuestion = '') => {
    event?.preventDefault()
    const question = (quickQuestion || assistantInput).trim()
    if (!question || assistantLoading) return

    const productContext = buildAssistantProductContext({
      products,
      question,
      recommendedProducts,
      discountedProducts,
      recentlyViewed,
      wishlist,
      cartItems,
    })
    const fallbackAnswer = buildAssistantFallbackAnswer(question, productContext)

    setAssistantInput('')
    setAssistantOpen(true)
    setAssistantMessages((current) => [...current, { role: 'user', text: question }])
    setAssistantLoading(true)

    try {
      const assistantResponse = await askAssistant({ question, products: productContext })
      setAssistantMessages((current) => [
        ...current,
        {
          role: 'assistant',
          text: assistantResponse.answer || fallbackAnswer,
          model: assistantResponse.model,
          provider: assistantResponse.provider,
        },
      ])
    } catch {
      setAssistantMessages((current) => [
        ...current,
        {
          role: 'assistant',
          text: `${fallbackAnswer} AI assistant is offline, so I used the local catalog.`,
          model: 'Local catalog',
          provider: 'SparkGadget',
        },
      ])
    } finally {
      setAssistantLoading(false)
    }
  }

  const selectedCategoryTitle =
    selectedCategory === 'all'
      ? 'All products'
      : uniqueCategories.find((category) => category.categoryId === selectedCategory)?.title ||
        categories.find((category) => normalizeCategoryTitle(category.title) === selectedCategoryKey)?.title ||
        'Selected products'
  const visibleCatalogProducts = filteredProducts.slice(0, catalogVisibleCount)
  const hasMoreCatalogProducts = visibleCatalogProducts.length < filteredProducts.length

  const hero = heroSlides[activeHero]

  return (
    <main id="main-content" className="min-h-[100dvh] bg-[#27272a] text-zinc-100">
      <header className="sticky top-0 z-40 border-b border-zinc-300/16 bg-[#27272a]/92 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
          <button type="button" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })} className="flex items-center gap-3">
            <span className="grid h-11 w-11 place-items-center rounded-lg bg-zinc-200 text-zinc-950 shadow-lg shadow-black/20">
              <FiShoppingCart className="text-xl" />
            </span>
            <span className="text-left">
              <span className="block text-lg font-black text-white">SparkGadget</span>
              <span className="hidden text-xs font-semibold uppercase text-zinc-200 sm:block">Smart tech market</span>
            </span>
          </button>

          <nav className="hidden items-center gap-6 text-sm font-bold text-zinc-300 lg:flex">
            <a href="#recommended" className="transition hover:text-white">Recommended</a>
            <a href="#discounts" className="transition hover:text-white">Deals</a>
            <a href="#catalog" className="transition hover:text-white">Catalog</a>
          </nav>

          <div className="relative flex items-center gap-2">
            <HeaderButton active={activePanel === 'profile'} onClick={() => setActivePanel(activePanel === 'profile' ? null : 'profile')} label={user?.name || 'Profile'} icon={FiUser} avatar={getProfileAvatar(user)} />
            <IconButton active={activePanel === 'wishlist'} onClick={() => setActivePanel(activePanel === 'wishlist' ? null : 'wishlist')} label="Wishlist" icon={FiHeart} count={wishlist.length} />
            <IconButton active={activePanel === 'cart'} onClick={() => setActivePanel(activePanel === 'cart' ? null : 'cart')} label="Cart" icon={FiShoppingCart} count={cartCount} />
            <IconButton active={activePanel === 'orders'} onClick={() => setActivePanel(activePanel === 'orders' ? null : 'orders')} label="Orders" icon={FiPackage} count={currentOrders.length} />
            <IconButton onClick={handleLogout} label="Logout" icon={FiLogOut} />

            {activePanel === 'wishlist' && (
              <HeaderPanel title="Wishlist" onClose={() => setActivePanel(null)}>
                {wishlist.length ? (
                  <div className="grid gap-3">
                    {wishlist.map((product) => (
                      <CompactProductRow
                        key={product.productId}
                        product={product}
                        actionLabel="Add"
                        actionIcon={FiShoppingCart}
                        onAction={() => addToCart(product.productId)}
                        onRemove={() => toggleWishlist(product.productId)}
                      />
                    ))}
                  </div>
                ) : (
                  <EmptyState text="Your saved products will appear here." />
                )}
              </HeaderPanel>
            )}

            {activePanel === 'cart' && (
              <HeaderPanel title="Cart" onClose={() => setActivePanel(null)}>
                {cartItems.length ? (
                  <div className="grid gap-4">
                    {cartItems.map((item) => (
                      <CompactCartRow
                        key={item.cartItemId}
                        item={item}
                        onDecrease={() => decreaseCart(item)}
                        onIncrease={() => increaseCart(item)}
                        onRemove={() => removeFromCart(item.cartItemId)}
                      />
                    ))}
                    <div className="rounded-lg border border-zinc-300/20 bg-zinc-800/70 p-4">
                      <div className="flex items-center justify-between text-sm font-semibold text-zinc-300">
                        <span>Items</span>
                        <span>{cartCount}</span>
                      </div>
                      <div className="mt-2 flex items-center justify-between text-lg font-black text-white">
                        <span>Total</span>
                        <span>{currency.format(cartTotal)}</span>
                      </div>
                      <button type="button" onClick={() => setCheckoutOpen(true)} className="mt-4 flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-zinc-200 text-sm font-black text-zinc-950 transition hover:bg-zinc-300">
                        <FiShoppingCart />
                        Checkout
                      </button>
                    </div>
                  </div>
                ) : (
                  <EmptyState text="Your cart is empty." />
                )}
              </HeaderPanel>
            )}

            {activePanel === 'orders' && (
              <HeaderPanel title="Current Orders" onClose={() => setActivePanel(null)}>
                {currentOrders.length ? (
                  <div className="grid gap-3">
                    {currentOrders.map((order) => (
                      <OrderSummaryCard key={order.orderId} order={order} onConfirmDelivery={confirmOrderDelivery} />
                    ))}
                  </div>
                ) : (
                  <EmptyState text="No current orders. Completed orders are in profile history." />
                )}
              </HeaderPanel>
            )}

            {activePanel === 'profile' && (
              <HeaderPanel title="Profile" onClose={() => setActivePanel(null)}>
                <div className="mb-4 flex items-center gap-3 rounded-lg border border-zinc-300/16 bg-zinc-300/10 p-3">
                  <img className="h-12 w-12 rounded-lg border border-zinc-300/20 bg-zinc-800 object-cover" src={getProfileAvatar(user)} alt="" />
                  <div className="min-w-0">
                    <p className="truncate text-sm font-black text-white">{user?.name || 'SparkGadget User'}</p>
                    <p className="truncate text-xs font-bold text-zinc-200">{user?.email}</p>
                  </div>
                </div>
                <form className="grid gap-3" onSubmit={saveProfile}>
                  {profileStatus.message && <StatusMessage type={profileStatus.type} message={profileStatus.message} />}
                  <PanelInput label="Name" value={profileForm.name} onChange={(value) => setProfileForm({ ...profileForm, name: value })} />
                  <PanelInput label="Email" type="email" value={profileForm.email} onChange={(value) => setProfileForm({ ...profileForm, email: value })} />
                  <label className="grid gap-2 text-sm font-bold text-zinc-300">
                    Gender
                    <select value={profileForm.gender} onChange={(event) => setProfileForm({ ...profileForm, gender: event.target.value })} className="h-11 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 text-sm font-semibold text-white outline-none focus:border-zinc-200/50">
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  </label>
                  <label className="grid gap-2 text-sm font-bold text-zinc-300">
                    About
                    <textarea value={profileForm.about} onChange={(event) => setProfileForm({ ...profileForm, about: event.target.value })} className="min-h-20 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 py-2 text-sm font-semibold text-white outline-none focus:border-zinc-200/50" />
                  </label>
                  <PanelInput label="New Password" type="password" value={profileForm.newPassword} onChange={(value) => setProfileForm({ ...profileForm, newPassword: value })} placeholder="Leave blank to keep current" />
                  <button type="submit" disabled={profileLoading} className="mt-1 flex h-11 items-center justify-center gap-2 rounded-lg bg-zinc-200 text-sm font-black text-zinc-950 transition hover:bg-zinc-300 disabled:opacity-60">
                    <FiSave />
                    {profileLoading ? 'Saving...' : 'Save Profile'}
                  </button>
                </form>
                <div className="mt-5 border-t border-zinc-300/16 pt-4">
                  <div className="mb-3 flex items-center justify-between gap-3">
                    <p className="text-sm font-black uppercase text-zinc-300">Order History</p>
                    <span className="rounded-lg bg-zinc-300/14 px-2 py-1 text-xs font-black text-zinc-200">{completedOrders.length}</span>
                  </div>
                  {completedOrders.length ? (
                    <div className="grid gap-3">
                      {completedOrders.map((order) => (
                        <OrderSummaryCard key={order.orderId} order={order} />
                      ))}
                    </div>
                  ) : (
                    <EmptyState text="Completed orders will appear here." />
                  )}
                </div>
              </HeaderPanel>
            )}
          </div>
        </div>
      </header>

      <section className="border-b border-zinc-300/16 bg-[linear-gradient(180deg,#27272a_0%,#3f3f46_100%)]">
        <div className="mx-auto grid max-w-7xl gap-5 px-4 py-6 sm:px-6 lg:grid-cols-[1.2fr_0.8fr] lg:px-8 lg:py-8">
          <div className="relative flex min-h-[420px] overflow-hidden rounded-lg border border-zinc-300/18 bg-zinc-700 shadow-2xl shadow-black/25">
            {heroSlides.map((slide, index) => (
              <img
                key={slide.id}
                className={`absolute inset-0 h-full w-full object-cover transition-opacity duration-700 ${index === activeHero ? 'opacity-100' : 'opacity-0'}`}
                src={slide.image}
                alt=""
              />
            ))}
            <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(9,9,11,0.9)_0%,rgba(24,24,27,0.68)_52%,rgba(9,9,11,0.16)_100%)]" />
            <div className="relative z-10 flex max-w-3xl flex-col justify-end p-6 sm:p-9">
              <p className="mb-3 inline-flex w-fit rounded-lg border border-zinc-200/30 bg-white/10 px-3 py-2 text-xs font-black uppercase text-white">{hero.eyebrow}</p>
              <h1 className="text-balance text-4xl font-black leading-[1.02] text-white sm:text-6xl">{hero.title}</h1>
              <p className="text-pretty mt-4 max-w-2xl text-base leading-7 text-zinc-100 sm:text-lg">{hero.subtitle}</p>
              <div className="mt-5 flex gap-2">
                {heroSlides.map((slide, index) => (
                  <button
                    key={slide.id}
                    type="button"
                    onClick={() => setActiveHero(index)}
                    className={`h-2.5 rounded-full transition-all ${index === activeHero ? 'w-9 bg-white' : 'w-2.5 bg-white/45 hover:bg-white/70'}`}
                    aria-label={`Show ${slide.title}`}
                  />
                ))}
              </div>
              <div className="mt-7 flex flex-wrap gap-3">
                <button type="button" onClick={() => document.getElementById('catalog')?.scrollIntoView({ behavior: 'smooth' })} className="inline-flex items-center gap-2 rounded-lg bg-zinc-200 px-5 py-3 text-sm font-black text-zinc-950 shadow-lg shadow-black/20 transition hover:bg-zinc-300">
                  <FiGrid />
                  Browse catalog
                </button>
              </div>
            </div>
          </div>
          <aside className="soft-scrollbar flex gap-4 overflow-x-auto pb-2 lg:grid lg:grid-cols-1 lg:overflow-visible lg:pb-0">
            <Metric icon={FiGrid} label="Categories" value={`${uniqueCategories.length} types`} helper="Curated buying lanes" accent="from-zinc-200/22 to-zinc-900/10" image="https://images.unsplash.com/photo-1516321318423-f06f85e504b3" />
            <Metric icon={FiShoppingCart} label="Products" value={`${products.length} live`} helper="Ready for checkout" accent="from-zinc-200/22 to-zinc-500/14" image="https://images.unsplash.com/photo-1498049794561-7780e7231661" />
            <Metric icon={FiPercent} label="Discounts" value={`${discountedProducts.length} active`} helper="Live deal signals" accent="from-zinc-200/22 to-zinc-500/14" image="https://images.unsplash.com/photo-1607082349566-187342175e2f" />
          </aside>
        </div>
      </section>

      {pageStatus.error && <InlineAlert message={pageStatus.error} />}
      {actionStatus && <InlineAlert message={actionStatus} />}

      <StoreSection id="recommended" title="Recommended Products" icon={FiPackage}>
        {pageStatus.loading ? (
          <ProductSkeletonRail />
        ) : (
          <HorizontalProducts products={recommendedProducts} wishlistIds={wishlistIds} onAddToCart={addToCart} onToggleWishlist={toggleWishlist} onView={trackView} />
        )}
      </StoreSection>

      <StoreSection id="discounts" title="Discounted Items" icon={FiPercent}>
        {pageStatus.loading ? (
          <ProductSkeletonRail />
        ) : (
          <HorizontalProducts products={discountedProducts} wishlistIds={wishlistIds} onAddToCart={addToCart} onToggleWishlist={toggleWishlist} onView={trackView} />
        )}
      </StoreSection>

      <StoreSection id="catalog" title="Product Categories & Catalog" icon={FiGrid} sectionRef={productSectionRef}>
        {pageStatus.loading ? (
          <CategorySkeletonRail />
        ) : uniqueCategories.length ? (
          <div className="soft-scrollbar mb-5 flex gap-4 overflow-x-auto pb-3">
            <CategoryCard
              title="All products"
              description="Browse the full SparkGadget catalog."
              image="https://images.unsplash.com/photo-1516321318423-f06f85e504b3"
              count={products.length}
              active={selectedCategory === 'all'}
              icon={FiGrid}
              onClick={() => navigateToCategory('all')}
            />
            {uniqueCategories.map((category) => (
              <CategoryCard
                key={category.categoryId}
                title={category.title}
                description={category.description}
                image={getCategoryImage(category)}
                count={productCountsByCategory.get(normalizeCategoryTitle(category.title)) || 0}
                active={selectedCategoryKey === normalizeCategoryTitle(category.title)}
                icon={FiPackage}
                onClick={() => navigateToCategory(category.categoryId)}
              />
            ))}
          </div>
        ) : (
          <EmptyState text="No categories found yet." />
        )}

        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <p className="text-xl font-black text-white">{selectedCategoryTitle}</p>
            <p className="mt-1 text-sm font-semibold text-zinc-400">
              {filteredProducts.length
                ? `Showing ${visibleCatalogProducts.length} of ${filteredProducts.length} ${searchUnavailable ? 'products available to browse' : 'matching products'}`
                : `0 ${searchUnavailable ? 'products available to browse' : 'matching products'}`}
            </p>
          </div>
        </div>

        <div className="mb-5 grid gap-3 rounded-lg border border-zinc-300/16 bg-zinc-700/76 p-4 shadow-xl shadow-black/10 md:grid-cols-[1.3fr_repeat(4,1fr)]">
          <label className="relative block">
            <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
            <input value={search} onChange={(event) => setSearch(event.target.value)} className="h-11 w-full rounded-lg border border-zinc-300/16 bg-zinc-800/80 pl-10 pr-3 text-sm font-semibold text-white outline-none placeholder:text-zinc-500 focus:border-zinc-200/50" placeholder="Search products by title" />
          </label>
          <Select value={selectedCategory} onChange={setSelectedCategory}>
            <option value="all">All categories</option>
            {uniqueCategories.map((category) => <option key={category.categoryId} value={category.categoryId}>{category.title}</option>)}
          </Select>
          <Select value={priceRange} onChange={setPriceRange}>
            <option value="all">All prices</option>
            <option value="budget">Under 10k</option>
            <option value="mid">10k - 50k</option>
            <option value="premium">Above 50k</option>
          </Select>
          <Select value={stockFilter} onChange={setStockFilter}>
            <option value="available">In stock</option>
            <option value="out">Out of stock</option>
            <option value="all">All stock</option>
          </Select>
          <Select value={sortBy} onChange={setSortBy}>
            <option value="newest">Newest</option>
            <option value="stock">Most stock</option>
            <option value="price-low">Price low</option>
            <option value="price-high">Price high</option>
          </Select>
        </div>

        {searchUnavailable && (
          <div className="mb-5 rounded-lg border border-zinc-200/30 bg-zinc-300/18 px-4 py-3 text-sm font-bold text-white">
            This product is not available right now. Showing other available products instead.
          </div>
        )}

        {pageStatus.loading ? (
          <ProductSkeletonGrid />
        ) : filteredProducts.length ? (
          <div className="grid gap-6">
            <HorizontalProducts layout="grid" products={visibleCatalogProducts} wishlistIds={wishlistIds} onAddToCart={addToCart} onToggleWishlist={toggleWishlist} onView={trackView} />
            {hasMoreCatalogProducts && (
              <div className="flex justify-center">
                <button
                  type="button"
                  onClick={() =>
                    setCatalogVisibleCount((current) => Math.min(current + catalogPageSize, filteredProducts.length))
                  }
                  className="inline-flex h-11 items-center justify-center rounded-lg border border-zinc-300/20 bg-zinc-300/14 px-5 text-sm font-black text-zinc-100 transition hover:border-zinc-200/35 hover:bg-zinc-300/20"
                >
                  Show more
                </button>
              </div>
            )}
          </div>
        ) : (
          <EmptyState text="No products matched these filters." />
        )}
      </StoreSection>

      <footer className="border-t border-zinc-300/16 bg-[#3f3f46] px-4 pt-12 text-zinc-200 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <div className="grid gap-10 lg:grid-cols-[1.1fr_0.9fr]">
            <div>
              <div className="flex items-center gap-3">
                <span className="grid h-12 w-12 place-items-center rounded-lg bg-zinc-200 text-zinc-950">
                  <FiShoppingCart className="text-xl" />
                </span>
                <div>
                  <p className="text-2xl font-black text-white">SparkGadget Enterprise</p>
                  <p className="mt-1 text-sm font-semibold text-zinc-200">Premium electronics marketplace suite</p>
                </div>
              </div>
              <p className="mt-5 max-w-2xl text-sm leading-6 text-zinc-400">
                Built for fast customer shopping, managed wishlists, live product discovery, secure checkout, and scalable retail workflows across teams and buyers.
              </p>
              <div className="mt-6 grid gap-3 sm:grid-cols-2">
                <FooterContact icon={FiMail} label="Mail us" value="support@sparkgadget.com" />
                <FooterContact icon={FiPhone} label="Enterprise desk" value="+91 98765 43210" />
                <FooterContact icon={FiMapPin} label="Registered office" value="Sector 62, Noida, India" />
                <FooterContact icon={FiBriefcase} label="Business hours" value="Mon-Sat, 9:00-20:00 IST" />
              </div>
            </div>

            <div className="grid gap-6 sm:grid-cols-2">
              {footerColumns.map((column) => (
                <div key={column.title}>
                  <p className="text-xs font-black uppercase text-zinc-500">{column.title}</p>
                  <div className="mt-4 grid gap-2">
                    {column.links.map((link) => (
                      <span key={link} className="text-sm font-bold text-zinc-200 transition hover:text-white">{link}</span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-10 border-t border-zinc-300/16 py-5">
            <div className="grid gap-5 lg:grid-cols-[1fr_auto] lg:items-center">
              <div className="flex flex-wrap gap-3">
                {enterpriseHighlights.map(({ icon: Icon, label }) => (
                  <span key={label} className="inline-flex items-center gap-2 rounded-lg border border-zinc-300/16 bg-zinc-300/10 px-3 py-2 text-xs font-black uppercase text-zinc-200">
                    <Icon className="text-white" />
                    {label}
                  </span>
                ))}
              </div>
              <div className="flex flex-wrap items-center gap-3 text-xs font-bold text-zinc-400">
                <span>(c) 2026 SparkGadget.com</span>
                <span className="hidden h-4 w-px bg-white/15 sm:block" />
                <span>CIN: SG-2026-ENT-IND</span>
                <span className="hidden h-4 w-px bg-white/15 sm:block" />
                <span>GST ready invoices</span>
              </div>
            </div>
          </div>
        </div>
      </footer>

      <div className="fixed bottom-4 right-4 z-50 flex flex-col items-end gap-3 sm:bottom-6 sm:right-6">
        <AnimatePresence>
          {assistantOpen && (
            <motion.section
              initial={{ opacity: 0, y: 18, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 12, scale: 0.96 }}
              transition={{ duration: 0.18 }}
              className="w-[min(calc(100vw-2rem),390px)] overflow-hidden rounded-lg border border-zinc-300/16 bg-zinc-700 shadow-2xl shadow-zinc-900/60"
            >
              <div className="flex items-center justify-between gap-3 border-b border-zinc-300/16 px-4 py-3">
                <div className="flex items-center gap-3">
                  <span className="grid h-9 w-9 place-items-center rounded-lg bg-zinc-300/14 text-zinc-200">
                    <img className="h-8 w-8 rounded-lg object-cover" src={assistantAvatarUrl} alt="" />
                  </span>
                  <div>
                    <p className="text-sm font-black text-white">Spark Assistant</p>
                    <p className="text-xs font-semibold text-zinc-400">Product picker</p>
                  </div>
                </div>
                <button type="button" onClick={() => setAssistantOpen(false)} className="grid h-8 w-8 place-items-center rounded-lg border border-zinc-300/16 text-zinc-300" aria-label="Close assistant">
                  <FiX />
                </button>
              </div>

              <div ref={assistantScrollRef} className="soft-scrollbar flex max-h-[360px] flex-col gap-3 overflow-y-auto px-4 py-4">
                {assistantMessages.map((message, index) => (
                  <div
                    key={`${message.role}-${index}-${message.text}`}
                    className={`max-w-[86%] rounded-lg px-3 py-2 text-sm leading-5 ${
                      message.role === 'user'
                        ? 'ml-auto bg-zinc-200 text-zinc-950 font-bold'
                        : 'mr-auto border border-zinc-300/16 bg-zinc-300/12 text-zinc-100'
                    }`}
                  >
                    {message.text}
                    {message.role === 'assistant' && message.model && (
                      <span className="mt-1 block text-[11px] font-black uppercase text-zinc-500">
                        {message.provider ? `${message.provider} - ` : ''}{message.model}
                      </span>
                    )}
                  </div>
                ))}
                {assistantLoading && (
                  <div className="mr-auto rounded-lg border border-zinc-300/16 bg-zinc-300/12 px-3 py-2 text-sm font-semibold text-zinc-300">
                    Thinking through the catalog...
                  </div>
                )}
              </div>

              <div className="border-t border-zinc-300/16 p-3">
                <div className="mb-3 flex gap-2 overflow-x-auto pb-1">
                  {assistantQuickPrompts.map((prompt) => (
                    <button
                      key={prompt}
                      type="button"
                      onClick={(event) => submitAssistantQuestion(event, prompt)}
                      disabled={assistantLoading}
                      className="shrink-0 rounded-lg border border-zinc-300/20 bg-zinc-300/14 px-3 py-2 text-xs font-black text-zinc-100 disabled:opacity-50"
                    >
                      {prompt}
                    </button>
                  ))}
                </div>
                <form onSubmit={submitAssistantQuestion} className="grid grid-cols-[1fr_auto] gap-2">
                  <input
                    value={assistantInput}
                    onChange={(event) => setAssistantInput(event.target.value)}
                    maxLength={500}
                    placeholder="Ask what to buy..."
                    className="h-11 min-w-0 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 text-sm font-semibold text-white outline-none placeholder:text-zinc-500 focus:border-zinc-200/50"
                  />
                  <button
                    type="submit"
                    disabled={assistantLoading || !assistantInput.trim()}
                    className="grid h-11 w-11 place-items-center rounded-lg bg-zinc-200 text-zinc-950 transition hover:bg-zinc-300 disabled:opacity-50"
                    aria-label="Send question"
                  >
                    <FiSend />
                  </button>
                </form>
              </div>
            </motion.section>
          )}
        </AnimatePresence>

        <button
          type="button"
          onClick={() => setAssistantOpen((current) => !current)}
          className="grid h-14 w-14 place-items-center rounded-lg border border-zinc-200/30 bg-zinc-200 text-xl text-zinc-950 shadow-2xl shadow-black/20 transition hover:-translate-y-0.5 hover:bg-zinc-300"
          aria-label="Open shopping assistant"
        >
          {assistantOpen ? <FiX /> : <img className="h-12 w-12 rounded-lg object-cover" src={assistantAvatarUrl} alt="" />}
        </button>
      </div>

      {checkoutOpen && (
        <div className="fixed inset-0 z-50 grid place-items-center bg-zinc-800/75 px-4 backdrop-blur">
          <form onSubmit={placeOrder} className="w-full max-w-lg rounded-lg border border-zinc-300/16 bg-zinc-700 p-5 shadow-2xl">
            <div className="mb-4 flex items-center justify-between gap-3">
              <h2 className="text-xl font-black text-white">Checkout</h2>
              <button type="button" onClick={closeCheckout} className="grid h-9 w-9 place-items-center rounded-lg border border-zinc-300/16 text-zinc-200"><FiX /></button>
            </div>
            <div className="grid gap-3">
              <PanelInput label="Billing Name" value={checkoutForm.billingName} onChange={(value) => setCheckoutForm({ ...checkoutForm, billingName: value })} required />
              <PanelInput label="Billing Phone" value={checkoutForm.billingPhone} onChange={(value) => setCheckoutForm({ ...checkoutForm, billingPhone: value })} required />
              <label className="grid gap-2 text-sm font-bold text-zinc-300">
                Billing Address
                <textarea required value={checkoutForm.billingAddress} onChange={(event) => setCheckoutForm({ ...checkoutForm, billingAddress: event.target.value })} className="min-h-24 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 py-2 text-sm font-semibold text-white outline-none focus:border-zinc-200/50" />
              </label>
              <div className="rounded-lg border border-zinc-300/20 bg-zinc-300/14 p-3 text-sm font-bold text-zinc-100">
                Total: {currency.format(cartTotal)}
              </div>
              {cartTotal > 50_000 && (
                <div className="rounded-lg border border-zinc-200/30 bg-zinc-300/18 px-3 py-2 text-sm font-bold text-white">
                  {cartTotal > 100_000
                    ? 'Use Card or Netbanking for this amount. Standard UPI supports up to INR 1,00,000.'
                    : 'Card and Netbanking are prioritized for this amount. Razorpay account and bank limits still apply.'}
                </div>
              )}
              {actionStatus && (
                <div role="alert" className="rounded-lg border border-zinc-300/20 bg-zinc-300/14 px-3 py-2 text-sm font-bold text-zinc-100">
                  {actionStatus}
                </div>
              )}
              <button type="submit" disabled={checkoutProcessing} className="flex h-11 items-center justify-center gap-2 rounded-lg bg-zinc-200 text-sm font-black text-zinc-950 transition hover:bg-zinc-300 disabled:cursor-not-allowed disabled:opacity-60">
                <FiCreditCard />
                {checkoutProcessing ? 'Opening Payment...' : 'Pay with Razorpay'}
              </button>
            </div>
          </form>
        </div>
      )}
    </main>
  )
}

function HeaderButton({ active, onClick, label, icon: Icon, avatar }) {
  return (
    <button type="button" onClick={onClick} className={`hidden items-center gap-2 rounded-lg border px-3 py-2 text-sm font-bold transition md:flex ${active ? 'border-zinc-200/50 bg-zinc-300/14 text-zinc-100' : 'border-zinc-300/16 bg-zinc-300/10 text-zinc-200 hover:border-zinc-200/35 hover:bg-zinc-300/16'}`}>
      {avatar ? (
        <img className="h-6 w-6 rounded-md border border-zinc-300/20 bg-zinc-800 object-cover" src={avatar} alt="" />
      ) : (
        <Icon className="text-zinc-300" />
      )}
      {label}
    </button>
  )
}

function IconButton({ active, onClick, label, icon: Icon, count }) {
  return (
    <button type="button" onClick={onClick} className={`relative grid h-10 w-10 place-items-center rounded-lg border transition ${active ? 'border-zinc-200/50 bg-zinc-300/14 text-zinc-100' : 'border-zinc-300/16 bg-zinc-300/10 text-zinc-200 hover:border-zinc-200/35 hover:bg-zinc-300/16'}`} aria-label={label}>
      <Icon />
      {count > 0 && <CountBadge count={count} />}
    </button>
  )
}

function CountBadge({ count }) {
  return <span className="absolute -right-2 -top-2 grid min-h-5 min-w-5 place-items-center rounded-full bg-zinc-200 px-1 text-[11px] font-black text-zinc-950">{count}</span>
}

function HeaderPanel({ title, children, onClose }) {
  return (
    <div className="absolute right-0 top-12 z-50 flex max-h-[calc(100vh-6.5rem)] w-[min(92vw,520px)] flex-col rounded-lg border border-zinc-300/16 bg-zinc-700/98 p-4 shadow-2xl shadow-zinc-900/50">
      <div className="mb-4 flex shrink-0 items-center justify-between">
        <h3 className="text-lg font-black text-white">{title}</h3>
        <button type="button" onClick={onClose} className="grid h-8 w-8 place-items-center rounded-lg border border-zinc-300/16 text-zinc-300"><FiX /></button>
      </div>
      <div className="soft-scrollbar min-h-0 overflow-y-auto pr-1">
        {children}
      </div>
    </div>
  )
}

function OrderSummaryCard({ order, onConfirmDelivery }) {
  return (
    <div className="rounded-lg border border-zinc-300/16 bg-zinc-800/70 p-3">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm font-black text-white">{currency.format(order.orderAmount)}</p>
        <span className="rounded-lg bg-zinc-300/14 px-2 py-1 text-xs font-black text-zinc-200">{formatOrderStatus(order.orderStatus)}</span>
      </div>
      <p className="mt-2 text-xs font-semibold text-zinc-400">{order.billingName} - {order.billingPhone}</p>
      <p className="mt-1 text-xs font-bold text-zinc-200">Payment: {order.paymentStatus || 'PENDING'}</p>
      <p className="mt-1 text-xs font-semibold text-zinc-500">{order.orderItems?.length || 0} products</p>
      {order.orderStatus === 'DELIVERED' && onConfirmDelivery && (
        <button type="button" onClick={() => onConfirmDelivery(order.orderId)} className="mt-3 flex h-9 w-full items-center justify-center gap-2 rounded-lg bg-zinc-500 text-xs font-black text-white">
          <FiCheckCircle />
          Confirm Delivery
        </button>
      )}
    </div>
  )
}

function StoreSection({ id, title, icon: Icon, children, sectionRef }) {
  return (
    <section ref={sectionRef} id={id} className="scroll-mt-24 border-t border-zinc-300/16 px-4 py-11 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="mb-6 flex items-center gap-3">
          <span className="grid h-11 w-11 place-items-center rounded-lg border border-zinc-300/16 bg-zinc-300/14 text-white"><Icon className="text-xl" /></span>
          <h2 className="text-2xl font-black text-white sm:text-3xl">{title}</h2>
        </div>
        {children}
      </div>
    </section>
  )
}

function CategoryCard({ title, description, image, count, active, icon: Icon, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`group relative min-h-[260px] w-[285px] shrink-0 overflow-hidden rounded-lg border text-left shadow-xl shadow-black/15 transition hover:-translate-y-1 ${active ? 'border-zinc-200/70 bg-white/10' : 'border-zinc-300/16 bg-zinc-300/10 hover:border-zinc-200/35'}`}
    >
      <img
        className="absolute inset-0 h-full w-full object-cover opacity-75 transition duration-500 group-hover:scale-105"
        src={withImageParams(image)}
        alt=""
        onError={(event) => {
          if (!event.currentTarget.dataset.fallbackApplied) {
            event.currentTarget.dataset.fallbackApplied = 'true'
            event.currentTarget.src = withImageParams(categoryImages.accessories)
          }
        }}
      />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(9,9,11,0.12)_0%,rgba(9,9,11,0.86)_62%,rgba(9,9,11,0.98)_100%)]" />
      <div className="relative z-10 flex min-h-[260px] flex-col justify-end p-5">
        <span className="mb-auto grid h-12 w-12 place-items-center rounded-lg border border-zinc-300/20 bg-zinc-800/70 text-white backdrop-blur">
          <Icon />
        </span>
        <span className="block text-xl font-black text-white">{title}</span>
        <span className="mt-2 line-clamp-2 min-h-10 text-sm font-semibold leading-5 text-zinc-200">{description}</span>
        <span className="mt-4 inline-flex w-fit rounded-lg bg-zinc-200 px-3 py-1 text-xs font-black uppercase text-zinc-950">
          {count} {count === 1 ? 'item' : 'items'}
        </span>
      </div>
    </button>
  )
}

function FooterContact({ icon: Icon, label, value }) {
  return (
    <div className="rounded-lg border border-zinc-300/16 bg-zinc-300/10 p-4">
      <div className="flex items-start gap-3">
        <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-zinc-300/14 text-zinc-200">
          <Icon />
        </span>
        <span>
          <span className="block text-xs font-black uppercase text-zinc-500">{label}</span>
          <span className="mt-1 block text-sm font-bold leading-5 text-zinc-200">{value}</span>
        </span>
      </div>
    </div>
  )
}

function Metric({ icon: Icon, label, value, helper, accent, image }) {
  return (
    <div className="relative min-w-[240px] overflow-hidden rounded-lg border border-zinc-300/16 bg-zinc-700 p-5 shadow-xl shadow-black/15">
      <img
        src={withImageParams(image, 700)}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 h-full w-full object-cover opacity-[0.48] saturate-[0.7] contrast-125"
      />
      <div className={`absolute inset-0 bg-gradient-to-br ${accent || 'from-zinc-200/18 to-zinc-900/8'}`} />
      <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(9,9,11,0.9)_0%,rgba(9,9,11,0.74)_58%,rgba(9,9,11,0.62)_100%)]" />
      <div className="relative">
        <span className="grid h-14 w-14 place-items-center rounded-lg border border-zinc-300/16 bg-zinc-800/70 text-white shadow-lg shadow-zinc-900/20"><Icon className="text-2xl" /></span>
        <p className="mt-7 text-sm font-bold uppercase text-zinc-400">{label}</p>
        <p className="mt-2 text-3xl font-black text-white">{value}</p>
        <p className="mt-3 flex items-center gap-2 text-xs font-bold uppercase text-zinc-100">
          <FiCheckCircle />
          {helper}
        </p>
      </div>
    </div>
  )
}

function Select({ value, onChange, children }) {
  return (
    <select value={value} onChange={(event) => onChange(event.target.value)} className="h-11 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 text-sm font-semibold text-white outline-none focus:border-zinc-200/50">
      {children}
    </select>
  )
}

function HorizontalProducts({ products, wishlistIds, onAddToCart, onToggleWishlist, onView, layout = 'scroll' }) {
  if (!products.length) return <EmptyState text="No products to show yet." />

  const isGrid = layout === 'grid'

  return (
    <div className={isGrid ? 'grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4' : 'soft-scrollbar flex gap-4 overflow-x-auto pb-3'}>
      {products.map((product) => (
        <div key={product.productId} className={isGrid ? 'min-w-0' : 'w-[280px] shrink-0 sm:w-[300px]'}>
          <ProductCard product={product} isLiked={wishlistIds.has(product.productId)} onAddToCart={() => onAddToCart(product.productId)} onToggleWishlist={() => onToggleWishlist(product.productId)} onView={() => onView(product.productId)} />
        </div>
      ))}
    </div>
  )
}

function ProductSkeletonRail() {
  return (
    <div className="soft-scrollbar flex gap-4 overflow-x-auto pb-3" aria-label="Loading products">
      {Array.from({ length: 4 }).map((_, index) => (
        <ProductSkeletonCard key={index} className="w-[280px] shrink-0 sm:w-[300px]" />
      ))}
    </div>
  )
}

function ProductSkeletonGrid() {
  return (
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4" aria-label="Loading catalog">
      {Array.from({ length: 8 }).map((_, index) => (
        <ProductSkeletonCard key={index} />
      ))}
    </div>
  )
}

function ProductSkeletonCard({ className = '' }) {
  return (
    <div className={`animate-pulse overflow-hidden rounded-lg border border-zinc-300/16 bg-zinc-700/76 ${className}`}>
      <div className="h-48 bg-zinc-800/80" />
      <div className="space-y-3 p-4">
        <div className="flex justify-between gap-3">
          <div className="h-6 w-24 rounded-lg bg-zinc-800" />
          <div className="h-6 w-16 rounded-lg bg-zinc-800" />
        </div>
        <div className="h-5 w-4/5 rounded-lg bg-zinc-800" />
        <div className="h-4 w-full rounded-lg bg-zinc-800" />
        <div className="h-4 w-2/3 rounded-lg bg-zinc-800" />
        <div className="flex items-end justify-between pt-2">
          <div className="h-7 w-28 rounded-lg bg-zinc-800" />
          <div className="h-10 w-20 rounded-lg bg-zinc-700" />
        </div>
      </div>
    </div>
  )
}

function CategorySkeletonRail() {
  return (
    <div className="soft-scrollbar mb-5 flex gap-4 overflow-x-auto pb-3" aria-label="Loading categories">
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index} className="h-[260px] w-[285px] shrink-0 animate-pulse rounded-lg border border-zinc-300/16 bg-zinc-700/76">
          <div className="h-full rounded-lg bg-zinc-800/70" />
        </div>
      ))}
    </div>
  )
}

function ProductCard({ product, isLiked, onAddToCart, onToggleWishlist, onView }) {
  const discount = getDiscountPercent(product)
  const imageUrl = getProductImage(product)
  const fallbackImage = getProductFallbackImage(product)
  const [cartPulse, setCartPulse] = useState(false)
  const [wishPulse, setWishPulse] = useState(false)

  const triggerPulse = (setter) => {
    setter(true)
    window.setTimeout(() => setter(false), 850)
  }

  const handleAddToCart = () => {
    triggerPulse(setCartPulse)
    onAddToCart()
  }

  const handleWishlist = () => {
    triggerPulse(setWishPulse)
    onToggleWishlist()
  }

  return (
    <article onClickCapture={onView} className={`group relative overflow-hidden rounded-lg border bg-zinc-700/88 text-left shadow-xl shadow-black/12 transition hover:-translate-y-1 hover:border-zinc-200/30 ${cartPulse || wishPulse ? 'border-white shadow-white/10' : 'border-zinc-300/16'}`}>
      <AnimatePresence>
        {(cartPulse || wishPulse) && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.92 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.96 }}
            transition={{ duration: 0.2 }}
            className="absolute left-3 top-3 z-20 inline-flex items-center gap-2 rounded-lg border border-zinc-200/30 bg-zinc-800/90 px-3 py-2 text-xs font-black uppercase text-white shadow-xl backdrop-blur"
          >
            {cartPulse ? <FiShoppingCart /> : <FiHeart className="fill-current" />}
            {cartPulse ? 'Added to cart' : isLiked ? 'Wishlist updated' : 'Saved'}
          </motion.div>
        )}
      </AnimatePresence>
      <div className="relative grid h-48 place-items-center overflow-hidden bg-zinc-800">
        {imageUrl ? (
          <img
            className={`h-full w-full opacity-90 transition duration-500 group-hover:scale-105 ${
              product.productId === 'seed-oneplus-y1s-pro-43' ? 'bg-white object-contain' : 'object-cover'
            }`}
            src={imageUrl}
            alt={product.title}
            onError={(event) => {
              if (!event.currentTarget.dataset.fallbackApplied) {
                event.currentTarget.dataset.fallbackApplied = 'true'
                event.currentTarget.src = fallbackImage
              }
            }}
          />
        ) : <FiPackage className="text-6xl text-zinc-200/55" />}
        <div className="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-zinc-950/90 to-transparent" />
        {discount > 0 && <span className="absolute left-3 top-3 rounded-lg bg-zinc-200 px-3 py-1 text-xs font-black text-zinc-950">{discount}% OFF</span>}
        <motion.button
          type="button"
          whileTap={{ scale: 0.82, rotate: -8 }}
          animate={wishPulse ? { scale: [1, 1.18, 1] } : { scale: 1 }}
          onClick={handleWishlist}
          className={`absolute right-3 top-3 grid h-9 w-9 place-items-center rounded-lg border backdrop-blur ${isLiked || wishPulse ? 'border-zinc-200/45 bg-zinc-300/18 text-zinc-100' : 'border-zinc-200/30 bg-zinc-800/70 text-white'}`}
          aria-label="Toggle wishlist"
        >
          <FiHeart className={isLiked ? 'fill-current' : ''} />
        </motion.button>
      </div>
      <div className="p-4">
        <div className="flex items-center justify-between gap-3">
          <span className="rounded-lg bg-zinc-800/80 px-2.5 py-1 text-xs font-black text-zinc-200">{product.category?.title || 'Uncategorized'}</span>
          <span className={`rounded-lg px-2.5 py-1 text-xs font-black ${product.stock ? 'bg-zinc-300/14 text-zinc-100' : 'bg-zinc-300/14 text-zinc-200'}`}>{product.stock ? 'In stock' : 'Out'}</span>
        </div>
        <h3 className="mt-3 line-clamp-2 min-h-12 text-lg font-black leading-6 text-white">{product.title}</h3>
        <p className="mt-2 line-clamp-2 min-h-10 text-sm leading-5 text-zinc-400">{product.description}</p>
        <p className="mt-2 text-xs font-semibold text-zinc-500">{product.quantity} units ready</p>
        <div className="mt-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-xl font-black text-white">{currency.format(getProductPrice(product))}</p>
            {discount > 0 && <p className="text-sm font-semibold text-zinc-500 line-through">{currency.format(product.price)}</p>}
          </div>
          <motion.button
            type="button"
            whileTap={{ scale: 0.9 }}
            animate={cartPulse ? { y: [0, -4, 0], scale: [1, 1.05, 1] } : { y: 0, scale: 1 }}
            onClick={handleAddToCart}
            disabled={!product.stock || product.quantity <= 0}
            className="inline-flex h-10 items-center gap-2 rounded-lg bg-zinc-200 px-3 text-sm font-black text-zinc-950 shadow-lg shadow-black/15 transition hover:bg-zinc-300 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <FiShoppingCart />
            {cartPulse ? 'Added' : 'Add'}
          </motion.button>
        </div>
      </div>
    </article>
  )
}

function CompactProductRow({ product, actionLabel, actionIcon: ActionIcon, onAction, onRemove }) {
  return (
    <div className="grid grid-cols-[48px_1fr_auto] items-center gap-3 rounded-lg border border-zinc-300/16 bg-zinc-300/10 p-2">
      <div className="grid h-12 w-12 place-items-center overflow-hidden rounded-lg bg-zinc-800">
        {getProductImage(product) ? <img className="h-full w-full object-cover" src={getProductImage(product)} alt={product.title} /> : <FiPackage className="text-zinc-200" />}
      </div>
      <div className="min-w-0">
        <p className="truncate text-sm font-black text-white">{product.title}</p>
        <p className="text-xs font-semibold text-zinc-400">{currency.format(getProductPrice(product))}</p>
      </div>
      <div className="flex items-center gap-2">
        <button type="button" onClick={onAction} className="grid h-9 w-9 place-items-center rounded-lg bg-zinc-200 text-zinc-950 transition hover:bg-zinc-300" aria-label={actionLabel}><ActionIcon /></button>
        <button type="button" onClick={onRemove} className="grid h-9 w-9 place-items-center rounded-lg border border-zinc-300/16 text-zinc-300" aria-label="Remove"><FiTrash2 /></button>
      </div>
    </div>
  )
}

function CompactCartRow({ item, onDecrease, onIncrease, onRemove }) {
  return (
    <div className="grid grid-cols-[1fr_auto] gap-3 rounded-lg border border-zinc-300/16 bg-zinc-300/10 p-3">
      <div className="min-w-0">
        <p className="truncate text-sm font-black text-white">{item.product.title}</p>
        <p className="mt-1 text-xs font-semibold text-zinc-400">{currency.format(getProductPrice(item.product))} each</p>
        <p className="mt-1 text-xs font-bold text-zinc-200">Total {currency.format(item.totalPrice)}</p>
      </div>
      <div className="flex items-center gap-2">
        <button type="button" onClick={onDecrease} className="grid h-8 w-8 place-items-center rounded-lg border border-zinc-300/16 text-zinc-200"><FiMinus /></button>
        <span className="w-7 text-center text-sm font-black text-white">{item.quantity}</span>
        <button type="button" onClick={onIncrease} className="grid h-8 w-8 place-items-center rounded-lg border border-zinc-300/16 text-zinc-200"><FiPlus /></button>
        <button type="button" onClick={onRemove} className="grid h-8 w-8 place-items-center rounded-lg border border-zinc-300/20 text-zinc-200"><FiTrash2 /></button>
      </div>
    </div>
  )
}

function PanelInput({ label, value, onChange, type = 'text', placeholder = '', required = false }) {
  return (
    <label className="grid gap-2 text-sm font-bold text-zinc-300">
      {label}
      <input required={required} type={type} value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} className="h-11 rounded-lg border border-zinc-300/16 bg-zinc-800/80 px-3 text-sm font-semibold text-white outline-none placeholder:text-zinc-500 focus:border-zinc-200/50" />
    </label>
  )
}

function StatusMessage({ type, message }) {
  return <div className={`rounded-lg border px-3 py-2 text-sm font-semibold ${type === 'success' ? 'border-zinc-300/20 bg-zinc-300/14 text-zinc-100' : 'border-zinc-300/20 bg-zinc-300/14 text-zinc-200'}`}>{message}</div>
}

function InlineAlert({ message }) {
  return (
    <div className="px-4 pt-4 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl rounded-lg border border-zinc-300/20 bg-zinc-300/14 px-4 py-3 text-sm font-bold text-zinc-100">{message}</div>
    </div>
  )
}

function EmptyState({ text }) {
  return (
    <div className="grid place-items-center rounded-lg border border-dashed border-zinc-300/20 bg-zinc-300/8 p-8 text-center">
      <span className="mb-3 grid h-11 w-11 place-items-center rounded-lg border border-zinc-300/16 bg-zinc-800/70 text-white">
        <FiPackage />
      </span>
      <p className="max-w-sm text-sm font-semibold leading-6 text-zinc-400">{text}</p>
    </div>
  )
}

export default MainStorePage
