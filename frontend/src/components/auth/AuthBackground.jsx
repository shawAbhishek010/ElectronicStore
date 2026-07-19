/*
Purpose
Animated visual layer for the auth landing page.
Responsibilities
Render a calm premium surface behind the auth card.
Props
None.
*/
function AuthBackground() {
  return (
    <div className="absolute inset-0 overflow-hidden" aria-hidden="true">
      <img
        className="absolute inset-0 h-full w-full object-cover opacity-35"
        src="/images/electronics-auth-showroom.png"
        alt=""
      />
      <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(9,9,11,0.96)_0%,rgba(24,24,27,0.86)_48%,rgba(39,39,42,0.92)_100%)]" />
      <div className="absolute inset-x-0 top-0 h-40 bg-[linear-gradient(180deg,rgba(245,245,245,0.16),transparent)]" />
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.035)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.025)_1px,transparent_1px)] bg-[size:56px_56px] opacity-45" />
      <div className="absolute bottom-0 left-0 right-0 h-40 bg-[linear-gradient(180deg,transparent,rgba(9,9,11,0.88))]" />
    </div>
  )
}

export default AuthBackground
