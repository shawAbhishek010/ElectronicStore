import AppRoutes from './routes/AppRoutes.jsx'

/*
Purpose
Root component for the React application.
Responsibilities
Render route-level pages and keep App small as the project grows.
Props
None.
*/
function App() {
  return (
    <>
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[70] focus:rounded-lg focus:bg-white focus:px-4 focus:py-3 focus:text-sm focus:font-black focus:text-zinc-950"
      >
        Skip to content
      </a>
      <AppRoutes />
      <div className="app-grain" aria-hidden="true" />
    </>
  )
}

export default App
