# 🍕 Eatzy - Food Delivery Backend APII

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
- **WebSocket Real-time Communication** - STOMP protocol cho real-time updates
  - 📡 **Order Status Notifications** - Thông báo real-time cho Customer, Restaurant, Driver khi đơn hàng thay đổi trạng thái
  - 💬 **Chat System** - Chat trực tiếp giữa Driver và Customer cho mỗi đơn hàng
  - 🔔 **Typing Indicators** - Hiển thị khi người dùng đang nhập tin nhắn
  - 📍 **Driver Location Tracking** - Theo dõi vị trí tài xế real-time thông qua WebSocket
    - Driver gửi location mỗi 5 giây qua `/app/driver/location/{orderId}`
    - Customer nhận location qua subscription `/topic/customer/{customerId}/driver-location`
    - Tự động cập nhật vị trí vào database (driver_profiles table)
- **Smart Driver Assignment** - Hệ thống tìm tài xế thông minh
  - 🎯 **Radius-based Search** - Chỉ tìm tài xế trong phạm vi bán kính cấu hình được (DRIVER_SEARCH_RADIUS_KM)
  - 🗺️ **Mapbox Integration** - Sử dụng Mapbox Directions API để tính khoảng cách đường đi thực tế
  - 🚗 **Real Driving Distance** - Ưu tiên tài xế có quãng đường lái xe ngắn nhất (không phải đường chim bay)
  - 🔄 **Haversine Pre-filter** - Lọc nhanh bằng Haversine formula trước khi gọi Mapbox API
  - ⚡ **Fallback Mechanism** - Tự động fallback nếu Mapbox API fails
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
- **Spring WebSocket** - Real-time bidirectional communication với STOMP protocol

### Database & Caching
- **MariaDB 11.2** - Primary database
- **HikariCP** - High-performance connection pooling

### Third-party Integrations
- **VNPay Payment Gateway** - Vietnamese payment platform
- **Cloudinary** - Cloud-based image storage
- **Gmail SMTP** - Email delivery service
- **Mapbox Directions API** - Real-time routing và distance calculation

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

# Mapbox
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoiZHVvbmdoaWV1MTgxMCIsImEiOiJjbWoyZ2NsdjIwZ24yM2VvanAyYWttNzhqIn0.SIACCMIF1zU4tLwz68MXTA
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

### WebSocket Endpoints
```http
# Connection Endpoint
WS     /ws                                  # WebSocket connection với SockJS fallback

# Subscribe Destinations (Client → Server)
SUBSCRIBE /topic/restaurant/{restaurantId}/orders    # Nhà hàng nhận thông báo đơn mới
SUBSCRIBE /topic/driver/{driverId}/orders            # Tài xế nhận thông báo được assign
SUBSCRIBE /topic/customer/{customerId}/orders        # Khách hàng nhận cập nhật đơn hàng
SUBSCRIBE /topic/chat/order/{orderId}                # Chat theo từng đơn hàng
SUBSCRIBE /topic/chat/order/{orderId}/typing         # Typing indicator

# Send Destinations (Client → Server)
SEND   /app/chat/{orderId}                  # Gửi tin nhắn chat
SEND   /app/typing/{orderId}                # Gửi typing indicator
```

**WebSocket Usage Example:**
```javascript
// Connect với SockJS + Stomp.js
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  // Subscribe nhận thông báo đơn hàng
  stompClient.subscribe('/topic/customer/4/orders', function(notification) {
    const orderUpdate = JSON.parse(notification.body);
    console.log('Order status:', orderUpdate.type);
  });
  
  // Subscribe chat cho đơn hàng #30
  stompClient.subscribe('/topic/chat/order/30', function(message) {
    const chatMsg = JSON.parse(message.body);
    displayMessage(chatMsg);
  });
  
  // Gửi tin nhắn chat
  const chatMessage = {
    orderId: 30,
    senderId: 4,
    senderName: "Customer 4",
    senderType: "CUSTOMER",
    message: "Hello driver!",
    messageType: "TEXT"
  };
  stompClient.send('/app/chat/30', {}, JSON.stringify(chatMessage));
});
```

Xem full API documentation tại **Swagger UI** sau khi start server.

---

## 🗂️ Project Structure

