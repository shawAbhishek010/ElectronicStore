# SparkGadget Project Architecture

This document is an onboarding guide for developers joining the SparkGadget / ElectronicStore project. It explains how the codebase is organized, how requests enter the system, how they move through frontend and backend layers, where data is saved, and where a developer should make changes for common features.

## 1. High-Level Overview

SparkGadget is a full-stack electronics store application.

- Frontend: React 19, Vite, React Router, Axios, Tailwind CSS.
- Backend: Spring Boot 3.5, Java 21, Spring MVC, Spring Security, JWT, Spring Data JPA.
- Database: MySQL.
- Payments: Razorpay integration through backend service methods.
- AI assistant: Groq chat-completions API, called only from backend.
- Images: Product images can be local files under `images/products/` or remote URLs.

The application has two main user experiences:

- Customer store: login/register, browse products, search, cart, wishlist, recently viewed products, checkout, orders, delivery confirmation, assistant.
- Admin dashboard: catalog/category management, order management, analytics.

## 2. Repository Structure

```text
ElectronicStore/
  frontend/
    src/
      components/       Reusable React UI components
      context/          Global React auth state
      hooks/            Small custom React hooks
      pages/            Full page experiences
      routes/           Browser route definitions and protected routes
      services/         Axios API wrappers
      utils/            Frontend-only product and assistant helpers
      App.jsx           Root app shell
      main.jsx          React bootstrap entry
      index.css         Global styles / Tailwind
    public/             Static browser assets
    package.json        Frontend dependencies and scripts
    vite.config.js      Vite config

  src/main/java/com/lcwd/electronicStore/ElectronicStore/
    config/             Spring beans, security config, Razorpay config, seed data
    controller/         REST API endpoints
    dtos/               Request and response objects exposed to API clients
    entities/           JPA database entities
    exceptions/         Custom exceptions and centralized exception handling
    helper/             Shared backend helper utilities
    repositories/       Spring Data JPA repositories
    security/           JWT, user details service, authorization guard
    services/           Service interfaces
    services/impl/      Business logic implementations
    ElectronicStoreApplication.java

  src/main/resources/
    application.properties

  src/test/
    Backend tests

  images/
    products/           Local product image storage
```

## 3. Runtime Entry Points

### Frontend Entry

The browser starts at `frontend/index.html`, which loads `frontend/src/main.jsx`.

`main.jsx` mounts React into the DOM and wraps the app with:

- `BrowserRouter` for route handling.
- `AuthProvider` for login state and JWT storage.
- `App` for the root UI shell.

Flow:

```text
index.html
  -> src/main.jsx
    -> <BrowserRouter>
      -> <AuthProvider>
        -> <App />
          -> <AppRoutes />
            -> Page component
```

### Backend Entry

