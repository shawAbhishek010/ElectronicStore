/*
Purpose:
Renders the admin portal for analytics, product/category management, and paid order fulfillment.
*/
import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  FiBarChart2,
  FiBox,
  FiCheckCircle,
  FiEdit3,
  FiGrid,
  FiLogOut,
  FiPackage,
  FiPlus,
  FiRefreshCw,
  FiSave,
  FiTrash2,
} from 'react-icons/fi'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'
import {
  createCategory,
  createProduct,
  deleteCategory,
  deleteProduct,
  getAdminAnalytics,
  getAllOrdersForAdmin,
  getAllProductsForAdmin,
  updateCategory,
  updateOrderStatus,
  updateProduct,
} from '../services/adminService.js'
import { getCategories } from '../services/catalogService.js'

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

const emptyCategory = {
  categoryId: '',
  title: '',
  description: '',
  coverImage: '',
}

const emptyProduct = {
  productId: '',
  title: '',
  description: '',
  price: '',
  discountedPrice: '',
  quantity: '',
  productImageName: '',
  categoryId: '',
  live: true,
  stock: true,
}

function AdminDashboardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [analytics, setAnalytics] = useState(null)
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [orders, setOrders] = useState([])
  const [categoryForm, setCategoryForm] = useState(emptyCategory)
  const [productForm, setProductForm] = useState(emptyProduct)
  const [status, setStatus] = useState({ type: '', message: '' })
  const [loading, setLoading] = useState(true)
  const [showAllCategories, setShowAllCategories] = useState(false)
  const [showAllProducts, setShowAllProducts] = useState(false)
  const [showAllOrders, setShowAllOrders] = useState(false)
  const [showAllBestSellers, setShowAllBestSellers] = useState(false)

  const loadDashboard = useCallback(async () => {
    setLoading(true)
    setStatus({ type: '', message: '' })
    try {
      const [analyticsData, categoryData, productData, orderData] = await Promise.all([
        getAdminAnalytics(),
        getCategories(),
        getAllProductsForAdmin(),
        getAllOrdersForAdmin(),
      ])
      setAnalytics(analyticsData)
      setCategories(categoryData)
      setProducts(productData)
      setOrders(orderData)
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDashboard()
  }, [loadDashboard])

  const paidOrders = useMemo(() => orders.filter((order) => order.paymentStatus === 'PAID'), [orders])
  const visibleCategories = showAllCategories ? categories : categories.slice(0, 5)
  const visibleProducts = showAllProducts ? products : products.slice(0, 5)
  const visibleOrders = showAllOrders ? paidOrders : paidOrders.slice(0, 5)
  const bestSellingProducts = analytics?.bestSellingProducts || []
  const visibleBestSellers = showAllBestSellers ? bestSellingProducts : bestSellingProducts.slice(0, 5)

  const saveCategory = async (event) => {
    event.preventDefault()
    try {
      const payload = {
        title: categoryForm.title,
        description: categoryForm.description,
        coverImage: categoryForm.coverImage,
      }
      const saved = categoryForm.categoryId
        ? await updateCategory(categoryForm.categoryId, payload)
        : await createCategory(payload)
      setCategories((current) => [saved, ...current.filter((category) => category.categoryId !== saved.categoryId)])
      setCategoryForm(emptyCategory)
      setStatus({ type: 'success', message: 'Category saved.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  const removeCategory = async (categoryId) => {
    try {
      await deleteCategory(categoryId)
      setCategories((current) => current.filter((category) => category.categoryId !== categoryId))
      setStatus({ type: 'success', message: 'Category deleted.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  const saveProduct = async (event) => {
    event.preventDefault()
    try {
      const payload = {
        title: productForm.title,
        description: productForm.description,
        price: Number(productForm.price || 0),
        discountedPrice: Number(productForm.discountedPrice || 0),
        quantity: Number(productForm.quantity || 0),
        productImageName: productForm.productImageName,
        categoryId: productForm.categoryId,
        live: productForm.live,
        stock: productForm.stock,
      }
      const saved = productForm.productId
        ? await updateProduct(productForm.productId, payload)
        : await createProduct(payload)
      setProducts((current) => [saved, ...current.filter((product) => product.productId !== saved.productId)])
      setProductForm(emptyProduct)
      setStatus({ type: 'success', message: 'Product saved.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  const editProduct = (product) => {
    setProductForm({
      productId: product.productId,
      title: product.title || '',
      description: product.description || '',
      price: product.price || '',
      discountedPrice: product.discountedPrice || '',
      quantity: product.quantity || '',
      productImageName: product.productImageName || '',
      categoryId: product.category?.categoryId || '',
      live: Boolean(product.live),
      stock: Boolean(product.stock),
    })
  }

  const removeProduct = async (productId) => {
    try {
      await deleteProduct(productId)
      setProducts((current) => current.filter((product) => product.productId !== productId))
      setStatus({ type: 'success', message: 'Product deleted.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  const moveOrder = async (order) => {
    const nextStatus = order.orderStatus === 'PAID' ? 'SHIPPED' : 'DELIVERED'
    try {
      const updated = await updateOrderStatus(order.orderId, nextStatus)
      setOrders((current) => current.map((item) => (item.orderId === updated.orderId ? updated : item)))
      setStatus({ type: 'success', message: `Order marked ${nextStatus}.` })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <header className="sticky top-0 z-40 border-b border-white/10 bg-slate-950/95 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <span className="grid h-11 w-11 place-items-center rounded-lg bg-cyan-500 text-white">
              <FiBarChart2 />
            </span>
            <div>
              <p className="text-lg font-black text-white">SparkGadget Admin</p>
              <p className="text-xs font-bold uppercase text-cyan-200">{user?.name || 'Administrator'}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={loadDashboard}
              disabled={loading}
              className="group inline-flex h-10 items-center gap-2 rounded-lg border border-white/10 px-3 text-sm font-black text-white transition hover:-translate-y-0.5 hover:border-cyan-300/45 hover:bg-cyan-400/10 active:translate-y-0 disabled:cursor-wait disabled:opacity-75"
            >
              <FiRefreshCw className={`transition-transform duration-500 group-hover:rotate-180 ${loading ? 'animate-spin text-cyan-200' : ''}`} />
              {loading ? 'Refreshing...' : 'Refresh'}
            </button>
            <button type="button" onClick={handleLogout} className="inline-flex h-10 items-center gap-2 rounded-lg bg-white px-3 text-sm font-black text-slate-950">
              <FiLogOut />
              Logout
            </button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:px-8">
        {status.message && (
          <div className={`rounded-lg border px-4 py-3 text-sm font-bold ${status.type === 'error' ? 'border-rose-400/30 bg-rose-400/10 text-rose-100' : 'border-emerald-400/30 bg-emerald-400/10 text-emerald-100'}`}>
            {status.message}
          </div>
        )}

        <section className="grid gap-4 md:grid-cols-5">
          <Metric icon={FiBarChart2} label="Total sales" value={currency.format(analytics?.totalSales || 0)} />
          <Metric icon={FiBarChart2} label="Weekly sales" value={currency.format(analytics?.weeklySales || 0)} />
          <Metric icon={FiBarChart2} label="Monthly sales" value={currency.format(analytics?.monthlySales || 0)} />
          <Metric icon={FiPackage} label="Orders" value={analytics?.numberOfOrders || paidOrders.length} />
          <Metric icon={FiBox} label="Revenue" value={currency.format(analytics?.revenue || 0)} />
        </section>

        <section className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <AdminSection title="Categories" icon={FiGrid}>
            <form className="grid gap-3" onSubmit={saveCategory}>
              <PanelInput label="Title" value={categoryForm.title} onChange={(value) => setCategoryForm({ ...categoryForm, title: value })} required />
              <PanelInput label="Description" value={categoryForm.description} onChange={(value) => setCategoryForm({ ...categoryForm, description: value })} />
              <PanelInput label="Cover image URL" value={categoryForm.coverImage} onChange={(value) => setCategoryForm({ ...categoryForm, coverImage: value })} />
              <FormActions editing={Boolean(categoryForm.categoryId)} onCancel={() => setCategoryForm(emptyCategory)} />
            </form>
            <div className="mt-5 grid gap-2">
              {visibleCategories.map((category) => (
                <DataRow key={category.categoryId} title={category.title} subtitle={category.description}>
                  <IconAction icon={FiEdit3} label="Edit" onClick={() => setCategoryForm(category)} />
                  <IconAction icon={FiTrash2} label="Delete" onClick={() => removeCategory(category.categoryId)} danger />
                </DataRow>
              ))}
              <ShowMoreButton
                total={categories.length}
                visible={visibleCategories.length}
                expanded={showAllCategories}
                onClick={() => setShowAllCategories((current) => !current)}
              />
            </div>
          </AdminSection>

          <AdminSection title="Products" icon={FiPackage}>
            <form className="grid gap-3" onSubmit={saveProduct}>
              <div className="grid gap-3 sm:grid-cols-2">
                <PanelInput label="Title" value={productForm.title} onChange={(value) => setProductForm({ ...productForm, title: value })} required />
                <label className="grid gap-2 text-sm font-bold text-slate-300">
                  Category
                  <select value={productForm.categoryId} onChange={(event) => setProductForm({ ...productForm, categoryId: event.target.value })} className="h-11 rounded-lg border border-white/10 bg-slate-950/80 px-3 text-sm font-semibold text-white outline-none focus:border-cyan-300">
                    <option value="">Uncategorized</option>
                    {categories.map((category) => <option key={category.categoryId} value={category.categoryId}>{category.title}</option>)}
                  </select>
                </label>
              </div>
              <PanelTextarea label="Description" value={productForm.description} onChange={(value) => setProductForm({ ...productForm, description: value })} />
              <div className="grid gap-3 sm:grid-cols-3">
                <PanelInput label="Original price" type="number" value={productForm.price} onChange={(value) => setProductForm({ ...productForm, price: value })} required />
                <PanelInput label="Discounted price" type="number" value={productForm.discountedPrice} onChange={(value) => setProductForm({ ...productForm, discountedPrice: value })} required />
                <PanelInput label="Quantity" type="number" value={productForm.quantity} onChange={(value) => setProductForm({ ...productForm, quantity: value })} required />
              </div>
              <PanelInput label="Image URL or file name" value={productForm.productImageName} onChange={(value) => setProductForm({ ...productForm, productImageName: value })} />
              <div className="grid gap-3 sm:grid-cols-2">
                <Toggle checked={productForm.stock} label="In stock" onChange={(checked) => setProductForm({ ...productForm, stock: checked })} />
                <Toggle checked={productForm.live} label="Visible to users" onChange={(checked) => setProductForm({ ...productForm, live: checked })} />
              </div>
              <FormActions editing={Boolean(productForm.productId)} onCancel={() => setProductForm(emptyProduct)} />
            </form>
            <div className="mt-5 grid gap-2">
              {visibleProducts.map((product) => (
                <DataRow key={product.productId} title={product.title} subtitle={`${currency.format(product.discountedPrice)} | ${product.stock ? 'In stock' : 'Out of stock'}`}>
                  <IconAction icon={FiEdit3} label="Edit" onClick={() => editProduct(product)} />
                  <IconAction icon={FiTrash2} label="Delete" onClick={() => removeProduct(product.productId)} danger />
                </DataRow>
              ))}
              <ShowMoreButton
                total={products.length}
                visible={visibleProducts.length}
                expanded={showAllProducts}
                onClick={() => setShowAllProducts((current) => !current)}
              />
            </div>
          </AdminSection>
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <AdminSection title="Customer Orders" icon={FiPackage}>
            <div className="grid gap-3">
              {visibleOrders.map((order) => (
                <DataRow key={order.orderId} title={`${currency.format(order.orderAmount)} - ${order.billingName || 'Customer'}`} subtitle={`Order: ${formatOrderStatus(order.orderStatus)} | Payment: ${order.paymentStatus}`}>
                  {(order.orderStatus === 'PAID' || isShippedStatus(order.orderStatus)) && (
                    <button type="button" onClick={() => moveOrder(order)} className="inline-flex h-9 items-center gap-2 rounded-lg bg-cyan-500 px-3 text-xs font-black text-white">
                      <FiCheckCircle />
                      Mark {order.orderStatus === 'PAID' ? 'Shipped' : 'Delivered'}
                    </button>
                  )}
                </DataRow>
              ))}
              <ShowMoreButton
                total={paidOrders.length}
                visible={visibleOrders.length}
                expanded={showAllOrders}
                onClick={() => setShowAllOrders((current) => !current)}
              />
              {!paidOrders.length && <EmptyState text={loading ? 'Loading paid orders...' : 'No paid orders found.'} />}
            </div>
          </AdminSection>

          <AdminSection title="Best Selling Products" icon={FiBarChart2}>
            <div className="grid gap-3">
              {visibleBestSellers.map((product) => (
                <DataRow key={product.productId} title={product.title} subtitle={`${product.quantitySold} sold | ${currency.format(product.revenue)}`} />
              ))}
              <ShowMoreButton
                total={bestSellingProducts.length}
                visible={visibleBestSellers.length}
                expanded={showAllBestSellers}
                onClick={() => setShowAllBestSellers((current) => !current)}
              />
              {!bestSellingProducts.length && <EmptyState text="Best-selling products will appear after paid orders." />}
            </div>
          </AdminSection>
        </section>
      </div>
    </main>
  )
}

function Metric({ icon: Icon, label, value }) {
  return (
    <div className="rounded-lg border border-white/10 bg-white/[0.04] p-4">
      <Icon className="text-xl text-cyan-200" />
      <p className="mt-3 text-xs font-black uppercase text-slate-500">{label}</p>
      <p className="mt-1 text-xl font-black text-white">{value}</p>
    </div>
  )
}

function AdminSection({ title, icon: Icon, children }) {
  return (
    <section className="rounded-lg border border-white/10 bg-white/[0.04] p-5">
      <div className="mb-5 flex items-center gap-2">
        <Icon className="text-cyan-200" />
        <h2 className="text-lg font-black text-white">{title}</h2>
      </div>
      {children}
    </section>
  )
}

function PanelInput({ label, value, onChange, type = 'text', required = false }) {
  return (
    <label className="grid gap-2 text-sm font-bold text-slate-300">
      {label}
      <input required={required} type={type} value={value} onChange={(event) => onChange(event.target.value)} className="h-11 rounded-lg border border-white/10 bg-slate-950/80 px-3 text-sm font-semibold text-white outline-none focus:border-cyan-300" />
    </label>
  )
}

function PanelTextarea({ label, value, onChange }) {
  return (
    <label className="grid gap-2 text-sm font-bold text-slate-300">
      {label}
      <textarea value={value} onChange={(event) => onChange(event.target.value)} className="min-h-24 rounded-lg border border-white/10 bg-slate-950/80 px-3 py-2 text-sm font-semibold text-white outline-none focus:border-cyan-300" />
    </label>
  )
}

function Toggle({ checked, label, onChange }) {
  return (
    <label className="flex h-11 items-center justify-between rounded-lg border border-white/10 bg-slate-950/80 px-3 text-sm font-bold text-slate-200">
      {label}
      <input type="checkbox" checked={checked} onChange={(event) => onChange(event.target.checked)} className="h-4 w-4 accent-cyan-400" />
    </label>
  )
}

function FormActions({ editing, onCancel }) {
  return (
    <div className="flex gap-2">
      <button type="submit" className="inline-flex h-11 flex-1 items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-cyan-400 text-sm font-black text-white">
        {editing ? <FiSave /> : <FiPlus />}
        {editing ? 'Update' : 'Create'}
      </button>
      {editing && (
        <button type="button" onClick={onCancel} className="h-11 rounded-lg border border-white/10 px-4 text-sm font-black text-white">
          Cancel
        </button>
      )}
    </div>
  )
}

function DataRow({ title, subtitle, children }) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-lg border border-white/10 bg-slate-950/45 p-3">
      <div className="min-w-0">
        <p className="truncate text-sm font-black text-white">{title}</p>
        <p className="mt-1 line-clamp-2 text-xs font-semibold text-slate-400">{subtitle}</p>
      </div>
      {children && <div className="flex shrink-0 gap-2">{children}</div>}
    </div>
  )
}

function ShowMoreButton({ total, visible, expanded, onClick }) {
  if (total <= 5) return null

  return (
    <button type="button" onClick={onClick} className="h-10 rounded-lg border border-cyan-300/20 bg-cyan-400/10 text-sm font-black text-cyan-100">
      {expanded ? 'Show less' : `Show more (${total - visible})`}
    </button>
  )
}

function IconAction({ icon: Icon, label, onClick, danger = false }) {
  return (
    <button type="button" onClick={onClick} className={`grid h-9 w-9 place-items-center rounded-lg border ${danger ? 'border-rose-400/30 text-rose-200' : 'border-cyan-300/25 text-cyan-100'}`} title={label} aria-label={label}>
      <Icon />
    </button>
  )
}

function isShippedStatus(status) {
  return status === 'SHIPPED' || status === 'DISPATCHED'
}

function formatOrderStatus(status) {
  return isShippedStatus(status) ? 'SHIPPED' : status
}

function EmptyState({ text }) {
  return (
    <div className="rounded-lg border border-dashed border-white/15 px-4 py-6 text-center text-sm font-bold text-slate-400">
      {text}
    </div>
  )
}

export default AdminDashboardPage
