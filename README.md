# 🍕 Eatzy - Food Delivery Backend API

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=for-the-badge&logo=springboot)
![MariaDB](https://img.shields.io/badge/MariaDB-11.2-blue?style=for-the-badge&logo=mariadb)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=for-the-badge&logo=jsonwebtokens)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)

**Modern Food Delivery Platform with Advanced Payment Integration**

[Features](#-features) • [Tech Stack](#-tech-stack) • [Quick Start](#-quick-start) • [API Documentation](#-api-documentation)

</div>

---

## 📖 Giới thiệu

**Eatzy Backend** là hệ thống RESTful API hoàn chỉnh cho nền tảng đặt đồ ăn trực tuyến, được xây dựng với Spring Boot 3 và Java 17. Dự án cung cấp đầy đủ tính năng từ quản lý đơn hàng, thanh toán đa kênh, đến phân phối thu nhập tự động cho tài xế và nhà hàng.

### 🎯 Mục tiêu dự án
- ✅ Xây dựng hệ thống backend scalable và maintainable
- ✅ Tích hợp đa phương thức thanh toán (Ví điện tử, COD, VNPay)
- ✅ Quản lý luồng đơn hàng phức tạp với multiple actors (Customer, Restaurant, Driver)
- ✅ Tự động hóa quy trình phân phối thu nhập và hoa hồng
- ✅ Bảo mật cao với JWT authentication và role-based authorization

---

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT Token Authentication** - Access & Refresh token với auto-renewal
- **Email Verification** - OTP 6 số qua email với expiration 15 phút
- **Role-Based Access Control** - ADMIN, CUSTOMER, RESTAURANT_OWNER, DRIVER
- **Permission Management** - Phân quyền chi tiết theo module và action

### 🍔 Core Business Features
- **Restaurant Management** - Quản lý thông tin, menu, món ăn, và danh mục
- **Order Workflow** - Luồng đơn hàng hoàn chỉnh từ tạo đến giao hàng
  - 🔄 Status flow: `PENDING` → `PREPARING`→ `DRIVER_ASSIGNED` → `READY` → `PICKED_UP`→ `ARRIVED` → `DELIVERED`
  - 👨‍🍳 Restaurant accept/reject orders
  - 🚗 Auto driver assignment với rejection tracking
  - 📦 Driver pickup và delivery confirmation
- **Menu & Dishes** - Quản lý món ăn với options (size, topping, etc.)
- **Driver Management** - Profile, availability status, COD limit
- **Ratings & Reviews** - Đánh giá restaurant và driver

### 💳 Payment Integration
- **Multi Payment Methods**
  - 💰 **Wallet** - Ví điện tử nội bộ với transaction history
  - 💵 **COD** - Thanh toán khi nhận hàng (Cash on Delivery)
  - 🏦 **VNPay** - Cổng thanh toán trực tuyến
- **Automatic Fund Distribution**
  - Driver nhận 80% phí giao hàng
  - Restaurant nhận 85% giá trị đơn (trừ 15% hoa hồng)
  - Admin nhận hoa hồng từ cả driver và restaurant
- **Payment Validation** - COD limit check cho driver, wallet balance verification

### 📊 Business Intelligence
- **Order Earnings Summary** - Tổng hợp thu nhập chi tiết mỗi đơn
- **Monthly Revenue Report** - Báo cáo doanh thu theo tháng cho restaurant
- **Wallet Transactions** - Lịch sử giao dịch đầy đủ với trạng thái

### 🛠️ Technical Features
- **Dynamic Client IP Extraction** - Tự động lấy IP từ request (X-Forwarded-For, X-Real-IP)
- **Scheduled Jobs** - Auto cleanup expired VNPay orders (15 minutes)
- **Circular Dependency Resolution** - @Lazy injection pattern
- **File Upload** - Cloudinary integration cho ảnh món ăn và restaurant
- **Email Service** - HTML templates đẹp cho verification và welcome emails
- **Exception Handling** - Global exception handler với custom error responses
- **API Documentation** - OpenAPI 3.0 (Swagger UI)
- **Pagination & Filtering** - Spring Data JPA Specification
- **Audit Trail** - Tracking createdAt, updatedAt cho tất cả entities

---

## 🛠️ Tech Stack

### Backend Framework
- **Java 17** - Modern LTS version với improved performance
- **Spring Boot 3.2** - Latest Spring framework với native support
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM với Hibernate implementation
- **Spring Mail** - Email service integration

### Database & Caching
- **MariaDB 11.2** - Primary database
- **HikariCP** - High-performance connection pooling

### Third-party Integrations
- **VNPay Payment Gateway** - Vietnamese payment platform
- **Cloudinary** - Cloud-based image storage
- **Gmail SMTP** - Email delivery service

### Security & Authentication
- **JWT (jjwt 0.12.3)** - JSON Web Token authentication
- **BCrypt** - Password hashing algorithm

### Development Tools
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **Gradle** - Build automation tool
- **Docker & Docker Compose** - Containerization

### API & Documentation
- **Springdoc OpenAPI** - API documentation generator
- **Swagger UI** - Interactive API explorer

---

## 🚀 Quick Start

### Prerequisites
```bash
# Required
- Java 17 or higher
- MariaDB 11.2 or MySQL 8.0+
- Gradle 8.x (or use included wrapper)

# Optional (for Docker)
- Docker & Docker Compose
```

### 1️⃣ Clone Repository
```bash
git clone https://github.com/hieuduong1810/FoodDelivery_backend.git
cd FoodDelivery_backend
```

### 2️⃣ Configure Environment Variables
Create `.env` file in root directory:
```env
# Database
DB_URL=jdbc:mariadb://localhost:3307/fooddelivery
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_base64_secret_key_here
JWT_ACCESS_TOKEN_VALIDITY=8640000
JWT_REFRESH_TOKEN_VALIDITY=8640000

# File Upload
UPLOAD_FILE_BASE_URI=file:///path/to/upload/

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Email (Gmail)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# VNPay
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:8080/api/v1/payment/vnpay/callback
```

### 3️⃣ Run with Docker (Recommended)
```bash
# Start all services (database + backend)
docker-compose up --build

# Stop services
docker-compose down
```

### 4️⃣ Run Manually
```bash
# Start MariaDB
docker run -d --name mariadb \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=fooddelivery \
  -p 3307:3306 mariadb:11.2

# Build and run Spring Boot
./gradlew bootRun
```

### 5️⃣ Access Application
- **API Base URL**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 📚 API Documentation

### Authentication Endpoints
```http
POST   /api/v1/auth/register       # Đăng ký tài khoản mới
POST   /api/v1/auth/login          # Đăng nhập
POST   /api/v1/auth/refresh        # Refresh access token
GET    /api/v1/auth/account        # Lấy thông tin user hiện tại
POST   /api/v1/email/verify-otp    # Xác thực email với OTP
POST   /api/v1/email/resend        # Gửi lại OTP
```

### Order Management
```http
POST   /api/v1/orders                          # Tạo đơn hàng mới
GET    /api/v1/orders                          # Lấy danh sách đơn hàng (pagination)
GET    /api/v1/orders/{id}                     # Chi tiết đơn hàng
PATCH  /api/v1/orders/{id}/restaurant/accept   # Restaurant chấp nhận đơn
PATCH  /api/v1/orders/{id}/restaurant/reject   # Restaurant từ chối đơn
PATCH  /api/v1/orders/{id}/restaurant/ready    # Đánh dấu món đã sẵn sàng
PATCH  /api/v1/orders/{id}/driver/accept       # Driver chấp nhận giao
PATCH  /api/v1/orders/{id}/driver/reject       # Driver từ chối giao
PATCH  /api/v1/orders/{id}/driver/picked-up    # Driver đã lấy món
PATCH  /api/v1/orders/{id}/driver/delivered    # Driver đã giao xong
```

### Payment Endpoints
```http
POST   /api/v1/payment/wallet              # Thanh toán qua ví
POST   /api/v1/payment/cod/validate        # Validate COD payment
GET    /api/v1/payment/vnpay/callback      # VNPay callback handler
```

### Wallet Management
```http
GET    /api/v1/wallets/{id}                # Chi tiết ví
GET    /api/v1/wallets/user/{userId}       # Ví theo user
POST   /api/v1/wallets/{id}/deposit        # Nạp tiền
POST   /api/v1/wallets/{id}/withdraw       # Rút tiền
GET    /api/v1/wallet-transactions         # Lịch sử giao dịch
```

Xem full API documentation tại **Swagger UI** sau khi start server.

---

## 🗂️ Project Structure

```
eatzy_backend/
├── src/main/java/com/example/FoodDelivery/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfiguration.java
│   │   ├── OpenAPIConfig.java
│   │   └── CloudinaryConfig.java
│   ├── controller/          # REST Controllers
│   │   ├── AuthController.java
│   │   ├── OrderController.java
│   │   ├── PaymentController.java
│   │   └── ...
│   ├── domain/              # JPA Entities
│   │   ├── User.java
│   │   ├── Order.java
│   │   ├── Restaurant.java
│   │   └── ...
│   ├── repository/          # Spring Data JPA Repositories
│   ├── service/             # Business Logic Layer
│   │   ├── OrderService.java
│   │   ├── PaymentService.java
│   │   ├── VNPayService.java
│   │   ├── EmailService.java
│   │   └── ...
│   ├── util/                # Utilities & Helpers
│   │   ├── SecurityUtil.java
│   │   ├── error/           # Exception handling
│   │   └── annotation/      # Custom annotations
│   └── FoodDeliveryApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── static/
├── docker-compose.yml
├── Dockerfile
├── build.gradle.kts
└── README.md
```

---

## 🔧 Key Configurations

### Application Properties
```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DB_URL}
spring.jpa.hibernate.ddl-auto=update

# JWT
foodDelivery.jwt.base64-secret=${JWT_SECRET}
foodDelivery.jwt.access-token-validity-in-seconds=8640000  # 100 days
foodDelivery.jwt.refresh-token-validity-in-seconds=8640000

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.properties.mail.smtp.starttls.enable=true

# Pagination
spring.data.web.pageable.one-indexed-parameters=true
```

---

## 🏗️ Database Schema

### Core Tables
- **users** - User accounts với roles
- **roles** - Role definitions (ADMIN, CUSTOMER, RESTAURANT_OWNER, DRIVER)
- **permissions** - Permission definitions
- **email_verifications** - OTP verification records
- **restaurants** - Restaurant information
- **dishes** - Menu items
- **dish_categories** - Dish categorization
- **menu_options** - Dish options (size, toppings)
- **orders** - Order records
- **order_items** - Order line items
- **order_item_options** - Selected options per item
- **order_driver_rejections** - Driver rejection tracking
- **wallets** - User wallet balances
- **wallet_transactions** - Transaction history
- **order_earnings_summary** - Earnings breakdown per order
- **driver_profiles** - Driver-specific data
- **ratings** - Restaurant & driver ratings

---

## 🔐 Security Implementation

### JWT Authentication Flow
1. User login → Generate access token (100 days) + refresh token
2. Each request includes `Authorization: Bearer <access_token>`
3. Token validates user identity and extracts roles/permissions
4. Refresh token used to get new access token when expired

### Email Verification
1. User registers → Account created with `isActive = false`
2. System generates 6-digit OTP, valid 15 minutes
3. OTP sent via email with HTML template
4. User verifies OTP → Account activated (`isActive = true`)
5. Welcome email sent automatically

### Role-Based Authorization
```java
@PreAuthorize("hasRole('ADMIN')")           // Admin only
@PreAuthorize("hasRole('RESTAURANT_OWNER')") // Restaurant owner only
@PreAuthorize("hasRole('DRIVER')")          // Driver only
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // Multiple roles
```

---

## 💰 Payment Flow

### 1. Wallet Payment
```
User places order → Check wallet balance → 
Deduct from user wallet → Add to admin wallet → 
Order status: PAID
```

### 2. COD (Cash on Delivery)
```
User places order → Validate driver COD limit → 
Driver accepts → Deliver → 
Mark as delivered → Payment status: PAID → Distribute earnings
```

### 3. VNPay Online Payment
```
User places order → Generate VNPay URL → 
User pays on VNPay → VNPay callback → 
Verify signature → Add to admin wallet → 
Order status: PAID (or auto-delete if failed)
```

### Earnings Distribution (on delivery)
```
Driver receives: 80% delivery fee
Restaurant receives: 85% subtotal
Admin receives: 15% subtotal + 20% delivery fee (commission)
```

---

## 🔄 Order State Machine

```
PENDING
   ↓ (Restaurant accept)
PREPARING
   ↓ (Driver assigned)
ASSIGNED
   ↓ (Restaurant ready)
READY
   ↓ (Driver accept)
DRIVER_ASSIGNED
   ↓ (Driver pickup)
PICKED_UP
   ↓ (Driver arrive)
ARRIVED
   ↓ (Driver deliver)
DELIVERED (✓ Auto-distribute earnings)

CANCELLED (✗ Can cancel anytime before PICKED_UP)
REJECTED (✗ Restaurant rejects)
```

---

## 🧪 Testing

### Manual Testing with Swagger UI
1. Start application
2. Navigate to http://localhost:8080/swagger-ui/index.html
3. Test endpoints interactively

### Testing Payment Integration
- **VNPay Sandbox**: Use test card numbers from VNPay documentation
- **COD**: Test with driver accounts having sufficient COD limit
- **Wallet**: Create test users with funded wallets

---

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Failed**
```bash
# Check MariaDB is running
docker ps | grep mariadb

# Verify credentials in .env
DB_URL=jdbc:mariadb://localhost:3307/fooddelivery
```

**2. Email Not Sending**
```bash
# Use Gmail App Password (not regular password)
# Enable "Less secure app access" or use OAuth2
```

**3. VNPay Payment Failed**
```bash
# Verify VNPAY credentials
# Check VNPAY_RETURN_URL is publicly accessible
# Ensure client IP is correctly extracted
```

**4. Circular Dependency Error**
```java
// Fixed with @Lazy annotation
public OrderService(@Lazy PaymentService paymentService) {...}
```

---

## 📝 Development Notes

### Adding New Features
1. Create domain entity in `domain/`
2. Create repository interface in `repository/`
3. Implement service logic in `service/`
4. Add REST controller in `controller/`
5. Configure security rules in `SecurityConfiguration`
6. Test via Swagger UI

### Database Migration
```bash
# DDL auto-update enabled (development only)
spring.jpa.hibernate.ddl-auto=update

# For production, use Flyway or Liquibase
```

---

## 🤝 Contributing

Dự án này phục vụ mục đích học tập. Nếu muốn đóng góp:
1. Fork repository (keep it PRIVATE)
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

---

## 📄 License

⚠️ **Educational Purpose Only** - This project is for learning purposes. Do not use in production without proper security audit and license compliance.

---

## 📧 Contact

For questions or support, please create an issue in the repository.

---

<div align="center">

**Made with ❤️ using Spring Boot**

![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat&logo=mariadb&logoColor=white)

</div>

