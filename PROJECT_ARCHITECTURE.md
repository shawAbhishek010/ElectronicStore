# SparkGadget Architecture

Simple architecture notes for the ElectronicStore project.

## 1. System Flow

```mermaid
flowchart LR
    User[Customer / Admin]
    Frontend[React + Vite Frontend]
    Backend[Spring Boot Backend]
    DB[(MySQL)]
    Razorpay[Razorpay]
    Groq[Groq AI API]
    Images[Product Images]

    User --> Frontend
    Frontend --> Backend
    Backend --> DB
    Backend --> Images
    Backend --> Razorpay
    Backend --> Groq
```

Key notes:

- Frontend: `frontend/`
- Backend: `src/main/java/com/lcwd/electronicStore/ElectronicStore/`
- Config: `src/main/resources/application.properties`
- Frontend API URL comes from `VITE_API_BASE_URL`.
- Secrets must stay in environment variables, not code.

## 2. Code Structure

```mermaid
flowchart TD
    Root[ElectronicStore]

    Root --> Frontend[frontend]
    Frontend --> FPages[pages]
    Frontend --> FComponents[components]
    Frontend --> FRoutes[routes]
    Frontend --> FServices[services]
    Frontend --> FContext[context]

    Root --> Backend[src/main/java/.../ElectronicStore]
    Backend --> Config[config]
    Backend --> Controllers[controller]
    Backend --> Services[services / services/impl]
    Backend --> Repos[repositories]
    Backend --> Entities[entities]
    Backend --> DTOs[dtos]
    Backend --> Security[security]
```

Key notes:

- React pages call files inside `frontend/src/services`.
- Controllers receive API requests.
- Services contain business logic.
- Repositories talk to MySQL.
- DTOs shape request/response data.

## 3. Frontend Startup

```mermaid
flowchart TD
    Index[index.html]
    Main[main.jsx]
    Router[BrowserRouter]
    Auth[AuthProvider]
    App[App.jsx]
    Routes[AppRoutes.jsx]
    Page[Login / Store / Admin Page]

    Index --> Main
    Main --> Router
    Router --> Auth
    Auth --> App
    App --> Routes
    Routes --> Page
```

Key notes:

- Routes are in `frontend/src/routes/AppRoutes.jsx`.
- Auth state is in `frontend/src/context/AuthContext.jsx`.
- JWT token is saved in browser `localStorage`.

## 4. Backend Startup

```mermaid
flowchart TD
    App[ElectronicStoreApplication.java]
    Spring[Spring Boot Starts]
    Config[Load Config]
    Security[Load Security + CORS]
    API[Register Controllers]
    Beans[Register Services + Repositories]
    Seeder[Run DataSeeder]
    Ready[Backend Ready]

    App --> Spring
    Spring --> Config
    Spring --> Security
    Spring --> API
    Spring --> Beans
    Beans --> Seeder
    Seeder --> Ready
```

Key notes:

- Railway provides `PORT`; local fallback is `8081`.
- `DataSeeder` adds starter products/categories in a fresh DB.

## 5. Normal API Request Flow

```mermaid
flowchart LR
    Page[React Page]
    FrontendService[Frontend Service File]
    Axios[apiClient.js]
    Security[Spring Security]
    Controller[Controller]
    Service[Service Impl]
    Repo[Repository]
    DB[(MySQL)]

    Page --> FrontendService
    FrontendService --> Axios
    Axios --> Security
    Security --> Controller
    Controller --> Service
    Service --> Repo
    Repo --> DB
    DB --> Repo
    Repo --> Service
    Service --> Controller
    Controller --> Axios
    Axios --> Page
```

Key notes:

- All frontend API calls should use `frontend/src/services/apiClient.js`.
- `apiClient.js` attaches `Authorization: Bearer <token>` when logged in.
- CORS is configured in `SecurityConfig.java`.

## 6. Login And Security Flow

```mermaid
flowchart TD
    Login[Login / Register UI]
    AuthService[authService.js]
    AuthAPI[/auth APIs]
    AuthController[AuthController]
    UserDetails[CustomUserDetailsService]
    Jwt[JwtHelper]
    BrowserStore[localStorage + AuthContext]
    ProtectedAPI[Protected API Call]
    JwtFilter[JwtAuthenticationFilter]
    Rules[SecurityConfig Rules]
    Guard[SecurityGuard Owner Check]

    Login --> AuthService
    AuthService --> AuthAPI
    AuthAPI --> AuthController
    AuthController --> UserDetails
    AuthController --> Jwt
    Jwt --> BrowserStore
    BrowserStore --> ProtectedAPI
    ProtectedAPI --> JwtFilter
    JwtFilter --> Rules
    Rules --> Guard
```

Key notes:

- `ROLE_USER` opens customer store.
- `ROLE_ADMIN` opens admin dashboard.
- Admin login/register also checks `ADMIN_PORTAL_PASSWORD`.
- `401` usually means missing/expired token.
- `403` usually means wrong role or wrong user ownership.

## 7. Customer Flow

