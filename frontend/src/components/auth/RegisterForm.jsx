import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { FiLock, FiMail, FiPhone, FiUser } from 'react-icons/fi'
import { useAuth } from '../../hooks/useAuth.js'
import FormInput from '../common/FormInput.jsx'
import PrimaryButton from '../common/PrimaryButton.jsx'

/*
Purpose
Registration form for new customers.
Responsibilities
Validate account fields, confirm password, and call backend registration through context.
Props
onLogin switches the card back to login mode.
*/
function RegisterForm({ onLogin }) {
  const { registerUser } = useAuth()
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
  })

  const password = watch('password')

  const onSubmit = async (values) => {
    setFormError('')
    setSuccessMessage('')
    try {
      // Step 1: Map frontend form fields to backend UserDto shape.
      await registerUser({
        name: values.name,
        email: values.email,
        password: values.password,
        gender: 'Other',
        about: `Phone: ${values.phone}`,
      })

      // Step 2: Reset the form after successful registration.
      reset()

      // Step 3: Ask the user to login because the backend register API returns user data, not a token.
      setSuccessMessage('Account created successfully. Please login now.')
    } catch (error) {
      setFormError(error.message)
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
      <div>
        <p className="text-2xl font-black text-slate-950">Create your account</p>
        <p className="mt-2 text-sm leading-6 text-slate-500">
          Join the store and get ready for a polished shopping experience.
        </p>
      </div>

      {formError && (
        <div className="rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm font-semibold text-red-600">
          {formError}
        </div>
      )}
      {successMessage && (
        <div className="rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700">
          {successMessage}
        </div>
      )}

      <FormInput
        label="Name"
        type="text"
        placeholder="Your full name"
        icon={FiUser}
        error={errors.name}
        register={register('name', {
          required: 'Name is required',
          minLength: {
            value: 3,
            message: 'Name must be at least 3 characters',
          },
        })}
      />

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
        label="Phone"
        type="tel"
        placeholder="9876543210"
        icon={FiPhone}
        error={errors.phone}
        register={register('phone', {
          required: 'Phone is required',
          pattern: {
            value: /^[0-9]{10}$/,
            message: 'Enter a 10 digit phone number',
          },
        })}
      />

      <div className="grid gap-4 sm:grid-cols-2">
        <FormInput
          label="Password"
          type="password"
          placeholder="Password"
          icon={FiLock}
          error={errors.password}
          register={register('password', {
            required: 'Password is required',
            minLength: {
              value: 6,
              message: 'Minimum 6 characters',
            },
          })}
        />
        <FormInput
          label="Confirm"
          type="password"
          placeholder="Confirm"
          icon={FiLock}
          error={errors.confirmPassword}
          register={register('confirmPassword', {
            required: 'Please confirm password',
            validate: (value) => value === password || 'Passwords do not match',
          })}
        />
      </div>

      <PrimaryButton type="submit" loading={isSubmitting}>
        Register
      </PrimaryButton>

      <p className="text-center text-sm font-semibold text-slate-500">
        Already have an account?{' '}
        <button type="button" onClick={onLogin} className="font-black text-blue-600 hover:text-blue-700">
          Login
        </button>
      </p>
    </form>
  )
}

export default RegisterForm
