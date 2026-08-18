# SparkGadget - Electronic Store

SparkGadget is a full-stack electronic store application with a Spring Boot REST API, MySQL persistence, JWT authentication, role-based access control, Razorpay payment flow support, an optional Groq-powered shopping assistant, and a React/Vite frontend.

The project supports two portals:

- User portal for browsing products, managing cart and wishlist, placing orders, tracking product views, and using the shopping assistant.
- Admin portal for managing catalog data, orders, users, product images, and analytics.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.5.0
- Spring Web
- Spring Data JPA
- Spring Security
- JWT 
- MySQL
- Maven
- ModelMapper
- Lombok
- Razorpay configuration support
- Groq chat-completions integration support

### Frontend

- React 19
- Vite
- Tailwind CSS 4
- Axios
- React Router
- React Hook Form
- Framer Motion
- React Icons
- Oxlint

## Project Structure

```text
ElectronicStore/
+-- frontend/                     # React + Vite frontend
|   +-- public/                   # Static frontend assets
|   +-- src/
|   |   +-- components/           # Reusable UI components
|   |   +-- context/              # Auth context
|   |   +-- hooks/                # React hooks
|   |   +-- pages/                # Login, store, admin pages
|   |   +-- routes/               # App route definitions
|   |   +-- services/             # Axios API service modules
|   |   +-- utils/                # Product/recommendation helpers
|   +-- package.json
|   +-- vite.config.js
+-- images/
|   +-- products/                 # Uploaded/local product images
|   +-- users/                    # Uploaded/local user images
+-- src/
|   +-- main/
|   |   +-- java/com/lcwd/electronicStore/ElectronicStore/
|   |   |   +-- config/           # Security, Razorpay, seeding, beans
|   |   |   +-- controller/       # REST controllers
|   |   |   +-- dtos/             # Request/response DTOs
|   |   |   +-- entities/         # JPA entities
|   |   |   +-- exceptions/       # Custom exceptions and handler
|   |   |   +-- helper/           # Utility helpers
|   |   |   +-- repositories/     # Spring Data repositories
|   |   |   +-- security/         # JWT and authorization helpers
|   |   |   +-- services/         # Business logic
|   |   +-- resources/
|   |       +-- application.properties
|   +-- test/                     # Backend tests
+-- pom.xml                       # Backend Maven config
+-- mvnw / mvnw.cmd               # Maven wrapper
+-- README.md
```

## Features

- User registration and login with JWT authentication.
- Role-based access for `ROLE_USER` and `ROLE_ADMIN`.
- Admin portal password check for admin account registration/login.
- Public product and category browsing.
- Product catalog with categories, pricing, discounted pricing, stock, live status, and images.
- Automatic starter data seeding for categories and products.
- Cart management with add, update quantity, remove item, and clear cart.
- Wishlist management.
- Order creation and management.
- Razorpay order creation, verification, and failure handling endpoints.
- User order history.
- Admin order listing and status updates.
- User profile APIs.
- Product view tracking for personalized experiences.
- Admin analytics endpoint.
- Optional AI shopping assistant using Groq.
- React frontend with protected user/admin routes.

## Prerequisites

Install these before running the project:

- Java 21 or newer
- MySQL Server
- Node.js and npm
- Git

You do not need a global Maven install because the project includes Maven wrapper scripts.

## Backend Configuration

Backend configuration is in:

```text
src/main/resources/application.properties
```

Default backend port:

```properties
server.port=8081
```

Default database URL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/electronicStore?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

The app can create the `electronicStore` database automatically if the MySQL user has permission.

## Environment Variables

The application has local defaults, but real credentials should be set through environment variables.