```mermaid
flowchart TD
    Store[MainStorePage.jsx]
    Products[Browse Products]
    Wishlist[Wishlist]
    Cart[Cart]
    Checkout[Checkout]
    Orders[Order History]
    Assistant[AI Assistant]

    Store --> Products
    Products --> Wishlist
    Products --> Cart
    Cart --> Checkout
    Checkout --> Orders
    Store --> Assistant
```

Key notes:

- Public: product/category browsing.
- Logged-in user: cart, wishlist, orders, checkout, assistant.
- Cart and order calculations happen on backend.

## 8. Admin Flow

```mermaid
flowchart TD
    Admin[AdminDashboardPage.jsx]
    Products[Manage Products]
    Categories[Manage Categories]
    Orders[Manage Orders]
    Analytics[View Analytics]
    Backend[Admin Backend APIs]
    DB[(MySQL)]

    Admin --> Products
    Admin --> Categories
    Admin --> Orders
    Admin --> Analytics
    Products --> Backend
    Categories --> Backend
    Orders --> Backend
    Analytics --> Backend
    Backend --> DB
```

Key notes:

- Admin APIs require `ROLE_ADMIN`.
- Product/category create, update, delete are admin-only.
- Analytics are calculated from order data.

## 9. Payment Flow

```mermaid
flowchart TD
    Checkout[Checkout]
    Method{Payment Method}
    Cash[Create Direct Order]
    RazorpayOrder[Create Local Pending Order]
    RazorpayCheckout[Open Razorpay Checkout]
    Result{Payment Result}
    Verify[Verify Signature On Backend]
    Paid[Mark Paid + Clear Cart]
    Failed[Record Failure]

    Checkout --> Method
    Method -->|Cash| Cash
    Cash --> Paid
    Method -->|Razorpay| RazorpayOrder
    RazorpayOrder --> RazorpayCheckout
    RazorpayCheckout --> Result
    Result -->|Success| Verify
    Verify --> Paid
    Result -->|Failure| Failed
```

Key notes:

- Razorpay secret key must stay only on backend.
- Payment success is accepted only after backend signature verification.
- Main payment logic is in `OrderServiceImpl`.

## 10. Assistant Flow

```mermaid
flowchart LR
    Store[MainStorePage]
    AssistantService[assistantService.js]
    Controller[AssistantController]
    GroqService[GroqAssistantService]
    GroqAPI[Groq API]

    Store --> AssistantService
    AssistantService --> Controller
    Controller --> GroqService
    GroqService --> GroqAPI
    GroqAPI --> GroqService
    GroqService --> Store
```

Key notes:

- `GROQ_API_KEY` is backend-only.
- Assistant API requires logged-in user.
- Frontend can fall back to local recommendations if Groq is unavailable.

## 11. Database Relationship Flow

```mermaid
flowchart TD
    User[User]
    Cart[Cart]
    CartItem[CartItem]
    Order[Order]
    OrderItem[OrderItem]
    Wishlist[WishlistItem]
    View[ProductView]
    Product[Product]
    Category[Category]

    User --> Cart
    Cart --> CartItem
    CartItem --> Product

    User --> Order
    Order --> OrderItem
    OrderItem --> Product

    User --> Wishlist
    Wishlist --> Product

    User --> View
    View --> Product

    Category --> Product
```

Key notes:

- User owns cart, wishlist, orders, and product views.
- Product belongs to category.
- Cart items and order items connect products with cart/order records.

## 12. Deployment Flow

```mermaid
flowchart LR
    GitHub[GitHub Repo]
    Vercel[Vercel Frontend]
    RailwayBackend[Railway Backend]
    RailwayDB[(Railway MySQL)]
    Browser[Browser]

    GitHub --> Vercel
    GitHub --> RailwayBackend
    RailwayBackend --> RailwayDB
    Browser --> Vercel
    Vercel --> RailwayBackend
```

Key notes:

- Vercel root directory: `frontend`
- Vercel build command: `npm run build`
- Vercel output directory: `dist`
- Vercel env: `VITE_API_BASE_URL=https://your-railway-backend-url`
- Railway backend root: project root
- Railway backend needs MySQL and app secret variables.
- Add Vercel domain in backend CORS.

## 13. Debug Flow

```mermaid
flowchart TD
    Problem[Problem]
    Browser[Browser Console / Network]
    ApiUrl[VITE_API_BASE_URL]
    Cors[SecurityConfig CORS]
    Logs[Backend Logs]
    DB[Database Variables]
    Auth[JWT / Role / Ownership]

    Problem --> Browser
    Browser --> ApiUrl
    ApiUrl --> Cors
    Cors --> Logs
    Logs --> DB
    Logs --> Auth
```

Key notes:

- CORS error: check frontend domain in `SecurityConfig.java`.
- Railway `502`: check latest backend deployment logs.
- Empty products: check database connection and `DataSeeder`.
- Login issue: check JWT secret, role, and admin portal password.
- Payment issue: check Razorpay variables.
- Assistant issue: check Groq variables.

