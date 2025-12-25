# 🔄 Migration: OrderDriverRejection từ Database sang Redis

## ✅ Đã hoàn thành:

### 1. **Xóa Entity và Repository**
- ❌ Deleted: `OrderDriverRejection.java` entity
- ❌ Deleted: `OrderDriverRejectionRepository.java`
- ✅ Removed imports from `OrderService.java`

### 2. **Chuyển sang Redis**
- ✅ Added `RedisTemplate<String, Object>` dependency to `OrderService`
- ✅ Modified `rejectOrderByDriver()` method to use Redis SET
- ✅ Redis Key Pattern: `order:{orderId}:rejected_drivers`
- ✅ Data Type: SET (lưu driver IDs)
- ✅ TTL: 24 hours (tự động cleanup)

---

## 🗄️ Database Cleanup (CẦN THỰC HIỆN):

### **Bước 1: Xóa table trong database**

```sql
-- Backup data trước khi xóa (optional)
CREATE TABLE order_driver_rejections_backup AS 
SELECT * FROM order_driver_rejections;

-- Drop foreign keys (nếu có)
ALTER TABLE order_driver_rejections 
DROP FOREIGN KEY order_driver_rejections_ibfk_1;

ALTER TABLE order_driver_rejections 
DROP FOREIGN KEY order_driver_rejections_ibfk_2;

-- Xóa table
DROP TABLE IF EXISTS order_driver_rejections;
```

### **Bước 2: Verify**

```sql
-- Kiểm tra table đã bị xóa
SHOW TABLES LIKE 'order_driver_rejections';
-- Kết quả: Empty set (0.00 sec)
```

---

## 📊 Cấu trúc Redis:

### **Key Pattern:**
```
order:{orderId}:rejected_drivers
```

### **Data Type: SET**
```bash
# Ví dụ: Order ID = 123, drivers 5, 12, 18 đã reject
redis-cli> SMEMBERS order:123:rejected_drivers
1) "5"
2) "12"
3) "18"

# TTL
redis-cli> TTL order:123:rejected_drivers
(integer) 86400  # 24 hours = 86400 seconds
```

### **Operations:**

```bash
# Add driver rejection
SADD order:123:rejected_drivers "5"
EXPIRE order:123:rejected_drivers 86400

# Get all rejected drivers
SMEMBERS order:123:rejected_drivers

# Check if driver rejected
SISMEMBER order:123:rejected_drivers "5"

# Remove rejection (optional)
SREM order:123:rejected_drivers "5"

# Delete all rejections for order
DEL order:123:rejected_drivers
```

---

## 🎯 Lợi ích của Redis:

1. **Performance**: Faster than database queries
2. **Auto Cleanup**: TTL 24 hours tự động xóa data cũ
3. **Scalability**: Redis handles high throughput better
4. **Simplicity**: No need for complex JPA queries
5. **Memory Efficient**: SET data type optimized for this use case

---

## 🔍 Testing:

### **Test Case 1: Driver rejects order**
```bash
# Before
curl -X POST http://localhost:8080/api/v1/orders/123/reject \
  -H "Authorization: Bearer <driver_token>" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Too far"}'

# Check Redis
redis-cli> SMEMBERS order:123:rejected_drivers
1) "5"  # Driver ID 5 rejected

# Check TTL
redis-cli> TTL order:123:rejected_drivers
(integer) 86395  # ~24 hours
```

### **Test Case 2: Find alternative driver (excluding rejected)**
```bash
# Order service will query:
# 1. Redis GEO: nearby drivers
# 2. Redis SET: filter out rejected drivers
# 3. Database: validate business rules
# 4. Mapbox: calculate real distance

# Log should show:
# 🔍 Searching for alternative drivers using Redis GEO (excluding 1 rejected drivers)
# 💾 Saved driver 5 rejection for order 123 to Redis (reason: Too far)
```

---

## ⚠️ Important Notes:

1. **Data Migration**: Existing rejection records in database will be lost after DROP TABLE. Nếu cần, backup trước.

2. **24-hour TTL**: Rejection history chỉ lưu 24 giờ. Sau đó Redis tự động xóa. This is intentional - old rejections not relevant.

3. **No Historical Data**: Redis không lưu rejection history lâu dài. If analytics needed, consider logging to separate analytics service.

4. **Redis Persistence**: Ensure Redis has persistence enabled (RDB or AOF) to prevent data loss on restart.

---

## 🚀 Deployment Checklist:

- [x] Delete `OrderDriverRejection.java`
- [x] Delete `OrderDriverRejectionRepository.java`
- [x] Update `OrderService.java` to use Redis
- [ ] **Drop database table** `order_driver_rejections`
- [ ] Restart application
- [ ] Test driver rejection flow
- [ ] Verify Redis keys are created
- [ ] Monitor Redis memory usage

---

## 📝 Redis Memory Estimation:

**Assumption:**
- Average order has 2 driver rejections
- 1000 active orders per day
- Each driver ID = 8 bytes (Long)
- SET overhead = 16 bytes per member

**Calculation:**
```
Memory per order = (8 + 16) * 2 drivers = 48 bytes
Total for 1000 orders = 48 KB
With 24h TTL, max memory = ~48 KB (negligible)
```

**Conclusion**: Redis memory impact is minimal.
