# Teklify — Electronic Tendering Platform

A production-grade backend service for real-time electronic tendering and auction management, built with Java and Spring Boot. Features live bidding via WebSockets, phone-based OTP authentication, escrow payment integration, wallet system, and comprehensive auction/product/category management.

> **Background:** MVP was initially shipped with Node.js and Express to validate the core concept, then fully rebuilt with Java and Spring Boot for scalability and long-term maintainability.

---

## Features

- **Real-time Bidding** — WebSocket-based auction engine, load-tested to 500 concurrent connections
- **Phone-based Auth** — OTP verification flow (send → verify → register/login) with JWT + refresh token support
- **Role-Based Access Control** — `BUYER`, `SELLER`, and `ADMIN` roles with method-level security
- **Wallet System** — Deposit, withdraw, and paginated transaction history per user
- **Escrow System** — Auction completion with configurable commission rates; release or refund flows
- **Payment Management** — Credit card and bank account CRUD with default selection and admin verification
- **Auction Lifecycle** — Full auction management: create, live toggle, featured toggle, ending-soon, upcoming, complete, cancel
- **Bid Engine** — Create bids, track highest bids, bid count, can-bid validation, bid cancellation
- **Product Management** — Full CRUD with image upload, category/brand filtering, and pagination
- **Category System** — Hierarchical category tree (root + subcategories) with display ordering
- **Address Management** — Multi-address support per user with invoice address designation
- **File Upload** — Profile photo upload and serving
- **Global Exception Handling** — Standardized `ApiResponse` wrapper across all endpoints

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Cache / OTP | Redis |
| Real-time | WebSockets |
| Auth | JWT, RBAC, OTP |
| Deployment | Linux Server |

---

## Architecture

```
Client (REST / WebSocket)
          │
          ▼
   Spring Boot Backend
   ├── Auth Layer (JWT + RBAC + OTP)
   ├── Auction Engine (WebSocket + Lifecycle Management)
   ├── Bid Engine (Real-time + Validation)
   ├── Escrow & Payment System
   ├── Wallet Service (Deposit / Withdraw / Transactions)
   ├── Product & Category Service
   ├── Address & File Service
   └── Global Exception Handler
          │
          ▼
   PostgreSQL (Spring Data JPA)
          │
   Redis (OTP sessions + caching)
```

---

## API Overview

### Auth — `/api/auth`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/send-otp` | Send OTP to phone number |
| POST | `/verify-otp` | Verify OTP code |
| POST | `/pre-register` | Register after phone verification |
| POST | `/login` | Login with phone + password |
| POST | `/refresh-token` | Refresh access token |
| POST | `/logout` | Invalidate token |
| POST | `/forgot-password` | Send password reset OTP |
| POST | `/verify-password-reset-otp` | Verify password reset OTP |
| PUT | `/reset-password` | Set new password |

### Auctions — `/api/auctions`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | SELLER, ADMIN | Create auction |
| PUT | `/{id}` | SELLER, ADMIN | Update auction |
| GET | `/` | Public | List all auctions (paginated) |
| GET | `/{id}` | Public | Get auction by ID (increments view count) |
| GET | `/active` | Public | Active auctions |
| GET | `/active/type/{type}` | Public | Active auctions by type |
| GET | `/status/{status}` | Public | Auctions by status |
| GET | `/seller/{sellerId}` | Public | Auctions by seller |
| GET | `/my-auctions` | SELLER, ADMIN | Authenticated seller's auctions |
| GET | `/upcoming` | Public | Upcoming auctions (custom date range) |
| GET | `/ending-soon` | Public | Auctions ending within N hours |
| GET | `/featured` | Public | Featured auctions |
| GET | `/live` | Public | Currently live auctions |
| PATCH | `/{id}/status` | SELLER, ADMIN | Update auction status |
| PATCH | `/{id}/toggle-featured` | ADMIN | Toggle featured flag |
| PATCH | `/{id}/toggle-live` | SELLER, ADMIN | Toggle live status |
| DELETE | `/{id}` | SELLER, ADMIN | Delete auction |

### Auction Management — `/api/auction-management`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/{id}/complete-with-escrow` | Complete auction and hold payment in escrow |
| POST | `/{id}/complete` | Complete auction without escrow |
| POST | `/{id}/cancel` | Cancel auction with reason |
| GET | `/{id}/winning-bid` | Get winning bid details |