| Variable | Required | Purpose | Default |
| --- | --- | --- | --- |
| `MYSQL_USER` | No | MySQL username | `root` |
| `MYSQL_PASSWORD` | Local only | MySQL password | Empty |
| `JWT_SECRET` | Recommended | Secret used to sign JWT tokens | Local development secret |
| `ADMIN_PORTAL_PASSWORD` | Required for admins | Password required for admin portal access | Empty |
| `RAZORPAY_KEY_ID` | For payments | Razorpay public key id | Empty |
| `RAZORPAY_KEY_SECRET` | For payments | Razorpay secret key | Empty |
| `GROQ_API_KEY` | For assistant | Groq API key | Empty |
| `GROQ_MODEL` | No | Groq model name | `openai/gpt-oss-20b` |
| `VITE_API_BASE_URL` | No | Frontend API base URL | `http://localhost:8081` |

### PowerShell Example

```powershell
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:JWT_SECRET="change-this-to-a-long-random-secret"
$env:ADMIN_PORTAL_PASSWORD="your-admin-portal-password"
$env:RAZORPAY_KEY_ID="your_razorpay_key_id"
$env:RAZORPAY_KEY_SECRET="your_razorpay_key_secret"
$env:GROQ_API_KEY="your_groq_api_key"
```

## How To Run

Run the backend and frontend in separate terminals.

### 1. Start MySQL

Make sure MySQL Server is running locally.

The application expects:

- Host: `localhost`
- Port: `3306`
- Database: `electronicStore`

If the database does not exist, Spring Boot will try to create it because the JDBC URL includes `createDatabaseIfNotExist=true`.

### 2. Run The Backend

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8081
```

On first startup, `DataSeeder` inserts starter categories and products.

### 3. Run The Frontend

Open a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

The backend CORS configuration allows:

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://localhost:5174`
- `http://127.0.0.1:5174`

## Frontend Routes

| Route | Access | Description |
| --- | --- | --- |
| `/login` | Public | Login page |
| `/signup` | Public | Registration page |
| `/store` | User | Main store page |
| `/admin` | Admin | Admin dashboard |
| `/` | Public | Redirects to `/login` |

## Login And Roles

Users can register through `/signup`.

Supported roles:

- `ROLE_USER`
- `ROLE_ADMIN`

For admin registration or login, the selected admin portal password must match:

```properties
admin.portal.password=${ADMIN_PORTAL_PASSWORD:}
```

Set `ADMIN_PORTAL_PASSWORD` in your local shell or hosting provider before creating or logging in as an admin.

## API Overview

Base URL:

```text
http://localhost:8081
```

Most protected endpoints require:

```http
Authorization: Bearer <jwt-token>
```

### Authentication

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Public | Register user/admin |
| `POST` | `/auth/login` | Public | Login and receive JWT |

### Products

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/products` | Public | List products |
| `GET` | `/products/live` | Public | List live products |
| `GET` | `/products/{productId}` | Public | Get product by id |
| `GET` | `/products/search/{query}` | Public | Search products |
| `GET` | `/products/image/{productId}` | Public | Get product image |
| `POST` | `/products` | Admin | Create product |
| `PUT` | `/products/{productId}` | Admin | Update product |
| `DELETE` | `/products/{productId}` | Admin | Delete product |
| `POST` | `/products/image/{productId}` | Admin | Upload product image |

### Categories

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/categories` | Public | List categories |
| `GET` | `/categories/{categoryId}` | Public | Get category by id |
| `GET` | `/categories/search` | Public | Search categories |
| `GET` | `/categories/{categoryId}/products` | Public | Products in category |
| `POST` | `/categories` | Admin | Create category |
| `PUT` | `/categories/{categoryId}` | Admin | Update category |
| `DELETE` | `/categories/{categoryId}` | Admin | Delete category |
| `POST` | `/categories/{categoryId}/products` | Admin | Create product in category |
| `PUT` | `/categories/{categoryId}/products/{productId}` | Admin | Assign/update product category |

