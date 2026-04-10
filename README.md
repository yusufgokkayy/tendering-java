# Teklify — Electronic Tendering Platform

A backend service for real-time electronic tendering, built with Java and Spring Boot. Features live bidding via WebSockets, secure authentication, and a robust relational data model.

---

## What It Does

Teklify enables organizations to create and manage tender processes digitally. Bidders participate in real-time auctions through a WebSocket connection, with the backend handling authentication, authorization, and bid persistence.

**Key technical highlights:**
- WebSocket architecture load-tested to **500 concurrent connections** with low latency
- JWT-based authentication with **Role-Based Access Control (RBAC)**
- Relational data model with complex associations (One-to-Many, Many-to-Many)
- Deployed to a remote Linux server

> **Background:** MVP was initially shipped with Node.js and Express to validate the concept quickly, then fully rebuilt with Java and Spring Boot for scalability and long-term maintainability.

---

## Architecture

```
Client (REST / WebSocket)
          │
          ▼
   Spring Boot Backend
   ├── Auth Layer (JWT + RBAC)
   ├── REST API (Tender & Bid Management)
   ├── WebSocket Handler (Real-time Bidding)
   └── Global Exception Handler
          │
          ▼
     PostgreSQL
   (Spring Data JPA / Hibernate)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Real-time | WebSockets |
| Auth | JWT, RBAC |
| Deployment | Linux Server |

---

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL

### Setup

```bash
# Clone the repository
git clone https://github.com/your-username/teklify.git
cd teklify

# Configure environment variables
cp .env.example .env
# Fill in your DB credentials and JWT secret

# Run
./mvnw spring-boot:run
```

---

## Environment Variables

```env
DB_URL=jdbc:postgresql://localhost:5432/teklify
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400000
```

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT |
| GET | `/tenders` | List all active tenders |
| POST | `/tenders` | Create a new tender |
| POST | `/tenders/{id}/bid` | Place a bid |
| WS | `/ws/tender/{id}` | Real-time bidding stream |

> Endpoints are role-protected. Admin routes require the `ADMIN` role.

---

## Project Status

Active development — core backend is production-ready and deployed.

