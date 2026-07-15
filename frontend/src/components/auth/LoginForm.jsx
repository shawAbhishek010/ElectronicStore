import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { FiLock, FiMail } from 'react-icons/fi'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth.js'
import FormInput from '../common/FormInput.jsx'
import PrimaryButton from '../common/PrimaryButton.jsx'

/*
Purpose
Login form for existing customers.
Responsibilities
Validate email/password, call the auth context, and show beginner-friendly feedback.
Props
onCreateAccount switches the card to register mode.
*/
function LoginForm({ onCreateAccount }) {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [formError, setFormError] = useState('')
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const onSubmit = async (values) => {
    setFormError('')
    try {
      // Step 1: Send only login credentials to the backend.
      await login(values)

      // Step 2: The context stores token/user when backend returns success.
      // Step 3: Send the customer to the storefront.
      navigate('/store')
    } catch (error) {
      setFormError(error.message)
    }
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
      <div>
        <p className="text-2xl font-black text-slate-950">Sign in to your account</p>
        <p className="mt-2 text-sm leading-6 text-slate-500">
          Shop electronics faster with a secure session.
        </p>
      </div>

      {formError && (
        <div className="rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm font-semibold text-red-600">
          {formError}
        </div>
      )}

      <FormInput
        label="Email"
        type="email"
        placeholder="abhishek@example.com"
        icon={FiMail}
        error={errors.email}
        register={register('email', {
          required: 'Email is required',
          pattern: {
            value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            message: 'Enter a valid email address',
          },
        })}
      />

      <FormInput
        label="Password"
        type="password"
        placeholder="Enter your password"
        icon={FiLock}
        error={errors.password}
        register={register('password', {
          required: 'Password is required',
          minLength: {
            value: 6,
            message: 'Password must be at least 6 characters',
          },
        })}
      />

      <div className="flex items-center justify-between gap-3 text-sm">
        <label className="flex items-center gap-2 font-semibold text-slate-500">
          <input className="h-4 w-4 rounded border-slate-300 text-blue-600" type="checkbox" />
          Remember me
        </label>
      </div>

      <PrimaryButton type="submit" loading={isSubmitting}>
        Login
      </PrimaryButton>

      <p className="text-center text-sm font-semibold text-slate-500">
        New to SparkGadget?{' '}
        <button type="button" onClick={onCreateAccount} className="font-black text-blue-600 hover:text-blue-700">
          Create Account
        </button>
      </p>
    </form>
  )
}

export default LoginForm
