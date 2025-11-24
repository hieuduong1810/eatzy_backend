# Food Delivery Backend - Environment Variables Setup

## Cách cấu hình biến môi trường

### 1. Tạo file .env
Copy file `.env.example` thành `.env`:
```bash
cp .env.example .env
```

### 2. Cập nhật thông tin trong .env
Mở file `.env` và cập nhật các giá trị theo môi trường của bạn:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/fooddelivery
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_secret_key_here
JWT_ACCESS_TOKEN_VALIDITY=8640000
JWT_REFRESH_TOKEN_VALIDITY=8640000

# File Upload Configuration
UPLOAD_FILE_BASE_URI=file:///C:/Food_Delivery/upload/

# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_BOT_ENABLED=true

# Email Configuration (Gmail SMTP)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# Application Base URL
APP_BASE_URL=http://localhost:8080
```

### 3. Các biến môi trường quan trọng

#### Database
- `DB_URL`: URL kết nối MySQL
- `DB_USERNAME`: Username database
- `DB_PASSWORD`: Password database

#### JWT
- `JWT_SECRET`: Secret key để mã hóa JWT token
- `JWT_ACCESS_TOKEN_VALIDITY`: Thời gian hết hạn access token (giây)
- `JWT_REFRESH_TOKEN_VALIDITY`: Thời gian hết hạn refresh token (giây)

#### Email
- `MAIL_USERNAME`: Gmail account
- `MAIL_PASSWORD`: Gmail App Password (không phải password thông thường)

#### Telegram Bot
- `TELEGRAM_BOT_TOKEN`: Token từ BotFather
- `TELEGRAM_BOT_USERNAME`: Username của bot
- `TELEGRAM_BOT_ENABLED`: Bật/tắt tính năng Telegram

### 4. Lưu ý bảo mật
- ⚠️ **KHÔNG** commit file `.env` lên Git
- File `.env` đã được thêm vào `.gitignore`
- Chỉ commit file `.env.example` với giá trị mẫu
- Mỗi môi trường (dev, staging, production) nên có file `.env` riêng

### 5. Run application
Sau khi cấu hình `.env`, chạy app như bình thường:
```bash
./gradlew bootRun
```

Spring Dotenv sẽ tự động load biến môi trường từ file `.env`.
