import { FiAlertCircle } from 'react-icons/fi'

/*
Purpose
Reusable styled input field for forms.
Responsibilities
Render label, icon, input, and validation error in one consistent design.
Props
label, icon, error, register, and all normal input props.
*/
function FormInput({ label, icon: Icon, error, register, ...inputProps }) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-bold text-zinc-700">{label}</span>
      <span className="flex min-h-12 items-center gap-3 rounded-lg border border-zinc-200 bg-zinc-200 px-4 transition focus-within:border-zinc-500 focus-within:bg-white focus-within:shadow-lg focus-within:shadow-zinc-500/10">
        {Icon && <Icon className="shrink-0 text-lg text-zinc-400" />}
        <input
          className="min-w-0 flex-1 bg-transparent text-sm font-semibold text-zinc-900 outline-none placeholder:text-zinc-400"
          {...register}
          {...inputProps}
        />
      </span>
      {error && (
        <span className="mt-2 flex items-center gap-1 text-xs font-semibold text-zinc-600">
          <FiAlertCircle />
          {error.message}
        </span>
      )}
    </label>
  )
}

export default FormInput
