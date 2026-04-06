# 🛒 GoToKart — Backend

<div align="center">

![GoToKart](https://img.shields.io/badge/GoToKart-Ecommerce_API-f5a623?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

**A full-featured REST API for an e-commerce shopping cart application**

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Authentication](#-authentication)
- [API Reference](#-api-reference)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)

---

## 🌟 Overview

GoToKart Backend is a production-ready Spring Boot REST API that powers an e-commerce platform. It supports user registration, JWT-based authentication, role-based authorization, product management, cart operations, and order placement — all backed by MySQL.

**Key Highlights:**
- RESTful API design with full CRUD operations
- JWT-based authentication with BCrypt password hashing
- Role-based access control (ADMIN / USER) via Spring Security
- 102 products seeded automatically on first startup across 11 categories
- Admin user auto-created on every startup if not found
- MySQL database with JPA/Hibernate ORM
- Smart product merging — adding duplicate products increases stock
- Transactional order placement with automatic stock reduction
- Cart management with quantity control
- Order history with item-level details

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 (Temurin) | Programming Language |
| Spring Boot | 3.4.3 | Application Framework |
| Spring Security | 3.4.3 | Auth & Authorization |
| Spring Data JPA | 3.4.3 | ORM & Database Layer |
| MySQL | 8.0 | Primary Database |
| JJWT | 0.11.5 | JWT token generation & validation |
| BCryptPasswordEncoder | — | Password hashing |
| Hibernate | 6.6.x | JPA Implementation |
| Lombok | 1.18.36 | Code Generation |
| Maven | 3.9.x | Build Tool |

---

## 📁 Project Structure

```
src/main/java/com/gotokart/
│
├── 📂 config/                   # Security, JWT, and Initialization
│   ├── SecurityConfig.java      # Spring Security + CORS + JWT filter chain
│   ├── JwtUtil.java             # JWT generate / validate / extract claims
│   ├── JwtAuthFilter.java       # JWT request filter (sets SecurityContext)
│   └── DataInitializer.java     # Seeds 102 products + admin user on startup
│
├── 📂 dto/                      # Data Transfer Objects
│   └── AuthResponse.java        # Login/register response (token + user info)
│
├── 📂 model/                    # JPA Entity Classes
│   ├── User.java                # User account entity (name, email, password, role)
│   ├── Product.java             # Product catalog entity (with category)
│   ├── CartItem.java            # Shopping cart item entity
│   ├── Order.java               # Order entity
│   └── OrderItem.java           # Order line item (snapshot)
│
├── 📂 repository/               # Spring Data JPA Repositories
│   ├── UserRepository.java      # User CRUD + findByEmail
│   ├── ProductRepository.java   # Product CRUD + findByName
│   ├── CategoryRepository.java  # Category CRUD
│   ├── CartItemRepository.java  # Cart operations with @Query
│   ├── OrderRepository.java     # Order fetch by userId
│   └── OrderItemRepository.java # Order item persistence
│
├── 📂 service/                  # Business Logic Layer
│   ├── UserService.java         # User registration & lookup
│   ├── ProductService.java      # Product management + merge
│   ├── CartService.java         # Cart add/remove/fetch
│   └── OrderService.java        # Order placement + stock update
│
├── 📂 controller/               # REST API Controllers
│   ├── AuthController.java      # /api/auth/** (login, register)
│   ├── UserController.java      # /api/users/** (list, get, /me)
│   ├── ProductController.java   # /api/products/** (ADMIN write)
│   ├── CartController.java      # /api/cart/**
│   └── OrderController.java     # /api/orders/**
│
└── GotokartApplication.java     # Spring Boot Entry Point

src/main/resources/
└── application.properties       # App configuration (DB, JWT secret, port)
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 21 (Eclipse Temurin recommended) |
| Maven | 3.9+ |
| MySQL | 8.0+ |

### 1. Clone the Repository

```bash
git clone https://github.com/gotokart/backend.git
cd backend
```

### 2. Set Up MySQL Database

```sql
CREATE DATABASE gotokart;
```

### 3. Configure application.properties

```properties
server.port=8080

# MySQL (local dev)
spring.datasource.url=jdbc:mysql://localhost:3306/gotokart?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Root@1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Jackson
spring.jackson.serialization.fail-on-empty-beans=false
spring.jackson.default-property-inclusion=non_null

# JWT
jwt.secret=GoToKartSuperSecretJwtKey2026!!VeryLongAndSecure@#$%
jwt.expiry-ms=86400000
```

### 4. Run the Application

```bash
mvn clean spring-boot:run
```

The API will be live at: **`http://localhost:8080`**

On first startup, the app automatically seeds:
- **Admin user** — `admin@gotokart.com` / `admin123`
- **102 products** across 11 categories

---

## 🔐 Authentication

GoToKart uses **JWT (JSON Web Tokens)** for authentication. Passwords are hashed with **BCrypt**.

### Default Admin Account

| Field | Value |
|-------|-------|
| Email | `admin@gotokart.com` |
| Password | `admin123` |
| Role | `ADMIN` |

The admin account is created automatically on startup if it does not already exist.

### How it Works

1. Client sends credentials to `POST /api/auth/login`
2. Backend validates email/password (BCrypt compare)
3. Backend returns a signed JWT containing `userId`, `email`, and `role`
4. Client stores the JWT in `localStorage` and sends it as `Authorization: Bearer <token>` on every subsequent request
5. `JwtAuthFilter` intercepts each request, validates the token, and sets the Spring Security context
6. Admin-only endpoints are protected with `@PreAuthorize("hasRole('ADMIN')")`

### Auth Endpoints

| Method | Path | Auth Required | Description |
|--------|------|:---:|-------------|
| `POST` | `/api/auth/login` | No | Login, returns JWT |
| `POST` | `/api/auth/register` | No | Register new user, returns JWT |
| `GET` | `/api/users/me` | Yes (JWT) | Get current user profile |

**Login Request:**
```json
POST /api/auth/login
{
  "email": "admin@gotokart.com",
  "password": "admin123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "name": "Admin",
  "email": "admin@gotokart.com",
  "role": "ADMIN"
}
```

Use the returned `token` in all subsequent protected requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 📡 API Reference

### 👤 Users

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `POST` | `/api/auth/login` | No | Login — returns JWT |
| `POST` | `/api/auth/register` | No | Register — returns JWT |
| `GET` | `/api/users/me` | JWT | Get own profile |
| `GET` | `/api/users` | No | List all users |
| `GET` | `/api/users/{id}` | No | Get user by ID |

---

### 📦 Products

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `GET` | `/api/products` | No | Get all products |
| `GET` | `/api/products/{id}` | No | Get product by ID |
| `POST` | `/api/products` | ADMIN JWT | Add product (merges if name exists) |
| `PUT` | `/api/products/{id}` | ADMIN JWT | Update product |
| `DELETE` | `/api/products/{id}` | ADMIN JWT | Delete product |

**Add Product — Request Body:**
```json
{
  "name": "iPhone 15",
  "description": "Apple iPhone 15 128GB",
  "price": 79999.0,
  "stock": 50,
  "category": { "id": 1 }
}
```

> If a product with the same name already exists, its stock is increased automatically.

---

### 🛒 Cart

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `GET` | `/api/cart/{userId}` | No | Get cart items for user |
| `POST` | `/api/cart/{userId}/add?productId=&quantity=` | No | Add item to cart |
| `DELETE` | `/api/cart/{userId}/remove?productId=` | No | Remove item from cart |

---

### 📋 Orders

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `POST` | `/api/orders/{userId}/place` | No | Place order (clears cart, reduces stock) |
| `GET` | `/api/orders/{userId}` | No | Get all orders for user |

**Place Order Response:**
```json
{
  "id": 1,
  "status": "PLACED",
  "totalAmount": 84999.0,
  "createdAt": "2026-03-19T10:30:00",
  "items": [
    {
      "productName": "iPhone 15",
      "productPrice": 79999.0,
      "quantity": 1,
      "subtotal": 79999.0
    }
  ]
}
```

---

## 🗄 Database Schema

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌────────────┐
│    users    │     │  cart_items  │     │  products   │     │ categories │
├─────────────┤     ├──────────────┤     ├─────────────┤     ├────────────┤
│ id (PK)     │◄────│ user_id (FK) │     │ id (PK)     │◄────│ id (PK)    │
│ name        │     │ product_id──►│────►│ name        │     │ name       │
│ email       │     │ quantity     │     │ description │     └────────────┘
│ password    │     │ id (PK)      │     │ price       │
│ role        │     └──────────────┘     │ stock       │
└─────────────┘                          │ category_id►│
                                         └─────────────┘
┌─────────────┐     ┌──────────────┐
│   orders    │     │ order_items  │
├─────────────┤     ├──────────────┤
│ id (PK)     │◄────│ order_id(FK) │
│ user_id(FK) │     │ product_name │
│ total_amount│     │ product_price│
│ status      │     │ quantity     │
│ created_at  │     │ subtotal     │
└─────────────┘     └──────────────┘
```

---

## ⚙️ Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-create/update tables |
| `spring.jpa.show-sql` | `true` | Log SQL queries |
| `spring.datasource.url` | — | MySQL connection URL |
| `jwt.secret` | — | HMAC-SHA signing key for JWT |
| `jwt.expiry-ms` | `86400000` | Token TTL in ms (default: 24 h) |

---

## 🌱 Data Seeding

`DataInitializer` runs on every startup:

- **Admin user** is created if `admin@gotokart.com` does not exist
- **102 products** across 11 categories are seeded if the products table is empty

To re-seed products from scratch:
```sql
DELETE FROM order_items;
DELETE FROM cart_items;
DELETE FROM products;
DELETE FROM categories;
```
Then restart the backend.

To reset all users (and re-create the admin with a hashed password):
```sql
DELETE FROM users;
```
Then restart.

---

## 🔧 Common Issues

| Error | Fix |
|-------|-----|
| `403 Forbidden on POST /api/products` | Make sure you are logged in as ADMIN and sending `Authorization: Bearer <token>` header |
| `Admin user not found` | Delete old users (`DELETE FROM users;`) and restart — admin is re-created with hashed password |
| `Lombok TypeTag UNKNOWN` | Maven is using wrong Java version — set `JAVA_HOME` to Java 21 |
| `Field cart_item_id has no default` | Drop and recreate `order_items` table |
| `405 Method Not Allowed` | Add `@CrossOrigin` with the required method to the controller |
| `deleteByUserIdAndProductId` silently fails | Add `@Modifying` + `@Transactional` + use `@Query` |

---

<div align="center">

Built with ❤️ using **Spring Boot**, **Spring Security**, and **MySQL**

**GoToKart** — Where every cart tells a story ⚡

</div>