```
eatzy_backend/
├── src/main/java/com/example/FoodDelivery/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfiguration.java
│   │   ├── WebSocketConfig.java
│   │   ├── OpenAPIConfig.java
│   │   └── CloudinaryConfig.java
│   ├── controller/          # REST Controllers
│   │   ├── AuthController.java
│   │   ├── OrderController.java
│   │   ├── PaymentController.java
│   │   ├── ChatController.java      # WebSocket chat handler
│   │   └── ...
│   ├── domain/              # JPA Entities
│   │   ├── User.java
│   │   ├── Order.java
│   │   ├── Restaurant.java
│   │   ├── res/websocket/   # WebSocket DTOs
│   │   │   ├── OrderNotification.java
│   │   │   └── ChatMessage.java
│   │   └── ...
│   ├── repository/          # Spring Data JPA Repositories
│   ├── service/             # Business Logic Layer
│   │   ├── OrderService.java
│   │   ├── PaymentService.java
│   │   ├── VNPayService.java
│   │   ├── EmailService.java
│   │   ├── WebSocketService.java    # WebSocket notification service
│   │   └── ...
│   ├── util/                # Utilities & Helpers
│   │   ├── SecurityUtil.java
│   │   ├── error/           # Exception handling
│   │   └── annotation/      # Custom annotations
│   └── FoodDeliveryApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── static/
├── test-chat.html           # WebSocket chat test client
├── test-websocket.html      # WebSocket notification test client
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
   ↓ (Restaurant accept) → 🔔 Notify Customer
PREPARING
   ↓ (Driver assigned) → 🔔 Notify Driver & Customer
ASSIGNED
   ↓ (Restaurant ready) → 🔔 Notify Driver & Customer
READY
   ↓ (Driver accept) → 🔔 Notify Customer & Restaurant
DRIVER_ASSIGNED
   ↓ (Driver pickup) → 🔔 Notify Customer
PICKED_UP
   ↓ (Driver arrive) → 🔔 Notify Customer
ARRIVED
   ↓ (Driver deliver) → 🔔 Notify Customer & Restaurant
DELIVERED (✓ Auto-distribute earnings)

CANCELLED (✗ Can cancel anytime before PICKED_UP)
REJECTED (✗ Restaurant rejects)
```

**Real-time Notifications:**
- Mỗi lần đơn hàng thay đổi trạng thái, WebSocket tự động gửi notification
- Restaurant nhận thông báo qua `/topic/restaurant/{id}/orders`
- Driver nhận thông báo qua `/topic/driver/{id}/orders`
- Customer nhận thông báo qua `/topic/customer/{id}/orders`

---

## 💬 WebSocket Chat System

### Chat Flow
```
Customer ←→ WebSocket Server ←→ Driver
     ↓                              ↓
Subscribe /topic/chat/order/30
     ↓                              ↓
Send message to /app/chat/30
     ↓                              ↓
Both receive via /topic/chat/order/30
```

### Chat Features
- **Per-Order Chat Room** - Mỗi đơn hàng có 1 chat room riêng
- **Real-time Messaging** - Tin nhắn hiển thị ngay lập tức
- **Typing Indicator** - Hiển thị khi người khác đang nhập
- **Message Types** - TEXT, IMAGE, LOCATION (extensible)
- **Broadcast Chat** - Sử dụng `/topic` cho simple implementation

### Integration Points
```java
// OrderService tự động gửi notification khi:
- createOrder() → notifyRestaurantNewOrder()
- acceptOrder() → notifyCustomerOrderUpdate()
- assignDriver() → notifyDriverOrderAssigned()
- acceptOrderByDriver() → broadcastOrderStatusChange()
- markOrderAsReady() → notifyCustomerOrderUpdate()
- markOrderAsPickedUp() → (implicit broadcast)
- markOrderAsArrived() → notifyCustomerOrderUpdate()
- markOrderAsDelivered() → broadcastOrderStatusChange()
- cancelOrder() → broadcastOrderStatusChange()
```

---

## 🧪 Testing

### Manual Testing with Swagger UI
1. Start application
2. Navigate to http://localhost:8080/swagger-ui/index.html
3. Test endpoints interactively

### Testing WebSocket Features

**1. Order Notifications**
- Open `test-websocket.html` in browser (via Live Server or http-server)
- Enter Restaurant ID / Driver ID / Customer ID
- Subscribe to appropriate topic
- Create/update orders via API
- See real-time notifications

**2. Chat System**
- Open `test-chat.html` in browser
- Enter Order ID, Customer ID, Driver ID
- Click "Connect Both" to simulate both users
- Send messages between customer and driver
- Test typing indicators

**WebSocket Testing Tools:**
```bash
# Serve HTML test files
npx http-server -p 3000 -c-1

# Or use VS Code Live Server extension
# Right-click test-chat.html → Open with Live Server
```

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

**5. WebSocket Connection Failed**
```bash
# Ensure WebSocket endpoint is accessible
curl http://localhost:8080/ws/info

# Check CORS settings in WebSocketConfig
# Test HTML files must be served via HTTP (not file://)
# Use Live Server or http-server

# Debug client connection
# Check browser console for WebSocket errors
# Verify subscribe destinations match server topics
```

