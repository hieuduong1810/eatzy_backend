<div align="center">

# 🍕 Eatzy Backend

### A Modern Food Delivery Platform API

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docker.com/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

**Production-grade RESTful API powering the Eatzy food delivery ecosystem — featuring real-time WebSocket communication, multi-channel payments, intelligent driver assignment, and automated CI/CD deployment.**

[Features](#-features) · [Architecture](#-architecture) · [Tech Stack](#-tech-stack) · [Quick Start](#-quick-start) · [API Reference](#-api-reference) · [Deployment](#-deployment)

---

**🌐 Live:** [`api.hoanduong.net`](https://api.hoanduong.net) &nbsp;|&nbsp; **📖 Docs:** [`api.hoanduong.net/swagger-ui`](https://api.hoanduong.net/swagger-ui/index.html)

</div>

---

## 📖 Overview

**Eatzy Backend** is a comprehensive RESTful API server that powers a full-featured food delivery platform. Built with **Spring Boot 3.2.4** and **Java 17**, it serves as the backbone for 4 client applications (Customer, Driver, Restaurant, Admin) with **38 REST controllers**, **44 service classes**, and **30 domain entities**.

The system handles the complete food delivery lifecycle — from browsing and ordering, to real-time tracking and payment settlement — with enterprise-grade security, caching, and automated deployment.

---

## ✨ Features

<table>
<tr>
<td width="50%">

### 🔐 Authentication & Security
- JWT access + refresh token authentication
- OAuth2 Resource Server integration
- Role-Based Access Control (RBAC) with 4 roles
- Dynamic permission system with custom interceptor
- Email verification with 6-digit OTP
- BCrypt password hashing

</td>
<td width="50%">

### 🍔 Order Management
- Complete order lifecycle with state machine pattern
- Multi-actor workflow (Customer → Restaurant → Driver)
- Automatic driver assignment & rejection tracking
- Order cleanup scheduler for expired payments
- Real-time status notifications via WebSocket

</td>
</tr>
<tr>
<td>

### 💳 Payment System
- **Wallet** — In-app digital wallet with transaction history
- **COD** — Cash on Delivery with driver limit validation
- **VNPay** — Online banking gateway with callback verification
- Automatic earnings distribution (Driver 80%, Restaurant 85%)
- Commission tracking for platform revenue

</td>
<td>

### 📡 Real-Time Communication
- **STOMP over WebSocket** with SockJS fallback
- Live order status tracking for all actors
- Real-time driver location broadcasting
- In-app chat (Customer ↔ Driver) per order
- Typing indicators & message history

</td>
</tr>
<tr>
<td>

### 🗺️ Smart Driver Assignment
- Radius-based geospatial search (configurable km)
- **Mapbox Directions API** for real driving distance
- Haversine pre-filtering for performance
- Automatic fallback when API unavailable
- Driver availability & COD limit checks

</td>
<td>

### 💰 Dynamic Pricing
- Distance-based delivery fee calculation
- Weather-aware surge pricing (OpenWeatherMap)
- Demand-based multipliers
- Configurable system parameters

</td>
</tr>
<tr>
<td>

### 📊 Analytics & Reporting
- Monthly revenue reports per restaurant
- Order earnings summary with actor breakdown
- Restaurant performance analytics
- Wallet transaction history & reconciliation
- User scoring system for recommendations

</td>
<td>

### 🛠️ Platform Features
- Cloudinary image upload & management
- Voucher/promotion system with usage constraints
- Favorites & review system with ratings
- Restaurant type categorization
- SEO (Sitemap & Robots controllers)
- OpenAPI/Swagger documentation

</td>
</tr>
</table>

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATIONS                         │
├──────────────┬──────────────┬──────────────┬───────────────────────┤
│  🛒 Customer │  🚗 Driver   │  🍳 Restaurant│  👨‍💼 Admin            │
└──────┬───────┴──────┬───────┴──────┬───────┴───────────┬───────────┘
       │              │              │                   │
       ▼              ▼              ▼                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     🔒 SPRING SECURITY LAYER                        │
│            JWT Auth · OAuth2 · RBAC · Permission Interceptor        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────┐    ┌──────────────────────────────────┐  │
│  │   REST Controllers   │    │    WebSocket Controllers          │  │
│  │   (38 controllers)   │    │    (Chat · Location · Orders)     │  │
│  └──────────┬───────────┘    └──────────────┬───────────────────┘  │
│             │                               │                      │
│  ┌──────────▼───────────────────────────────▼───────────────────┐  │
│  │                    SERVICE LAYER (44 services)                │  │
│  │                                                               │  │
│  │  OrderService · PaymentService · VNPayService · CartService   │  │
│  │  RestaurantService · DriverProfileService · ChatService       │  │
│  │  DynamicPricingService · WeatherService · MapboxService       │  │
│  │  WalletService · VoucherService · ReviewService · ...         │  │
│  └──────────┬───────────────────────────────┬───────────────────┘  │
│             │                               │                      │
│  ┌──────────▼───────────┐    ┌──────────────▼───────────────────┐  │
│  │   JPA Repositories   │    │     External Services             │  │
│  │   (30 entities)      │    │     Cloudinary · VNPay · Mapbox   │  │
│  │                      │    │     Gmail SMTP · OpenWeatherMap   │  │
│  └──────────┬───────────┘    └──────────────────────────────────┘  │
│             │                                                      │
└─────────────┼──────────────────────────────────────────────────────┘
              │
   ┌──────────▼───────────┐    ┌──────────────────────┐
   │     📦 MariaDB       │    │     ⚡ Redis           │
   │   Primary Database   │    │   Cache · Geo · Chat  │
   └──────────────────────┘    └──────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technologies |
|:---|:---|
| **Runtime** | Java 17 (LTS) |
| **Framework** | Spring Boot 3.2.4, Spring Security, Spring Data JPA, Spring WebSocket, Spring Mail |
| **Auth** | OAuth2 Resource Server, JWT (Access + Refresh Tokens) |
| **Database** | MariaDB with Hibernate ORM |
| **Caching** | Redis — Caching, Geospatial Queries, Chat History, Rejection Tracking |
| **Real-Time** | STOMP over WebSocket with SockJS fallback |
| **Payments** | VNPay Gateway, In-app Wallet, COD |
| **Maps** | Mapbox Directions API, Haversine Formula |
| **Weather** | OpenWeatherMap API (for dynamic pricing) |
| **Media** | Cloudinary (image upload & CDN) |
| **Email** | Spring Mail + Gmail SMTP (HTML templates) |
| **API Docs** | Springdoc OpenAPI 3.0, Swagger UI |
| **Build** | Gradle 8.x with Kotlin DSL |
| **Container** | Docker (multi-stage build), Docker Compose |
| **CI/CD** | GitHub Actions → Docker Hub → VPS (SSH deploy) |
| **Code Quality** | Lombok, Spring Validation, Global Exception Handling |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- MariaDB 11.x / MySQL 8.x
- Redis 7.x
- Gradle 8.x (or use included wrapper)
- Docker & Docker Compose *(optional)*

### 1. Clone & Configure

```bash
git clone https://github.com/hieuduong1810/eatzy_backend.git
cd eatzy_backend
```

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Fill in the required environment variables:

```env
# Database
DB_URL=jdbc:mariadb://localhost:3306/fooddelivery
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_base64_secret_key
JWT_ACCESS_TOKEN_VALIDITY=8640000
JWT_REFRESH_TOKEN_VALIDITY=8640000

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Email (Gmail App Password)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# VNPay
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

# Mapbox
MAPBOX_ACCESS_TOKEN=your_mapbox_token

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# OpenWeatherMap
OPENWEATHERMAP_API_KEY=your_api_key
```

### 2. Run with Docker (Recommended)

```bash
docker compose up --build -d
```

### 3. Run Manually

```bash
# Start dependencies (MariaDB + Redis)
docker compose up -d db redis

# Run Spring Boot
./gradlew bootRun
```

### 4. Access

| Service | URL |
|:---|:---|
| API Base | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI Docs | `http://localhost:8080/v3/api-docs` |
| WebSocket | `ws://localhost:8080/ws` |

---

## 📚 API Reference

### 🔐 Authentication

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/auth/register` | Register new account |
| `POST` | `/api/v1/auth/login` | Login & receive JWT tokens |
| `POST` | `/api/v1/auth/refresh` | Refresh access token |
| `GET` | `/api/v1/auth/account` | Get current user info |
| `POST` | `/api/v1/email/verify-otp` | Verify email with OTP |
| `POST` | `/api/v1/email/resend` | Resend verification OTP |

### 🍔 Orders

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/orders` | Create new order |
| `GET` | `/api/v1/orders` | List orders (paginated) |
| `GET` | `/api/v1/orders/{id}` | Get order details |
| `PATCH` | `/api/v1/orders/{id}/restaurant/accept` | Restaurant accepts order |
| `PATCH` | `/api/v1/orders/{id}/restaurant/reject` | Restaurant rejects order |
| `PATCH` | `/api/v1/orders/{id}/restaurant/ready` | Mark as ready for pickup |
| `PATCH` | `/api/v1/orders/{id}/driver/accept` | Driver accepts delivery |
| `PATCH` | `/api/v1/orders/{id}/driver/reject` | Driver rejects delivery |
| `PATCH` | `/api/v1/orders/{id}/driver/picked-up` | Driver picked up order |
| `PATCH` | `/api/v1/orders/{id}/driver/delivered` | Order delivered |

### 💳 Payments

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/payment/wallet` | Pay via wallet |
| `POST` | `/api/v1/payment/cod/validate` | Validate COD payment |
| `GET` | `/api/v1/payment/vnpay/callback` | VNPay callback handler |

### 💰 Wallets

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/v1/wallets/{id}` | Get wallet details |
| `POST` | `/api/v1/wallets/{id}/deposit` | Deposit funds |
| `POST` | `/api/v1/wallets/{id}/withdraw` | Withdraw funds |
| `GET` | `/api/v1/wallet-transactions` | Transaction history |

### 📡 WebSocket

```
Connection:    ws://localhost:8080/ws (SockJS)

Subscribe:
  /topic/restaurant/{id}/orders         → Restaurant receives new orders
  /topic/driver/{id}/orders             → Driver receives assignments
  /topic/customer/{id}/orders           → Customer receives order updates
  /topic/customer/{id}/driver-location  → Customer receives driver location
  /topic/chat/order/{orderId}           → Chat messages for order
  /topic/chat/order/{orderId}/typing    → Typing indicators

Publish:
  /app/chat/{orderId}                   → Send chat message
  /app/typing/{orderId}                 → Send typing indicator
  /app/driver/location/{orderId}        → Send driver location
```

> 📖 **Full API documentation available at [Swagger UI](http://localhost:8080/swagger-ui/index.html) after starting the server.**

---

## 🔄 Order Lifecycle

```
                  ┌──── CANCELLED (anytime before PICKED_UP)
                  │
   PENDING ───────┤
                  │
                  └──── Restaurant Accept
                              │
                        PREPARING
                              │
                        Driver Assigned ──── Driver Reject ──→ Re-assign
                              │
                     DRIVER_ASSIGNED
                              │
                    Restaurant Ready
                              │
                          READY
                              │
                      Driver Pickup
                              │
                        PICKED_UP
                              │
                      Driver Arrive
                              │
                         ARRIVED
                              │
                      Driver Deliver
                              │
                       ✅ DELIVERED
                    (auto-distribute earnings)
```

> Every status change triggers a real-time WebSocket notification to all relevant actors.

---

## 💰 Earnings Distribution

When an order is delivered, earnings are automatically distributed:

| Actor | Share | Calculation |
|:---|:---|:---|
| 🚗 **Driver** | 80% of delivery fee | `deliveryFee × 0.80` |
| 🍳 **Restaurant** | 85% of food subtotal | `subtotal × 0.85` |
| 👨‍💼 **Admin** | 15% food + 20% delivery | `subtotal × 0.15 + deliveryFee × 0.20` |

---

## 📁 Project Structure

```
eatzy_backend/
├── .github/workflows/
│   └── cicd.yml                    # GitHub Actions CI/CD pipeline
├── src/main/java/com/example/FoodDelivery/
│   ├── config/                     # 15 configuration classes
│   │   ├── SecurityConfiguration   # JWT + OAuth2 + CORS
│   │   ├── WebSocketConfig         # STOMP + SockJS setup
│   │   ├── RedisConfiguration      # Cache + Geo + Chat
│   │   ├── PermissionInterceptor   # Dynamic RBAC enforcement
│   │   ├── CloudinaryConfig        # Image upload
│   │   └── OpenAPIConfig           # Swagger documentation
│   ├── controller/                 # 38 REST controllers
│   │   ├── AuthController          # Login, Register, JWT
│   │   ├── OrderController         # Full order lifecycle
│   │   ├── PaymentController       # Wallet, COD, VNPay
│   │   ├── ChatController          # WebSocket chat handler
│   │   ├── DriverLocationController# Real-time location
│   │   └── ...
│   ├── domain/                     # 30 JPA entities + DTOs
│   │   ├── req/                    # Request DTOs
│   │   └── res/                    # Response DTOs
│   ├── repository/                 # Spring Data JPA repos
│   ├── service/                    # 44 business logic services
│   │   ├── OrderService            # Order state machine (80KB)
│   │   ├── RestaurantService       # Restaurant management
│   │   ├── DynamicPricingService   # Weather + demand pricing
│   │   ├── MapboxService           # Route calculation
│   │   ├── RedisGeoService         # Geospatial driver search
│   │   ├── VNPayService            # Payment gateway
│   │   └── ...
│   └── util/                       # Utilities & error handling
├── src/main/resources/
│   ├── application.properties      # App configuration
│   ├── static/                     # WebSocket test clients
│   └── templates/                  # Email HTML templates
├── Dockerfile                      # Multi-stage build
├── docker-compose.yml              # Production deployment
├── build.gradle.kts                # Gradle Kotlin DSL
└── .env.example                    # Environment template
```

---

## 🚢 Deployment

### CI/CD Pipeline (GitHub Actions)

The project uses a fully automated CI/CD pipeline:

```
Push to main → GitHub Actions → Build Docker Image → Push to Docker Hub → SSH Deploy to VPS
```

**Pipeline Steps:**
1. **Checkout** — Pull latest source code
2. **Docker Build** — Multi-stage build (Gradle 8.7 + JDK 17 → JRE 17 runtime)
3. **Docker Push** — Push image to Docker Hub registry
4. **SSH Deploy** — Connect to VPS, pull image, restart container via Docker Compose

### Production Stack

```
┌─────────────────────────────────┐
│          Nginx (Reverse Proxy)  │
│          SSL/TLS Termination    │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │  Docker: eatzy_backend    │  │
│  │  Port 8080                │  │
│  └─────────┬─────────────────┘  │
│            │                    │
│  ┌─────────▼────┐  ┌────────┐  │
│  │   MariaDB    │  │ Redis  │  │
│  └──────────────┘  └────────┘  │
└─────────────────────────────────┘
```

---

## 🔐 Security

| Feature | Implementation |
|:---|:---|
| Authentication | JWT (access + refresh tokens) via OAuth2 Resource Server |
| Authorization | RBAC with 4 roles: `ADMIN`, `CUSTOMER`, `RESTAURANT_OWNER`, `DRIVER` |
| Permissions | Dynamic permission system with `PermissionInterceptor` |
| Password | BCrypt hashing |
| Email Verify | 6-digit OTP with 15-min expiration |
| WebSocket Auth | Custom `WebSocketAuthInterceptor` for secure connections |
| CORS | Configured whitelist for frontend origins |
| API Security | Request validation, global exception handling |

---

## 🧪 Testing

### Swagger UI
Navigate to `http://localhost:8080/swagger-ui/index.html` for interactive API testing.

### WebSocket Test Clients
HTML test clients are available in `src/main/resources/static/`:
- `test-chat.html` — Chat system testing
- `test-chat-authenticated.html` — Authenticated chat testing
- `test-order-status.html` — Order notification testing
- `test-driver-location.html` — Driver tracking testing

```bash
# Serve test files via Spring Boot static resources
# Access at http://localhost:8080/test-chat.html
```

---

## 📄 License

⚠️ **Educational Purpose** — This project is built for learning and portfolio demonstration.

---

<div align="center">

**Built with ❤️ by Hoan Duong**

[![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://docker.com/)

</div>
