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
      <span className="mb-2 block text-sm font-bold text-slate-700">{label}</span>
      <span className="flex min-h-12 items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 transition focus-within:border-blue-500 focus-within:bg-white focus-within:shadow-lg focus-within:shadow-blue-500/10">
        {Icon && <Icon className="shrink-0 text-lg text-slate-400" />}
        <input
          className="min-w-0 flex-1 bg-transparent text-sm font-semibold text-slate-900 outline-none placeholder:text-slate-400"
          {...register}
          {...inputProps}
        />
      </span>
      {error && (
        <span className="mt-2 flex items-center gap-1 text-xs font-semibold text-red-500">
          <FiAlertCircle />
          {error.message}
        </span>
      )}
    </label>
  )
}

export default FormInput