**6. Chat Messages Not Displaying**
```bash
# Common issue: Path mismatch
# Server sends to: /topic/chat/order/{orderId}
# Client must subscribe to: /topic/chat/order/{orderId}

# Check console logs:
# "Broadcasted chat message to topic..." → Server OK
# "Customer received message..." → Client OK

# If server logs show message sent but client doesn't receive:
# - Verify subscription path matches exactly
# - Check order ID is correct
# - Restart both server and client
```

**7. Driver Location Tracking Not Working**
```bash
# WebSocket paths must match:
# Driver sends to: /app/driver/location/{orderId}
# Customer subscribes to: /topic/customer/{customerId}/driver-location

# Common issues:
# - Order ID mismatch
# - Customer ID incorrect
# - Driver not assigned to order
# - WebSocket connection dropped

# Debug:
# - Check browser console for WebSocket errors
# - Verify driver is assigned to order (order.driver != null)
# - Test with driver-location-test.html file
# - Check database: driver_profiles.current_latitude/longitude updated

# Mapbox API errors:
# - Verify token is valid: pk.eyJ1...
# - Check API rate limits (free tier: 100,000/month)
# - Ensure coordinates are valid (latitude: -90 to 90, longitude: -180 to 180)
```

**8. Driver Assignment Fails or Wrong Driver Selected**
```bash
# Check system configuration
SELECT * FROM system_configuration WHERE config_key = 'DRIVER_SEARCH_RADIUS_KM';
# Default: 10 km

# Verify driver profiles have coordinates
SELECT id, user_id, status, current_latitude, current_longitude 
FROM driver_profiles 
WHERE status IN ('ONLINE', 'AVAILABLE');

# Check Mapbox service logs
# Should see: "Updated driver {id} location in database"
# And: "Sent driver location to customer {id}"

# If no driver found:
# - Increase search radius in system_configuration
# - Check driver status (must be ONLINE or AVAILABLE)
# - Verify restaurant has coordinates set
# - Check wallet balance >= 0
# - For COD orders: verify driver cod_limit >= order amount

# Mapbox API fallback:
# If Mapbox API fails, system will skip that driver
# Check logs: "Failed to get driving distance from Mapbox for driver {id}"
# Increase candidate count or check Mapbox API status
```

---

## 🗺️ Mapbox Integration

### Setup
```java
// Token already configured in MapboxService.java
private static final String MAPBOX_TOKEN = "pk.eyJ1IjoiZHVvbmdoaWV1MTgxMCIsImEiOiJjbWoyZ2NsdjIwZ24yM2VvanAyYWttNzhqIn0.SIACCMIF1zU4tLwz68MXTA";

// API Endpoint
https://api.mapbox.com/directions/v5/mapbox/driving/{lng},{lat};{lng},{lat}
```

### How It Works
1. **Pre-filter with Haversine** - Lọc tài xế trong bán kính bằng công thức Haversine (đường chim bay)
2. **Calculate Real Distance** - Gọi Mapbox API cho từng tài xế để tính khoảng cách đường đi thực tế
3. **Sort by Distance** - Chọn tài xế có quãng đường lái xe ngắn nhất
4. **Assign Driver** - Gán tài xế vào đơn hàng

### API Methods
```java
// MapboxService.java
BigDecimal getDrivingDistance(lat1, lng1, lat2, lng2)  // Returns km
BigDecimal getDrivingDuration(lat1, lng1, lat2, lng2)  // Returns minutes
```

### Rate Limits
- Free tier: 100,000 requests/month
- ~3,300 requests/day
- Monitor usage in [Mapbox Dashboard](https://account.mapbox.com/)

---

## 📍 Driver Location Tracking

### WebSocket Endpoints
```javascript
// Driver sends location updates
SEND /app/driver/location/{orderId}
Body: { "latitude": 10.762622, "longitude": 106.660172 }

// Customer receives location updates
SUBSCRIBE /topic/customer/{customerId}/driver-location
Receives: { "latitude": 10.762622, "longitude": 106.660172, "timestamp": "2024-..." }
```

### Testing
Use provided HTML test file:
```bash
# Open in browser (must use http-server, not file://)
npx http-server
# Navigate to http://localhost:8080/driver-location-test.html
```

### Implementation Details
- Driver sends location every 5 seconds (configurable)
- Location stored in `driver_profiles` table (`current_latitude`, `current_longitude`)
- Real-time broadcast to customer via WebSocket
- Coordinates validated before saving

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