### Cart

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/carts/{userId}` | User | Get user cart |
| `POST` | `/carts/{userId}` | User | Add item to cart |
| `PATCH` | `/carts/{userId}/items/{itemId}` | User | Update cart item quantity |
| `DELETE` | `/carts/{userId}/items/{itemId}` | User | Remove cart item |
| `DELETE` | `/carts/{userId}` | User | Clear cart |

### Wishlist

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/wishlist/{userId}` | User | Get wishlist |
| `POST` | `/wishlist/{userId}/products/{productId}` | User | Add product to wishlist |
| `DELETE` | `/wishlist/{userId}/products/{productId}` | User | Remove product from wishlist |

### Orders And Payments

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/orders` | User | Create order |
| `POST` | `/orders/razorpay` | User | Create Razorpay order |
| `POST` | `/orders/razorpay/verify` | User | Verify Razorpay payment |
| `POST` | `/orders/razorpay/failure` | User | Record payment failure |
| `GET` | `/orders/users/{userId}` | User/Admin | Get user orders |
| `PUT` | `/orders/{orderId}/confirm-delivery` | User | Confirm delivery |
| `GET` | `/orders` | Admin | List all orders |
| `PUT` | `/orders/{orderId}/status` | Admin | Update order status |
| `DELETE` | `/orders/{orderId}` | Admin | Delete order |

### Users

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/users/getSingle/{userId}` | User/Admin owner check | Get user profile |
| `PUT` | `/users/update/{userId}` | User/Admin owner check | Update user profile |
| `POST` | `/users/create` | Admin | Create user |
| `GET` | `/users/getAll` | Admin | List users |
| `GET` | `/users/getEmail/{emailId}` | Admin | Find user by email |
| `GET` | `/users/search/{keyword}` | Admin | Search users |
| `DELETE` | `/users/delete/{userId}` | Admin | Delete user |

### Assistant, Views, And Analytics

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/assistant/chat` | User | Chat with shopping assistant |
| `GET` | `/product-views/{userId}` | User | Get product view history |
| `POST` | `/product-views/{userId}/products/{productId}` | User | Record product view |
| `GET` | `/admin/analytics` | Admin | Get admin dashboard analytics |

## Running Tests

Run backend tests from the project root:

```powershell
.\mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

Run frontend lint:

```powershell
cd frontend
npm run lint
```

Create a frontend production build:

```powershell
cd frontend
npm run build
```

Preview the production build:

```powershell
cd frontend
npm run preview
```

## Image Storage

Product images are stored under:

```text
images/products/
```

User images are stored under:

```text
images/users/
```

The backend product image path is configured with:

```properties
product.image.path=images/products/
```

## Notes For Development

- `spring.jpa.hibernate.ddl-auto=update` updates the local schema automatically during development.
- `spring.jpa.show-sql=true` prints SQL queries in the backend console.
- JWT tokens expire after `3600000` milliseconds, which is 1 hour.
- The frontend stores the JWT token in `localStorage` using `electronic_store_token`.
- If the backend runs on a different URL, set `VITE_API_BASE_URL` before starting Vite.
- Razorpay and Groq features need valid secrets to work fully.

## Troubleshooting

### Backend cannot connect to MySQL

Check that MySQL is running and that `MYSQL_USER` and `MYSQL_PASSWORD` match your local credentials.

### Frontend shows API or CORS errors

Make sure the backend is running on:

```text
http://localhost:8081
```

If using another backend URL, create/update the frontend environment variable:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8081"
npm run dev
```

### Admin login fails

Make sure the account role is `ROLE_ADMIN` and the admin portal password matches `ADMIN_PORTAL_PASSWORD`.

### Payments do not complete

Set both Razorpay variables:

```powershell
$env:RAZORPAY_KEY_ID="your_razorpay_key_id"
$env:RAZORPAY_KEY_SECRET="your_razorpay_key_secret"
```

### Assistant does not respond

Set:

```powershell
$env:GROQ_API_KEY="your_groq_api_key"
```

Then restart the backend.