### Bids — `/api/bids`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | BUYER, SELLER | Place a bid |
| GET | `/{id}` | Public | Get bid by ID |
| GET | `/auction/{auctionId}` | Public | All bids for an auction (paginated) |
| GET | `/auction/{auctionId}/highest` | Public | Top N highest bids |
| GET | `/auction/{auctionId}/highest-amount` | Public | Current highest bid amount |
| GET | `/auction/{auctionId}/count` | Public | Total bid count for auction |
| GET | `/auction/{auctionId}/can-bid` | BUYER, SELLER | Check if user can place a bid |
| GET | `/auction/{auctionId}/my` | BUYER, SELLER | Authenticated user's bids for auction |
| GET | `/my-bids` | BUYER, SELLER | All bids by authenticated user |
| PATCH | `/{id}/cancel` | BUYER, SELLER | Cancel a bid |

### Escrow — `/api/escrow`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/create` | Create escrow for auction winning bid |
| GET | `/my-escrows` | Get authenticated user's escrows |
| GET | `/{id}` | Get escrow details |
| PUT | `/{id}/release` | Release escrow to seller |
| PUT | `/{id}/refund` | Refund escrow to buyer |

### Wallet — `/api/wallets`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{userId}` | Get wallet info and balance |
| POST | `/{userId}/deposit` | Deposit funds |
| POST | `/{userId}/withdraw` | Withdraw funds |
| GET | `/{userId}/transactions` | Paginated transaction history |

### Payment — Bank Accounts — `/api/payment/bank-accounts`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add bank account |
| GET | `/` | List user's bank accounts |
| GET | `/{id}` | Get bank account details |
| PUT | `/{id}/set-default` | Set default bank account |
| PUT | `/{id}/verify` | Admin: verify bank account |
| DELETE | `/{id}` | Delete bank account |

### Payment — Credit Cards — `/api/payment/credit-cards`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add credit card |
| GET | `/` | List user's credit cards |
| GET | `/{id}` | Get credit card details |
| PUT | `/{id}/set-default` | Set default credit card |
| PUT | `/{id}/verify` | Verify credit card via gateway token |
| DELETE | `/{id}` | Delete credit card |

### Products — `/api/products`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | SELLER, ADMIN | Create product |
| GET | `/` | Public | List products (search, category, brand, paginated) |
| GET | `/{id}` | Public | Get product by ID |
| GET | `/category/{categoryId}` | Public | Products by category |
| GET | `/auction/{auctionId}` | Public | Products in an auction |
| PUT | `/{id}` | SELLER, ADMIN | Update product |
| PATCH | `/{id}/status` | SELLER, ADMIN | Update product status |
| POST | `/{id}/images` | SELLER, ADMIN | Add image to product |
| DELETE | `/{id}/images/{imageId}` | SELLER, ADMIN | Remove image |
| DELETE | `/{id}` | SELLER, ADMIN | Delete product |

### Categories — `/api/categories`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | ADMIN | Create category |
| GET | `/` | Public | All categories |
| GET | `/root` | Public | Root categories |
| GET | `/{parentId}/subcategories` | Public | Subcategories |
| GET | `/paginated` | Public | Paginated list |
| GET | `/{id}` | Public | Get by ID |
| PUT | `/{id}` | ADMIN | Update |
| PATCH | `/{id}/display-order` | ADMIN | Update display order |
| DELETE | `/{id}` | ADMIN | Delete |

### Addresses — `/api/addresses`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/user/{userId}` | Add address |
| GET | `/user/{userId}` | Get all user addresses |
| GET | `/invoice/{userId}` | Get invoice address |
| PUT | `/{addressId}` | Update address |
| PUT | `/{addressId}/set-invoice` | Set as invoice address |
| DELETE | `/{addressId}` | Delete address |

### Files — `/api/files`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/profile-photos/upload/{userId}` | Upload profile photo |
| GET | `/profile-photos/{filename}` | Serve profile photo |

---

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL
- Redis

### Setup

```bash
git clone https://github.com/yusufgokkayy/tendering-java.git
cd tendering-java

# Configure src/main/resources/application.properties
./mvnw spring-boot:run
```

### Key Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tendering_db
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=your_jwt_secret
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

---

## Project Status

Active development — core auction engine, bidding system, wallet, escrow, and auth are complete and deployed.