The backend starts from:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/ElectronicStoreApplication.java
```

Spring Boot scans the package tree and registers:

- `@RestController` classes as API endpoints.
- `@Service` classes as business logic beans.
- `JpaRepository` interfaces as database access beans.
- `@Configuration` classes as framework/application config.
- `@Component` classes like JWT filters, guards, and data seeders.

## 4. End-to-End Request Flow

Most browser-to-database requests follow this path:

```text
React page/component
  -> frontend/src/services/*.js
    -> Axios apiClient
      -> HTTP request to Spring Boot
        -> CORS check
        -> JwtAuthenticationFilter
        -> Spring Security route authorization
        -> Controller method
        -> Service interface
        -> Service implementation
        -> Repository
        -> JPA entity
        -> MySQL database
        -> Entity returned
        -> DTO mapped/build
        -> Controller ResponseEntity
        -> JSON response
        -> Axios response
        -> React state update
        -> UI rerender
```

Example: loading live products in the store

```text
MainStorePage.jsx
  -> getProducts() in catalogService.js
    -> apiClient.get('/products/live')
      -> ProductController.getAllLive(...)
        -> ProductService.getAllLive(...)
          -> ProductServiceImpl.getAllLive(...)
            -> ProductRepository.findByLiveTrue(...)
              -> products table
            -> PageableHelper.getPageableResponse(...)
          -> PageableResponse<ProductDto>
      -> JSON
    -> service returns response.data.content
  -> page stores products in React state
  -> UI renders catalog cards
```

Example: adding a product to cart

```text
MainStorePage.jsx
  -> addCartItem(userId, productId, quantity)
    -> apiClient.post('/carts/{userId}', { productId, quantity })
      -> Axios adds Authorization: Bearer <token>
      -> JwtAuthenticationFilter validates token
      -> SecurityGuard confirms the token owner matches {userId}
      -> CartController.addItemToCart(...)
        -> CartService.addItemToCart(...)
          -> CartServiceImpl.addItemToCart(...)
            -> UserRepository loads user
            -> ProductRepository loads product
            -> CartRepository loads or creates cart
            -> CartItem added or quantity increased
            -> CartRepository saves cart and items
          -> CartDto
      -> JSON CartDto
  -> React updates cart state
```

## 5. Frontend Architecture

### Routing

Routes are defined in:

```text
frontend/src/routes/AppRoutes.jsx
```

Important routes:

| Browser path | Page | Access |
| --- | --- | --- |
| `/login` | `AuthLandingPage` | Public |
| `/signup` | `AuthLandingPage` | Public |
| `/store` | `MainStorePage` | Authenticated user, `ROLE_USER` |
| `/admin` | `AdminDashboardPage` | Authenticated admin, `ROLE_ADMIN` |

`ProtectedRoute` checks:

- Whether a JWT/user exists in auth context.
- Whether the logged-in user's role matches the page requirement.
- If not authenticated, it redirects to `/login`.
- If role is wrong, it redirects to the correct area.

### Auth State

Auth state lives in:

```text
frontend/src/context/AuthContext.jsx
```

It stores:

- `electronic_store_token` in `localStorage`.
- `electronic_store_user` in `localStorage`.
- Current user object in React state.
- Login/register/logout helper functions.

Login flow:

```text
LoginForm/AuthLandingPage
  -> useAuth().login(credentials)
    -> loginUser(credentials)
      -> POST /auth/login
    -> save token and user to localStorage
    -> update React auth context
    -> navigate to /store or /admin depending on role
```

### API Client

All frontend HTTP calls should go through:

```text
frontend/src/services/apiClient.js
```

Responsibilities:

- Sets base URL from `VITE_API_BASE_URL`, defaulting to `http://localhost:8081`.
- Adds `Authorization: Bearer <token>` automatically when a token exists.
- Handles `401` by clearing local auth data and redirecting to `/login`.
- Converts backend error responses into JavaScript `Error` objects.

Because of this, page components should not manually add JWT headers.

### Frontend Service Files

Service files wrap backend endpoints and keep API details out of page components.

| File | Purpose |
| --- | --- |
| `authService.js` | Login and registration |
| `catalogService.js` | Public categories, live products, product search |
| `cartService.js` | Load cart, add items, update quantity, remove, clear |
| `wishlistService.js` | Load, add, remove wishlist products |
| `productViewService.js` | Track and load recently viewed products |
| `orderService.js` | Checkout, Razorpay order creation/verification/failure, user orders, delivery confirmation |
| `adminService.js` | Admin analytics, catalog management, order status updates |
| `assistantService.js` | Calls backend assistant proxy |
| `userService.js` | Current user profile load/update |

### Main Frontend Pages

| Page | Role |
| --- | --- |
| `AuthLandingPage.jsx` | Login/register experience |
| `MainStorePage.jsx` | Customer store, product browsing, cart, checkout, wishlist, assistant |
| `AdminDashboardPage.jsx` | Admin products/categories/orders/analytics |

When adding new UI features, first decide whether the feature belongs in a page, a reusable component, or a service wrapper.

## 6. Backend Architecture

The backend follows a layered Spring architecture.

```text
Controller layer
  Receives HTTP requests and validates route/body parameters.

Service layer
  Contains business rules and workflow logic.

Repository layer
  Reads/writes database records through Spring Data JPA.

Entity layer
  Represents database tables and relationships.

DTO layer
  Represents request/response payloads sent over the API.
```

### Controller Layer

Controllers are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/controller/
```

| Controller | Base path | Purpose |
| --- | --- | --- |
| `AuthController` | `/auth` | Register and login |
| `UserController` | `/users` | User profile and admin user operations |
| `CategoryController` | `/categories` | Category catalog and category-product assignment |
| `ProductController` | `/products` | Product catalog, admin product CRUD, product images |
| `CartController` | `/carts` | User cart operations |
| `WishlistController` | `/wishlist` | User wishlist operations |
| `ProductViewController` | `/product-views` | Recently viewed product tracking |
| `OrderController` | `/orders` | Orders, Razorpay, delivery confirmation, admin order management |
| `AdminAnalyticsController` | `/admin/analytics` | Admin dashboard analytics |
| `AssistantController` | `/assistant` | Authenticated Groq assistant proxy |

Controllers should stay thin. They should receive requests, call services, and return responses. Business rules should usually live in service implementations.

### Service Layer

Service interfaces are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/services/
```

Implementations are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/services/impl/
```

Important implementations:

| Implementation | Main responsibility |
| --- | --- |
| `UserServicesImpl` | Create/update/delete/search users, encode passwords, map user DTOs |
| `CategoryServiceImpl` | Category CRUD and category search |
| `ProductServiceImpl` | Product CRUD, live catalog, search, category assignment |
| `CartServiceImpl` | Add/update/remove/clear cart items and compute cart totals |
| `OrderServiceImpl` | Cash order creation, Razorpay order creation, payment verification/failure, status changes |
| `FileServiceImpl` | Save uploaded product images and read local image files |
| `GroqAssistantService` | Build assistant prompts, call Groq API, parse answer |

### Repository Layer

Repositories are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/repositories/
```

They extend `JpaRepository` and use Spring Data method names for queries.

| Repository | Entity | Important queries |
| --- | --- | --- |
| `UserRepository` | `User` | `findByEmail`, `findByNameContaining` |
| `CategoryRepository` | `Category` | `findByTitleContaining` |
| `ProductRepository` | `Product` | `findByLiveTrue`, `findByTitleContaining`, `findByCategory`, `findByTitleIgnoreCase` |
| `CartRepository` | `Cart` | `findByUser` |
| `CartItemRepository` | `CartItem` | Basic CRUD |
| `OrderRepository` | `Order` | `findByUser`, latest order by user/payment status |
| `OrderItemRepository` | `OrderItem` | Basic CRUD |
| `WishlistItemRepository` | `WishlistItem` | User wishlist lookup, unique product check |
| `ProductViewRepository` | `ProductView` | Top 20 recently viewed products, user/product lookup |

### DTO Layer

DTOs are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/dtos/
```

DTOs prevent API clients from depending directly on JPA entities. They are also used for request validation with annotations like `@Valid`, `@NotBlank`, `@Size`, and similar constraints.

Common DTOs:

- `UserDto`
- `ProductDto`
- `CategoryDto`
- `CartDto`
- `CartItemDto`
- `CreateOrderRequest`
- `OrderDto`
- `OrderItemDto`
- `JwtRequest`
- `JwtResponse`
- `RazorpayPaymentDto`
- `AssistantChatRequest`
- `AssistantChatResponse`
- `PageableResponse`
- `ApiResponse`

### Entity Layer and Database Relationships

Entities are in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/entities/
```

Main relationships:

```text
User
  -> one-to-many Orders
  -> one-to-one Cart
  -> many WishlistItems
  -> many ProductViews

Category
  -> one-to-many Products

Product
  -> many-to-one Category
  -> many CartItems
  -> many OrderItems
  -> many WishlistItems
  -> many ProductViews

Cart
  -> one-to-one User
  -> one-to-many CartItems

CartItem
  -> many-to-one Cart
  -> many-to-one Product

Order
  -> many-to-one User
  -> one-to-many OrderItems

OrderItem
  -> many-to-one Order
  -> many-to-one Product

WishlistItem
  -> many-to-one User
  -> many-to-one Product

ProductView
  -> many-to-one User
  -> many-to-one Product
```

Important uniqueness rules:

- `cart_items` has a unique `(cart_id, product_id)` pair.
- `order_items` has a unique `(order_id, product_id)` pair.
- `wishlist_items` has a unique `(user_id, product_id)` pair.
- `product_views` has a unique `(user_id, product_id)` pair.

## 7. Security and Authorization Flow

Security is configured in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/config/SecurityConfig.java
```

JWT flow:

```text
Client sends request
  -> If endpoint is secured, client includes Authorization: Bearer <token>
  -> JwtAuthenticationFilter reads token
  -> JwtHelper extracts email/username and validates signature/expiry
  -> CustomUserDetailsService loads User by email
  -> Spring Security stores authentication in SecurityContext
  -> Route authorization rules and @PreAuthorize checks run
  -> Controller method executes
```

Login flow:

```text
POST /auth/login
  -> AuthController.login(...)
    -> AuthenticationManager.authenticate(...)
      -> CustomUserDetailsService.loadUserByUsername(email)
      -> PasswordEncoder validates password
    -> JwtHelper.generateToken(userDetails)
    -> UserService.getUserByEmail(email)
    -> role/admin portal password checks
    -> JwtResponse returned
```

Ownership checks:

```text
@PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
```

`SecurityGuard` compares the requested `userId` with the user ID of the authenticated email. Admin users pass ownership checks automatically.

Role model:

- `ROLE_USER` for customer features.
- `ROLE_ADMIN` for admin dashboard and catalog/order management.

Public backend routes:

- `/auth/**`
- `GET /products/**`
- `GET /categories/**`
- `/error`
- `OPTIONS /**`

Common secured route groups:

- `/admin/**` requires admin.
- Product/category writes require admin.
- Cart/wishlist/product-view APIs require user ownership.
- Order creation requires `ROLE_USER` and matching user ID.
- Assistant chat requires `ROLE_USER`.

## 8. Feature Flows

### Register

```text
Frontend
  AuthLandingPage/RegisterForm
    -> registerUserAccount(payload)
      -> POST /auth/register

Backend
  AuthController.register
    -> normalize requested role
    -> validate admin portal password if role is ROLE_ADMIN
    -> UserService.createUser
      -> UserServicesImpl.createUser
        -> encode password
        -> assign ID
        -> save via UserRepository
        -> map User to UserDto
    -> clear password fields from response
    -> return 201 Created
```

### Login

```text
Frontend
  LoginForm/AuthLandingPage
    -> login(credentials)
      -> loginUser(credentials)
        -> POST /auth/login
      -> save token/user in localStorage
      -> route to /store or /admin

Backend
  AuthController.login
    -> Spring AuthenticationManager verifies email/password
    -> JwtHelper creates JWT
    -> UserService loads UserDto
    -> role selection is verified
    -> admin portal password is verified for admins
    -> JwtResponse returned
```

### Browse Catalog

```text
Frontend
  MainStorePage
    -> getCategories()
      -> GET /categories
    -> getProducts()
      -> GET /products/live
    -> searchProducts(query)
      -> GET /products/search/{query}

Backend
  CategoryController / ProductController
    -> CategoryServiceImpl / ProductServiceImpl
      -> CategoryRepository / ProductRepository
      -> PageableResponse DTO
```

Catalog reads are public, so a user can browse categories and live products without a JWT.

### Admin Product Management

```text
Frontend
  AdminDashboardPage
    -> adminService.createProduct/updateProduct/deleteProduct

Backend
  ProductController or CategoryController
    -> requires ROLE_ADMIN
    -> ProductServiceImpl
      -> validates product/category existence
      -> saves Product entity
      -> maps ProductDto
```

Product image upload:

```text
POST /products/image/{productId}
  -> ProductController.uploadProductImage
    -> FileServiceImpl.uploadImage
    -> ProductService.get/update product image name
```

Product image serving:

```text
GET /products/image/{productId}
  -> if productImageName is http/https, redirect to remote image
  -> otherwise read from images/products/
```

### Cart

```text
Frontend
  MainStorePage
    -> getCart(userId)
    -> addCartItem(userId, productId, quantity)
    -> updateCartItemQuantity(userId, itemId, quantity)
    -> removeCartItem(userId, itemId)
    -> clearCart(userId)

Backend
  CartController
    -> requires current user ownership
    -> CartServiceImpl
      -> loads User and Product
      -> creates Cart if missing
      -> adds/updates/removes CartItem
      -> recalculates totals
      -> returns CartDto
```

### Wishlist

```text
Frontend
  MainStorePage
    -> getWishlist(userId)
    -> addWishlistProduct(userId, productId)
    -> removeWishlistProduct(userId, productId)

Backend
  WishlistController
    -> requires current user ownership
    -> loads User and Product
    -> saves/deletes WishlistItem
    -> returns ProductDto list or ApiResponse
```

Wishlist logic currently lives directly in `WishlistController` instead of a separate service.

### Recently Viewed Products

```text
Frontend
  MainStorePage
    -> trackProductView(userId, productId)
    -> getRecentlyViewed(userId)

Backend
  ProductViewController
    -> requires current user ownership
    -> loads User and Product
    -> creates or updates ProductView
    -> increments view count
    -> returns latest ProductDto list
```

Product view logic currently lives directly in `ProductViewController` instead of a separate service.

### Cash / Direct Order

```text
Frontend
  MainStorePage checkout
    -> createOrder(payload)
      -> POST /orders

Backend
  OrderController.createOrder
    -> requires ROLE_USER and matching request.userId
    -> OrderServiceImpl.createOrder
      -> loads User
      -> reads user's Cart
      -> validates cart/items/prices
      -> creates Order
      -> creates OrderItem rows
      -> sets order/payment status
      -> updates product quantity/stock where applicable
      -> clears cart if order succeeds
      -> saves through OrderRepository/ProductRepository/CartRepository
    -> returns OrderDto
```

### Razorpay Payment

Razorpay uses a multi-step flow so the backend can create an order, verify the signature, and record success/failure.

```text
Step 1: Create Razorpay order
Frontend
  createRazorpayOrder(payload)
    -> POST /orders/razorpay

Backend
  OrderController.createRazorpayOrder
    -> OrderServiceImpl.createRazorpayOrder
      -> validates user/cart
      -> computes amount
      -> creates local pending order
      -> creates Razorpay order response
      -> returns Razorpay order data to browser

Step 2: User pays in Razorpay checkout UI

Step 3: Verify payment
Frontend
  verifyRazorpayPayment(payload)
    -> POST /orders/razorpay/verify

Backend
  OrderController.verifyRazorpayPayment
    -> OrderServiceImpl.verifyRazorpayPayment
      -> verifies Razorpay signature
      -> marks order payment status as PAID
      -> updates order status
      -> clears cart
      -> returns verification response

Failure path
Frontend
  reportRazorpayPaymentFailure(payload)
    -> POST /orders/razorpay/failure

Backend
  OrderServiceImpl.recordRazorpayPaymentFailure
    -> stores failure metadata
    -> keeps order traceable for support/admin review
```

Razorpay settings come from:

```text
razorpay.key-id
razorpay.key-secret
razorpay.currency
```

These are configured in `application.properties`, preferably through environment variables.

### Orders and Delivery

Customer order history:

```text
GET /orders/users/{userId}
  -> OrderController.getOrdersOfUser
  -> SecurityGuard verifies ownership
  -> OrderServiceImpl.getOrdersOfUser
  -> OrderRepository.findByUser
```

Admin order list:

```text
GET /orders
  -> requires ROLE_ADMIN
  -> paginated OrderDto response
```

Admin status update:

```text
PUT /orders/{orderId}/status?status=SHIPPED
  -> requires ROLE_ADMIN
  -> OrderServiceImpl.updateOrderStatus
```

Customer delivery confirmation:

```text
PUT /orders/{orderId}/confirm-delivery
  -> requires ROLE_USER
  -> authenticated email is passed to service
  -> service confirms that the order belongs to that user
  -> order status is updated
```

### Admin Analytics

```text
Frontend
  AdminDashboardPage
    -> getAdminAnalytics()
      -> GET /admin/analytics

Backend
  AdminAnalyticsController
    -> requires ROLE_ADMIN
    -> OrderRepository.findAll()
    -> filters paid orders
    -> calculates total revenue, weekly sales, monthly sales, paid order count
    -> calculates top 5 best-selling products from order items
    -> returns AnalyticsDashboardDto
```

### AI Shopping Assistant

```text
Frontend
  MainStorePage
    -> buildAssistantProductContext(...)
    -> askAssistant({ question, products })
      -> POST /assistant/chat

Backend
  AssistantController.chat
    -> requires ROLE_USER
    -> GroqAssistantService.chat
      -> checks GROQ_API_KEY is configured
      -> builds system prompt and product-context user prompt
      -> POST https://api.groq.com/openai/v1/chat/completions
      -> extracts choices[0].message.content
      -> returns AssistantChatResponse
```

The Groq API key must stay on the backend. The frontend never receives or stores it.

Configuration:

```text
groq.api.key=${GROQ_API_KEY:}
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=${GROQ_MODEL:openai/gpt-oss-20b}
```

If Groq is not configured or unavailable, the frontend currently falls back to local catalog recommendations.

## 9. Backend API Map

### Auth

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Public | Create user or admin account |
| `POST` | `/auth/login` | Public | Validate credentials and return JWT |

### Products

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/products` | Public | Paginated all products |
| `GET` | `/products/live` | Public | Paginated live products |
| `GET` | `/products/{productId}` | Public | Single product |
| `GET` | `/products/search/{query}` | Public | Search by title |
| `GET` | `/products/image/{productId}` | Public | Serve or redirect product image |
| `POST` | `/products` | Admin | Create product |
| `PUT` | `/products/{productId}` | Admin | Update product |
| `DELETE` | `/products/{productId}` | Admin | Delete product |
| `POST` | `/products/image/{productId}` | Admin | Upload product image |

### Categories

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/categories` | Public | Paginated categories |
| `GET` | `/categories/{categoryId}` | Public | Single category |
| `GET` | `/categories/search?keyword=...` | Public | Search categories |
| `GET` | `/categories/{categoryId}/products` | Public | Products in category |
| `POST` | `/categories` | Admin | Create category |
| `PUT` | `/categories/{categoryId}` | Admin | Update category |
| `DELETE` | `/categories/{categoryId}` | Admin | Delete category |
| `POST` | `/categories/{categoryId}/products` | Admin | Create product under category |
| `PUT` | `/categories/{categoryId}/products/{productId}` | Admin | Move product to category |

### Cart

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/carts/{userId}` | Owner user | Load cart |
| `POST` | `/carts/{userId}` | Owner user | Add product to cart |
| `PATCH` | `/carts/{userId}/items/{itemId}` | Owner user | Update quantity |
| `DELETE` | `/carts/{userId}/items/{itemId}` | Owner user | Remove item |
| `DELETE` | `/carts/{userId}` | Owner user | Clear cart |

### Wishlist

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/wishlist/{userId}` | Owner user | Load wishlist |
| `POST` | `/wishlist/{userId}/products/{productId}` | Owner user | Add product |
| `DELETE` | `/wishlist/{userId}/products/{productId}` | Owner user | Remove product |

### Product Views

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/product-views/{userId}` | Owner user | Load recently viewed products |
| `POST` | `/product-views/{userId}/products/{productId}` | Owner user | Track product view |

### Orders

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/orders` | Owner user | Create direct/cash order |
| `POST` | `/orders/razorpay` | Owner user | Create Razorpay order |
| `POST` | `/orders/razorpay/verify` | User | Verify Razorpay payment |
| `POST` | `/orders/razorpay/failure` | User | Record Razorpay failure |
| `GET` | `/orders/users/{userId}` | Owner user/admin | User order history |
| `PUT` | `/orders/{orderId}/confirm-delivery` | User | Confirm delivery |
| `GET` | `/orders` | Admin | Paginated all orders |
| `PUT` | `/orders/{orderId}/status?status=...` | Admin | Update order status |
| `DELETE` | `/orders/{orderId}` | Admin | Delete order |

### Admin

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/admin/analytics` | Admin | Sales/revenue/best-seller dashboard data |

### Assistant

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/assistant/chat` | User | Ask Groq assistant using selected product context |

### Users

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/users/create` | Admin | Create user |
| `PUT` | `/users/update/{userId}` | Owner user/admin | Update user |
| `DELETE` | `/users/delete/{userId}` | Admin | Delete user |
| `GET` | `/users/getAll` | Admin | Paginated users |
| `GET` | `/users/getSingle/{userId}` | Owner user/admin | Single user profile |
| `GET` | `/users/getEmail/{emailId}` | Admin | Find by email |
| `GET` | `/users/search/{keyword}` | Admin | Search users |

## 10. Configuration

Main config file:

```text
src/main/resources/application.properties
```

Important values:

| Property | Purpose |
| --- | --- |
| `server.port` | Backend port, currently `8081` |
| `spring.datasource.url` | MySQL JDBC URL |
| `spring.datasource.username` | MySQL username |
| `spring.datasource.password` | MySQL password |
| `spring.jpa.hibernate.ddl-auto` | Schema update mode |
| `product.image.path` | Local product image directory |
| `jwt.secret` | Secret key used to sign JWT tokens |
| `jwt.expiration-ms` | JWT lifetime |
| `admin.portal.password` | Extra password required for admin portal login/register |
| `razorpay.key-id` | Razorpay public key ID |
| `razorpay.key-secret` | Razorpay backend secret |
| `razorpay.currency` | Payment currency |
| `groq.api.key` | Groq backend API key |
| `groq.api.url` | Groq chat completions endpoint |
| `groq.model` | Groq model name |

Recommended environment variables:

```powershell
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:JWT_SECRET="at_least_32_characters_secret"
$env:ADMIN_PORTAL_PASSWORD="your_admin_portal_password"
$env:RAZORPAY_KEY_ID="your_razorpay_key_id"
$env:RAZORPAY_KEY_SECRET="your_razorpay_key_secret"
$env:GROQ_API_KEY="your_groq_api_key"
$env:GROQ_MODEL="openai/gpt-oss-20b"
```

Frontend API base URL can be configured in:

```text
frontend/.env
```

Example:

```text
VITE_API_BASE_URL=http://localhost:8081
```

## 11. Error Handling

Centralized backend error handling lives in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/exceptions/GlobalExceptionHandler.java
```

It handles:

- `ResourceNotFoundException` as `404`.
- `MethodArgumentNotValidException` as `400` with field-level messages.
- `BadApiRequestException` as `400`.
- `AuthenticationException` as `401`.
- `AccessDeniedException` as `403`.
- `ResponseStatusException` using its own status code.

Frontend error normalization happens in:

```text
frontend/src/services/apiClient.js
```

The interceptor reads backend error fields such as:

- `message`
- `email`
- `password`
- `role`

and converts them into a JavaScript `Error`.

## 12. Data Seeding

Initial categories and products are inserted/updated by:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/config/DataSeeder.java
```

`DataSeeder` runs at application startup because it implements `CommandLineRunner`.

Responsibilities:

- Upsert default categories.
- Upsert default products by title or ID.
- Assign seeded products to categories.
- Merge duplicate categories by normalized title.

This means a fresh database will get starter catalog content automatically when the backend starts.

## 13. Pagination Pattern

Many list endpoints accept:

```text
pageNumber
pageSize
sortBy
sortDir
```

Backend services use Spring Data `Pageable` and return:

```text
PageableResponse<T>
```

Frontend service wrappers often unwrap `response.data.content || []` when pages only need the list.

## 14. Mapping Pattern

The backend uses `ModelMapper`, configured in:

```text
src/main/java/com/lcwd/electronicStore/ElectronicStore/config/ProjectConfig.java
```

Typical flow:

```text
DTO from controller
  -> ModelMapper maps DTO to Entity
  -> Repository saves Entity
  -> ModelMapper maps Entity back to DTO
  -> Controller returns DTO
```

Some workflows build DTOs manually when totals, nested items, payment metadata, or custom fields are needed.

## 15. How to Add a New Feature

### New Backend Feature

1. Add or update an entity if the database shape changes.
2. Add or update a DTO for request/response data.
3. Add repository methods if new queries are needed.
4. Add method to the service interface.
5. Implement logic in `services/impl`.
6. Add controller endpoint.
7. Add route security in `SecurityConfig` or `@PreAuthorize`.
8. Add exception handling only if a new exception type is needed.
9. Add tests for risky business logic.

### New Frontend Feature

1. Add an API wrapper in `frontend/src/services`.
2. Call it from the relevant page or hook.
3. Store results in React state.
4. Update UI.
5. Use auth context instead of reading localStorage directly.
6. Keep endpoint strings in service files, not scattered through pages.

### New Admin API

1. Put admin endpoints under an existing admin controller or `/admin/...`.
2. Require `ROLE_ADMIN`.
3. Add the frontend call to `adminService.js`.
4. Use `AdminDashboardPage.jsx` for the UI unless the admin area is split into more pages later.

### New Customer-Owned API

Use:

```java
@PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
```

when the route contains a `userId` path variable.

This prevents one customer from reading or editing another customer's cart, wishlist, profile, or order history.

## 16. Common Debugging Paths

### Frontend cannot reach backend

Check:

- Backend is running on `server.port`, currently `8081`.
- `VITE_API_BASE_URL` points to the same backend URL.
- CORS origins in `SecurityConfig` include the Vite dev server origin.
- Browser network tab shows the expected endpoint.

### User gets redirected to login

Check:

- `electronic_store_token` exists in browser localStorage.
- Token is not expired.
- Backend `jwt.secret` did not change since login.
- Endpoint is not returning `401`.
- Axios interceptor in `apiClient.js` is clearing auth on `401`.

### User gets forbidden

Check:

- User role is `ROLE_USER` or `ROLE_ADMIN`.
- Frontend route role matches backend role.
- `@PreAuthorize` ownership check uses the correct `userId`.
- Logged-in token email maps to the expected user row.

### Product list is empty

Check:

- `DataSeeder` ran successfully.
- Products have `live=true`.
- Frontend is calling `/products/live`.
- MySQL connection is working.
- `spring.jpa.hibernate.ddl-auto=update` has created/updated tables.

### Cart or order totals look wrong

Check:

- `CartServiceImpl` for cart quantity and total calculations.
- `OrderServiceImpl` for order amount and item totals.
- `PaymentAmountHelper` for payment amount conversions.
- Product `discountedPrice`, `price`, `quantity`, and `stock` values.

### Razorpay is not working

Check:

- `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` are configured.
- Backend is creating a local order before payment.
- Signature verification payload contains the expected order ID, payment ID, and signature.
- Frontend is calling failure reporting when checkout fails.

### Groq assistant is not working

Check:

- `GROQ_API_KEY` is set in the backend environment before Spring Boot starts.
- `groq.model` is a model available to the Groq account.
- User is logged in as `ROLE_USER`.
- Request reaches `POST /assistant/chat`.
- Backend logs show whether Groq returned an API error or the service was unreachable.
- Frontend fallback may hide the failure by showing local recommendations.

## 17. Development Commands

Backend:

```powershell
.\mvnw spring-boot:run
```

Backend tests:

```powershell
.\mvnw test
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Frontend build:

```powershell
cd frontend
npm run build
```

Frontend lint:

```powershell
cd frontend
npm run lint
```

## 18. Important Files for New Developers

Start with these files when trying to understand the project:

| File | Why it matters |
| --- | --- |
| `README.md` | Setup and project overview |
| `PROJECT_ARCHITECTURE.md` | This onboarding architecture guide |
| `src/main/resources/application.properties` | Backend runtime config |
| `SecurityConfig.java` | Route security and CORS |
| `JwtAuthenticationFilter.java` | Per-request JWT validation |
| `AuthController.java` | Login/register flow |
| `ProductController.java` | Catalog/product APIs |
| `OrderController.java` | Checkout/payment/order APIs |
| `CartServiceImpl.java` | Cart business rules |
| `OrderServiceImpl.java` | Order/payment business rules |
| `DataSeeder.java` | Default catalog seed data |
| `frontend/src/services/apiClient.js` | Shared frontend HTTP behavior |
| `frontend/src/routes/AppRoutes.jsx` | Browser routes and role protection |
| `frontend/src/context/AuthContext.jsx` | Frontend auth lifecycle |
| `frontend/src/pages/MainStorePage.jsx` | Main customer workflow |
| `frontend/src/pages/AdminDashboardPage.jsx` | Main admin workflow |

## 19. Architectural Notes and Current Tradeoffs

- Most domain logic is in service implementations, but wishlist and product-view workflows currently place logic directly in controllers. If these features grow, move that logic into services.
- `AdminAnalyticsController` calculates analytics in memory from `orderRepository.findAll()`. This is simple and fine for small data. For a larger store, move analytics to repository queries or database aggregation.
- The frontend currently has large page components. If UI features keep growing, split page sections into smaller components while keeping API calls in service files.
- JWT auth is stateless. Changing `jwt.secret` invalidates existing logins.
- Product image names may be local filenames or remote URLs. `ProductController` handles both.
- Secrets should be provided through environment variables, not hardcoded into committed config files.

## 20. Mental Model

When you are debugging or adding a feature, think in this order:

```text
Browser route
  -> Page component
  -> Frontend service file
  -> apiClient and JWT header
  -> Backend security config/filter
  -> Controller endpoint
  -> Service method
  -> Repository query/save
  -> Entity/table relationship
  -> DTO response
  -> React state/UI update
```

If you can follow that chain for a feature, you can usually find the correct file quickly.
