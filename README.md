[README_BACKEND.md](https://github.com/user-attachments/files/26132680/README_BACKEND.md)
# 🛒 GoToKart — Backend

<div align="center">

![GoToKart](https://img.shields.io/badge/GoToKart-Ecommerce_API-f5a623?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**A full-featured REST API for an e-commerce shopping cart application**

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)

---

## 🌟 Overview

GoToKart Backend is a production-ready Spring Boot REST API that powers an e-commerce platform. It supports user registration, product management, cart operations, and order placement — all backed by MySQL.

**Key Highlights:**
- RESTful API design with full CRUD operations
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
| Spring Data JPA | 3.4.3 | ORM & Database Layer |
| MySQL | 8.0 | Primary Database |
| Hibernate | 6.6.x | JPA Implementation |
| Lombok | 1.18.36 | Code Generation |
| Maven | 3.9.x | Build Tool |

---

## 📁 Project Structure

```
src/main/java/com/gotokart/
│
├── 📂 model/                    # JPA Entity Classes
│   ├── User.java                # User account entity
│   ├── Product.java             # Product catalog entity
│   ├── CartItem.java            # Shopping cart item entity
│   ├── Order.java               # Order entity
│   └── OrderItem.java           # Order line item (snapshot)
│
├── 📂 repository/               # Spring Data JPA Repositories
│   ├── UserRepository.java      # User CRUD + findByEmail
│   ├── ProductRepository.java   # Product CRUD + findByName
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
│   ├── UserController.java      # /api/users/**
│   ├── ProductController.java   # /api/products/**
│   ├── CartController.java      # /api/cart/**
│   └── OrderController.java     # /api/orders/**
│
└── GotokartApplication.java     # Spring Boot Entry Point

src/main/resources/
└── application.properties       # App configuration
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
CREATE DATABASE gotokartDB;
```

### 3. Configure application.properties

```properties
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/gotokartDB
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Jackson
spring.jackson.serialization.fail-on-empty-beans=false
spring.jackson.default-property-inclusion=non_null
```

### 4. Set Java 21 (Windows)

```powershell
$env:JAVA_HOME = "C:\Users\<you>\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
```

### 5. Run the Application

```bash
mvn clean spring-boot:run
```

The API will be live at: **`http://localhost:8080`**

---

## 📡 API Reference

### 👤 Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users/register` | Register a new user |
| `GET` | `/api/users` | Get all users |
| `GET` | `/api/users/{id}` | Get user by ID |

**Register User — Request Body:**
```json
{
  "name": "Ankit Pandey",
  "email": "ankit@gotokart.com",
  "password": "secret123"
}
```

---

### 📦 Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | Get all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/products` | Add product (merges if name exists) |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Delete product |

**Add Product — Request Body:**
```json
{
  "name": "iPhone 15",
  "description": "Apple iPhone 15 128GB",
  "price": 79999.0,
  "stock": 50
}
```

> 💡 If a product with the same name already exists, its stock is increased automatically.

---

### 🛒 Cart

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/cart/{userId}` | Get cart items for user |
| `POST` | `/api/cart/{userId}/add?productId=&quantity=` | Add item to cart |
| `DELETE` | `/api/cart/{userId}/remove?productId=` | Remove item from cart |

---

### 📋 Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/orders/{userId}/place` | Place order (clears cart, reduces stock) |
| `GET` | `/api/orders/{userId}` | Get all orders for user |

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
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│    users    │     │  cart_items  │     │  products   │
├─────────────┤     ├──────────────┤     ├─────────────┤
│ id (PK)     │◄────│ user_id (FK) │     │ id (PK)     │
│ name        │     │ product_id──►│────►│ name        │
│ email       │     │ quantity     │     │ description │
│ password    │     │ id (PK)      │     │ price       │
└─────────────┘     └──────────────┘     │ stock       │
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

---

## 🔧 Common Issues

| Error | Fix |
|-------|-----|
| `Lombok TypeTag UNKNOWN` | Maven is using Java 25. Set JAVA_HOME to Java 21 |
| `Field cart_item_id has no default` | Drop and recreate `order_items` table |
| `405 Method Not Allowed` | Add `@CrossOrigin` with DELETE method to controller |
| `deleteByUserIdAndProductId` silently fails | Add `@Modifying` + `@Transactional` + use `@Query` |

---

<div align="center">

Built with ❤️ using **Spring Boot** & **MySQL**

**GoToKart** — Where every cart tells a story ⚡

</div>
