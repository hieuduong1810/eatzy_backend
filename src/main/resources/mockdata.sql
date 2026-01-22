SET time_zone = '+07:00';

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE vouchers;
TRUNCATE TABLE voucher_restaurant;
TRUNCATE TABLE menu_options;
TRUNCATE TABLE menu_option_groups;
TRUNCATE TABLE dishes;
TRUNCATE TABLE dish_categories;
TRUNCATE TABLE restaurant_restaurant_type;
TRUNCATE TABLE restaurants;
TRUNCATE TABLE restaurant_types;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Thêm dữ liệu vào bảng restaurant_types
INSERT INTO restaurant_types (id, display_order, image, name, slug) VALUES
(1, 1, 'https://placehold.co/200x200?text=Vietnamese', 'Món Việt', 'mon-viet'),
(2, 2, 'https://placehold.co/200x200?text=European', 'Món Âu', 'mon-au'),
(3, 3, 'https://placehold.co/200x200?text=Chicken', 'Gà Rán', 'ga-ran'),
(4, 4, 'https://placehold.co/200x200?text=Pizza', 'Pizza', 'pizza'),
(5, 5, 'https://placehold.co/200x200?text=Snack', 'Ăn Vặt', 'an-vat');

-- 2. Thêm dữ liệu vào bảng restaurants (Status đã đổi thành ONLINE)
INSERT INTO restaurants (id, address, average_rating, commission_rate, contact_phone, description, five_star_count, four_star_count, latitude, longitude, name, one_star_count, schedule, slug, status, three_star_count, two_star_count, owner_id) VALUES
(1, 'Canteen B4, KTX Khu B ĐHQG, Đông Hòa, Dĩ An', 4.5, 10.00, '0901234567', 'Cơm trưa sinh viên, cơm tấm sườn bì chả', 150, 20, 10.883712, 106.780654, 'Cơm Tấm B4', 5, '06:00 - 20:00', 'com-tam-b4', 'OPEN', 10, 2, 1),
(2, 'Tòa D5, KTX Khu B, Dĩ An, Bình Dương', 4.2, 12.00, '0902345678', 'Bún đậu mắm tôm chuẩn vị Hà Nội', 80, 30, 10.884100, 106.781200, 'Bún Đậu Mắm Tôm Cô Hai', 2, '09:00 - 21:00', 'bun-dau-co-hai', 'OPEN', 5, 1, 2),
(3, 'Đường Tạ Quang Bửu, Làng Đại Học', 4.8, 15.00, '0903456789', 'Pizza phong cách Ý nướng củi', 200, 10, 10.882500, 106.782000, 'Pizza Sinh Viên', 1, '10:00 - 22:00', 'pizza-sinh-vien', 'OPEN', 3, 0, 3),
(4, 'Tòa D2, KTX Khu B', 4.0, 10.00, '0904567890', 'Gà rán giòn tan, sốt cay đặc biệt', 50, 40, 10.885000, 106.779500, 'Gà Rán KTX', 10, '08:00 - 22:00', 'ga-ran-ktx', 'OPEN', 15, 5, 4),
(5, 'Cổng chính KTX Khu B', 4.6, 12.00, '0905678901', 'Bò bít tết, mỳ ý sốt bò bằm', 90, 15, 10.881500, 106.783000, 'Bistro Âu Lạc', 3, '10:00 - 21:30', 'bistro-au-lac', 'OPEN', 5, 2, 5),
(6, 'Chợ Đêm Làng Đại Học', 4.3, 10.00, '0906789012', 'Phở bò tái nạm gầu gân', 70, 25, 10.880000, 106.784000, 'Phở Bò Gia Truyền 24h', 4, '05:30 - 23:00', 'pho-bo-gia-truyen-24h', 'OPEN', 8, 1, 6),
(7, 'Canteen B3, KTX Khu B', 4.1, 10.00, '0907890123', 'Cơm phần tự chọn, canh chua cá kho', 60, 30, 10.883500, 106.780100, 'Cơm Mẹ Nấu B3', 8, '06:00 - 19:30', 'com-me-nau-b3', 'OPEN', 12, 4, 7),
(8, 'Khu dịch vụ công cộng KTX B', 4.7, 15.00, '0908901234', 'Gà rán sốt phô mai Hàn Quốc', 120, 15, 10.884500, 106.781500, 'Gà Rán Oppa', 2, '09:00 - 22:00', 'ga-ran-oppa', 'OPEN', 4, 1, 8),
(9, 'Khu phố ăn uống, đường vành đai KTX', 4.4, 12.00, '0909012345', 'Pizza hải sản, Pizza xúc xích', 85, 20, 10.879500, 106.785000, 'The Pizza House', 5, '10:00 - 22:00', 'the-pizza-house', 'ONLINE', 6, 2, 9),
(10, 'Cổng sau KTX Khu B (hướng hồ đá)', 4.2, 10.00, '0910123456', 'Bánh mì chảo full topping, bò né', 55, 20, 10.886000, 106.778500, 'Bánh Mì Chảo 3 Ngon', 3, '06:00 - 21:00', 'banh-mi-chao-3-ngon', 'OPEN', 10, 3, 10),
(11, 'Khu B mở rộng, tòa E', 4.5, 10.00, '0911234567', 'Bún bò Huế, mỳ quảng', 95, 15, 10.882000, 106.779000, 'Bún Bò Huế O Nở', 4, '06:00 - 21:00', 'bun-bo-hue-o-no', 'OPEN', 7, 2, 11),
(12, 'Tòa E1, KTX Khu B', 4.3, 15.00, '0912345678', 'Trà sữa trân châu, gà lắc phô mai', 75, 25, 10.887000, 106.780500, 'Ăn Vặt E1', 6, '09:00 - 23:00', 'an-vat-e1', 'OPEN', 9, 3, 12),
(13, 'Gần trạm xe buýt KTX B', 4.1, 10.00, '0913456789', 'Hủ tiếu gõ, mì gõ', 65, 35, 10.878000, 106.786000, 'Hủ Tiếu Mì Đêm', 10, '16:00 - 02:00', 'hu-tieu-mi-dem', 'OPEN', 15, 5, 13),
(14, 'Đường Nguyễn Du, Dĩ An (gần KTX)', 4.6, 12.00, '0914567890', 'Pizza size khổng lồ', 110, 12, 10.888000, 106.777000, 'Big Size Pizza', 2, '11:00 - 22:00', 'big-size-pizza', 'OPEN', 5, 1, 14),
(15, 'Ngã 3 621', 4.0, 10.00, '0915678901', 'Cơm gà xối mỡ, cơm rang dưa bò', 40, 30, 10.876000, 106.788000, 'Cơm Gà Xối Mỡ 123', 8, '09:00 - 21:00', 'com-ga-xoi-mo-123', 'OPEN', 12, 6, 15);

-- 3. Thêm dữ liệu vào bảng trung gian restaurant_restaurant_type
INSERT INTO restaurant_restaurant_type (restaurant_id, restaurant_type_id) VALUES
(1, 1), (2, 1), (2, 5), (3, 4), (3, 2), (4, 3), (4, 5), (5, 2), (5, 4), (6, 1), (7, 1), (8, 3), (9, 4), (9, 2), (10, 1), (10, 2), (11, 1), (12, 3), (12, 5), (13, 1), (14, 4), (14, 2), (15, 1), (15, 3);

-- 1. INSERT 50 VOUCHERS (Đã sửa start_date thành '2026-01-01')
INSERT INTO vouchers (id, code, description, discount_type, discount_value, max_discount_amount, min_order_value, start_date, end_date, total_quantity, usage_limit_per_user) VALUES
-- --- Voucher Riêng (ID 1-45) ---
-- Nhà hàng 1
(1, 'B4_WELCOME', 'Giảm 20k cho đơn đầu tiên', 'FIXED', 20000, 20000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 1),
(2, 'B4_VIP', 'Giảm 10% cho khách quen', 'PERCENTAGE', 10, 20000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(3, 'B4_FREESHIP', 'Miễn phí vận chuyển KTX', 'FIXED', 15000, 15000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 10),
-- Nhà hàng 2
(4, 'COHAI_MAM', 'Tặng 1 ly nước sấu (trừ tiền)', 'FIXED', 15000, 15000, 60000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(5, 'COHAI_10', 'Giảm 10% khi mua 2 mẹt', 'PERCENTAGE', 10, 30000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(6, 'COHAI_GROUP', 'Giảm 30k cho nhóm 4 người', 'FIXED', 30000, 30000, 200000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
-- Nhà hàng 3
(7, 'PIZZA_SV', 'Đồng giá 59k Pizza nhỏ', 'FIXED', 20000, 20000, 79000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 3),
(8, 'PIZZA_MUA2', 'Giảm 20% khi mua 2 cái', 'PERCENTAGE', 20, 100000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(9, 'PIZZA_COKE', 'Tặng 1 Coke 1.5L (trừ tiền)', 'FIXED', 20000, 20000, 120000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 1),
-- Nhà hàng 4
(10, 'GA_CRISPY', 'Giảm 15k combo gà giòn', 'FIXED', 15000, 15000, 60000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 2),
(11, 'GA_PARTY', 'Giảm 50k cho Party Set', 'FIXED', 50000, 50000, 250000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
(12, 'GA_STUDENT', 'Ưu đãi sinh viên giảm 5%', 'PERCENTAGE', 5, 10000, 30000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 10),
-- Nhà hàng 5
(13, 'BISTRO_DATE', 'Giảm 15% cho cặp đôi', 'PERCENTAGE', 15, 100000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 1),
(14, 'BISTRO_WINE', 'Giảm 20k món Beefsteak', 'FIXED', 20000, 20000, 80000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(15, 'BISTRO_NEW', 'Món mới giảm 10%', 'PERCENTAGE', 10, 50000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 1),
-- Nhà hàng 6
(16, 'PHO_SANG', 'Giảm 5k ăn sáng (6h-9h)', 'FIXED', 5000, 5000, 35000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 10),
(17, 'PHO_DEM', 'Giảm 10k ăn đêm (22h-2h)', 'FIXED', 10000, 10000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 10),
(18, 'PHO_QUAY', 'Tặng 1 phần quẩy', 'FIXED', 5000, 5000, 45000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 5),
-- Nhà hàng 7
(19, 'COM_TRUA', 'Giảm 5k cơm trưa', 'FIXED', 5000, 5000, 30000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(20, 'COM_CANH', 'Tặng canh chua', 'FIXED', 5000, 5000, 35000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(21, 'COM_ME', 'Giảm 10% đơn trên 100k', 'PERCENTAGE', 10, 20000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
-- Nhà hàng 8
(22, 'OPPA_KOREA', 'Giảm 15k món Hàn', 'FIXED', 15000, 15000, 70000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 2),
(23, 'OPPA_SPICY', 'Thử thách cay giảm 10%', 'PERCENTAGE', 10, 20000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 1),
(24, 'OPPA_TOK', 'Tặng Tokbokki (Trừ tiền)', 'FIXED', 35000, 35000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
-- Nhà hàng 9
(25, 'HOUSE_SEA', 'Giảm 20% Pizza Hải Sản', 'PERCENTAGE', 20, 50000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(26, 'HOUSE_BIG', 'Mua L giảm 30k', 'FIXED', 30000, 30000, 180000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(27, 'HOUSE_CHEESE', 'Free Viền Phô Mai', 'FIXED', 30000, 30000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 1),
-- Nhà hàng 10
(28, 'CHAO_3NGON', 'Giảm 5k cho đơn 40k', 'FIXED', 5000, 5000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(29, 'CHAO_SUA', 'Tặng sữa đậu nành', 'FIXED', 10000, 10000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(30, 'CHAO_FULL', 'Giảm 10% phần đặc biệt', 'PERCENTAGE', 10, 10000, 55000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
-- Nhà hàng 11
(31, 'HUE_SATE', 'Giảm 5k bún bò', 'FIXED', 5000, 5000, 45000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(32, 'HUE_COMBO', 'Combo 2 tô giảm 15k', 'FIXED', 15000, 15000, 90000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(33, 'HUE_MIA', 'Tặng nước mía', 'FIXED', 10000, 10000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 2),
-- Nhà hàng 12
(34, 'E1_TRASUA', 'Giảm 50% ly thứ 2', 'PERCENTAGE', 25, 15000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 10),
(35, 'E1_COMBO', 'Combo ăn vặt giảm 10k', 'FIXED', 10000, 10000, 60000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 3),
(36, 'E1_SHIP', 'Freeship nội khu', 'FIXED', 10000, 10000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 10),
-- Nhà hàng 13
(37, 'HUTIEU_KHO', 'Giảm 5k hủ tiếu khô', 'FIXED', 5000, 5000, 35000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(38, 'HUTIEU_TO', 'Tô đặc biệt giảm 10%', 'PERCENTAGE', 10, 10000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2),
(39, 'HUTIEU_DEM', 'Mã đêm khuya giảm 15k', 'FIXED', 15000, 15000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 5),
-- Nhà hàng 14
(40, 'BIG_XXL', 'Giảm 50k Pizza XXL', 'FIXED', 50000, 50000, 250000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
(41, 'BIG_FAMILY', 'Combo gia đình giảm 10%', 'PERCENTAGE', 10, 100000, 300000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
(42, 'BIG_WINGS', 'Tặng cánh gà (trừ tiền)', 'FIXED', 60000, 60000, 300000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
-- Nhà hàng 15
(43, 'GA123_XOI', 'Giảm 5k cơm gà xối mỡ', 'FIXED', 5000, 5000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 5),
(44, 'GA123_RANG', 'Giảm 10k cơm rang dưa bò', 'FIXED', 10000, 10000, 45000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 2),
(45, 'GA123_CANH', 'Tặng canh rong biển', 'FIXED', 10000, 10000, 40000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 3),

-- --- Voucher Dùng Chung Mới (ID 46-50) ---
(46, 'FS_TOANKHU', 'Miễn phí vận chuyển toàn KTX', 'FREESHIP', NULL, 15000, 50000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 10),
(47, 'TIEC_TUNG', 'Giảm 30k cho tiệc cuối tuần', 'FIXED', 30000, 30000, 150000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 2),
(48, 'FS_ANVAT', 'Freeship cho đơn ăn vặt', 'FREESHIP', NULL, 10000, 30000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 5),
(49, 'FLASH_SALE', 'Giảm nóng 50k đơn từ 100k', 'FIXED', 50000, 50000, 100000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 1),
(50, 'FS_DEM', 'Cứu đói đêm khuya không lo ship', 'FREESHIP', NULL, 20000, 60000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 10);

-- 2. INSERT BẢNG TRUNG GIAN (Áp dụng voucher cho nhà hàng)
INSERT INTO voucher_restaurant  (restaurant_id, voucher_id) VALUES
-- Mapping cho 45 Voucher Riêng (Mỗi nhà hàng 3 cái)
(1, 1), (1, 2), (1, 3),
(2, 4), (2, 5), (2, 6),
(3, 7), (3, 8), (3, 9),
(4, 10), (4, 11), (4, 12),
(5, 13), (5, 14), (5, 15),
(6, 16), (6, 17), (6, 18),
(7, 19), (7, 20), (7, 21),
(8, 22), (8, 23), (8, 24),
(9, 25), (9, 26), (9, 27),
(10, 28), (10, 29), (10, 30),
(11, 31), (11, 32), (11, 33),
(12, 34), (12, 35), (12, 36),
(13, 37), (13, 38), (13, 39),
(14, 40), (14, 41), (14, 42),
(15, 43), (15, 44), (15, 45),

-- Mapping cho 5 Voucher Chung (Nhiều nhà hàng dùng chung 1 voucher)
-- ID 46: FS_TOANKHU (Cơm, Bún, Phở)
(1, 46), (2, 46), (6, 46), (7, 46), (10, 46), (11, 46), (13, 46), (15, 46),
-- ID 47: TIEC_TUNG (Pizza, Gà, Steak)
(3, 47), (4, 47), (5, 47), (8, 47), (9, 47), (14, 47),
-- ID 48: FS_ANVAT (Ăn vặt, Trà sữa)
(12, 48), (8, 48), (4, 48),
-- ID 49: FLASH_SALE (Toàn bộ 15 nhà hàng)
(1, 49), (2, 49), (3, 49), (4, 49), (5, 49), (6, 49), (7, 49), (8, 49), (9, 49), (10, 49), (11, 49), (12, 49), (13, 49), (14, 49), (15, 49),
-- ID 50: FS_DEM (Bán đêm)
(6, 50), (13, 50), (12, 50);

INSERT INTO dish_categories (id, display_order, name, slug, restaurant_id) VALUES
-- 1. Cơm Tấm B4
(1, 1, 'Cơm Tấm Sài Gòn', 'com-tam-sai-gon', 1),
(2, 2, 'Món Thêm', 'mon-them', 1),
(3, 3, 'Canh & Giải Khát', 'canh-giai-khat', 1),

-- 2. Bún Đậu Mắm Tôm Cô Hai
(4, 1, 'Mẹt Bún Đậu', 'met-bun-dau', 2),
(5, 2, 'Topping Gọi Thêm', 'topping-goi-them', 2),
(6, 3, 'Nước Uống', 'nuoc-uong', 2),

-- 3. Pizza Sinh Viên
(7, 1, 'Pizza Truyền Thống', 'pizza-truyen-thong', 3),
(8, 2, 'Mỳ Ý & Salad', 'my-y-salad', 3),
(9, 3, 'Combo Siêu Tiết Kiệm', 'combo-sieu-tiet-kiem', 3),

-- 4. Gà Rán KTX
(10, 1, 'Combo Gà Rán', 'combo-ga-ran', 4),
(11, 2, 'Gà Lẻ Từng Miếng', 'ga-le-tung-mieng', 4),
(12, 3, 'Khoai Tây & Nước', 'khoai-tay-nuoc', 4),

-- 5. Bistro Âu Lạc
(13, 1, 'Beefsteak Thượng Hạng', 'beefsteak-thuong-hang', 5),
(14, 2, 'Pasta & Spaghetti', 'pasta-spaghetti', 5),
(15, 3, 'Salad & Soup', 'salad-soup', 5),

-- 6. Phở Bò Gia Truyền 24h
(16, 1, 'Phở Bò', 'pho-bo', 6),
(17, 2, 'Phở Gà', 'pho-ga', 6),
(18, 3, 'Món Ăn Kèm', 'mon-an-kem', 6),

-- 7. Cơm Mẹ Nấu B3
(19, 1, 'Món Mặn Hôm Nay', 'mon-man-hom-nay', 7),
(20, 2, 'Canh & Rau Xào', 'canh-rau-xao', 7),
(21, 3, 'Tráng Miệng', 'trang-mieng', 7),

-- 8. Gà Rán Oppa
(22, 1, 'Gà Sốt Hàn Quốc', 'ga-sot-han-quoc', 8),
(23, 2, 'Cơm Gà Hải Nam', 'com-ga-hai-nam', 8),
(24, 3, 'Panchan & Tokbokki', 'panchan-tokbokki', 8),

-- 9. The Pizza House
(25, 1, 'Pizza Hải Sản', 'pizza-hai-san', 9),
(26, 2, 'Pizza Bò & Gà', 'pizza-bo-ga', 9),
(27, 3, 'Khai Vị', 'khai-vi', 9),

-- 10. Bánh Mì Chảo 3 Ngon
(28, 1, 'Bánh Mì Chảo', 'banh-mi-chao', 10),
(29, 2, 'Bò Né', 'bo-ne', 10),
(30, 3, 'Sữa Đậu & Nước Ngọt', 'sua-dau-nuoc-ngot', 10),

-- 11. Bún Bò Huế O Nở
(31, 1, 'Bún Bò Đặc Biệt', 'bun-bo-dac-biet', 11),
(32, 2, 'Bún Bò Truyền Thống', 'bun-bo-truyen-thong', 11),
(33, 3, 'Giải Khát', 'giai-khat', 11),

-- 12. Ăn Vặt E1
(34, 1, 'Trà Sữa & Trà Trái Cây', 'tra-sua-tra-trai-cay', 12),
(35, 2, 'Xiên Que & Chiên Rán', 'xien-que-chien-ran', 12),
(36, 3, 'Gà Lắc', 'ga-lac', 12),

-- 13. Hủ Tiếu Mì Đêm
(37, 1, 'Hủ Tiếu Nam Vang', 'hu-tieu-nam-vang', 13),
(38, 2, 'Mì Hoành Thánh', 'mi-hoanh-thanh', 13),
(39, 3, 'Hủ Tiếu Khô', 'hu-tieu-kho', 13),

-- 14. Big Size Pizza
(40, 1, 'Pizza Size XXL', 'pizza-size-xxl', 14),
(41, 2, 'Món Khai Vị', 'mon-khai-vi', 14),
(42, 3, 'Nước Ngọt Chai Lớn', 'nuoc-ngot-chai-lon', 14),

-- 15. Cơm Gà Xối Mỡ 123
(43, 1, 'Cơm Gà Xối Mỡ', 'com-ga-xoi-mo', 15),
(44, 2, 'Cơm Rang Dưa Bò', 'com-rang-dua-bo', 15),
(45, 3, 'Canh & Soup', 'canh-soup', 15);

-- Xóa dữ liệu cũ để tránh trùng lặp nếu cần
-- TRUNCATE TABLE dishes; 

INSERT INTO dishes (id, availability_quantity, description, image_url, name, price, category_id, restaurant_id) VALUES
-- ===================================================
-- NHÀ HÀNG 1: Cơm Tấm B4 (Cat: 1-Cơm, 2-Thêm, 3-Canh)
-- ===================================================
(1, 100, 'Sườn cốt lết nướng than hoa', 'https://placehold.co/200x200?text=Com+Suon', 'Cơm Tấm Sườn', 35000, 1, 1),
(2, 100, 'Sườn nướng bì chả truyền thống', 'https://placehold.co/200x200?text=Com+Suon+Bi+Cha', 'Cơm Sườn Bì Chả', 45000, 1, 1),
(3, 100, 'Ba rọi nướng đậm đà', 'https://placehold.co/200x200?text=Com+Ba+Roi', 'Cơm Ba Rọi Nướng', 38000, 1, 1),
(4, 100, 'Gà góc tư nướng mật ong', 'https://placehold.co/200x200?text=Com+Ga+Nuong', 'Cơm Gà Nướng', 40000, 1, 1),
(5, 200, 'Chả trứng hấp', 'https://placehold.co/200x200?text=Cha+Trung', 'Chả Trứng', 5000, 2, 1),
(6, 200, 'Bì heo trộn thính', 'https://placehold.co/200x200?text=Bi+Heo', 'Bì Heo', 5000, 2, 1),
(7, 200, 'Trứng ốp la', 'https://placehold.co/200x200?text=Trung+Op+La', 'Trứng Ốp La', 6000, 2, 1),
(8, 50, 'Canh khổ qua dồn thịt', 'https://placehold.co/200x200?text=Canh+Kho+Qua', 'Canh Khổ Qua', 12000, 3, 1),

-- ===================================================
-- NHÀ HÀNG 2: Bún Đậu Cô Hai (Cat: 4-Mẹt, 5-Topping, 6-Nước)
-- ===================================================
(9, 100, 'Đầy đủ các loại topping', 'https://placehold.co/200x200?text=Bun+Dau+Ta+La', 'Bún Đậu Tá Lả', 55000, 4, 2),
(10, 100, 'Bún và đậu hũ chiên', 'https://placehold.co/200x200?text=Bun+Dau+Truyen+Thong', 'Bún Đậu Truyền Thống', 30000, 4, 2),
(11, 100, 'Thêm thịt chân giò luộc', 'https://placehold.co/200x200?text=Bun+Dau+Thit', 'Bún Đậu Thịt Luộc', 40000, 4, 2),
(12, 150, 'Chả cốm Hà Nội', 'https://placehold.co/200x200?text=Cha+Com', 'Chả Cốm', 15000, 5, 2),
(13, 150, 'Nem chua rán', 'https://placehold.co/200x200?text=Nem+Chua', 'Nem Chua Rán', 30000, 5, 2),
(14, 150, 'Dồi sụn nướng', 'https://placehold.co/200x200?text=Doi+Sun', 'Dồi Sụn', 25000, 5, 2),
(15, 200, 'Nước sấu ngâm', 'https://placehold.co/200x200?text=Nuoc+Sau', 'Nước Sấu', 15000, 6, 2),

-- ===================================================
-- NHÀ HÀNG 3: Pizza Sinh Viên (Cat: 7-Pizza, 8-Mỳ, 9-Combo)
-- ===================================================
(16, 100, 'Pizza phô mai mozzarella', 'https://placehold.co/200x200?text=Pizza+Cheese', 'Pizza Phô Mai', 60000, 7, 3),
(17, 100, 'Pizza xúc xích Pepperoni', 'https://placehold.co/200x200?text=Pizza+Pepperoni', 'Pizza Pepperoni', 70000, 7, 3),
(18, 100, 'Pizza thịt dăm bông và dứa', 'https://placehold.co/200x200?text=Pizza+Hawaii', 'Pizza Hawaii', 75000, 7, 3),
(19, 100, 'Pizza bò bằm sốt BBQ', 'https://placehold.co/200x200?text=Pizza+BBQ', 'Pizza Bò BBQ', 80000, 7, 3),
(20, 100, 'Mỳ ý sốt cà chua bò bằm', 'https://placehold.co/200x200?text=Spaghetti', 'Mỳ Ý Bolognese', 35000, 8, 3),
(21, 100, 'Combo tiết kiệm cho nhóm', 'https://placehold.co/200x200?text=Combo+Ban+Be', 'Combo Bạn Bè', 120000, 9, 3),

-- ===================================================
-- NHÀ HÀNG 4: Gà Rán KTX (Cat: 10-Combo, 11-Gà lẻ, 12-Khoai)
-- ===================================================
(22, 100, 'Gà rán truyền thống da giòn', 'https://placehold.co/200x200?text=Ga+Truyen+Thong', 'Gà Rán Truyền Thống', 30000, 11, 4),
(23, 100, 'Gà rán sốt cay', 'https://placehold.co/200x200?text=Ga+Sot+Cay', 'Gà Rán Sốt Cay', 35000, 11, 4),
(24, 100, 'Gà viên chiên giòn', 'https://placehold.co/200x200?text=Popcorn+Chicken', 'Gà Viên Popcorn', 30000, 11, 4),
(25, 100, 'Khoai tây chiên', 'https://placehold.co/200x200?text=French+Fries', 'Khoai Tây Chiên', 15000, 12, 4),
(26, 100, 'Khoai tây lắc phô mai', 'https://placehold.co/200x200?text=Cheese+Fries', 'Khoai Tây Lắc Phô Mai', 20000, 12, 4),

-- ===================================================
-- NHÀ HÀNG 5: Bistro Âu Lạc (Cat: 13-Beefsteak, 14-Pasta, 15-Salad)
-- ===================================================
(27, 50, 'Bò bít tết thăn ngoại', 'https://placehold.co/200x200?text=Beefsteak', 'Bò Bít Tết', 85000, 13, 5),
(28, 50, 'Bò lúc lắc khoai tây', 'https://placehold.co/200x200?text=Bo+Luc+Lac', 'Bò Lúc Lắc', 75000, 13, 5),
(29, 60, 'Mỳ ý sốt kem nấm', 'https://placehold.co/200x200?text=Carbonara', 'Mỳ Ý Carbonara', 60000, 14, 5),
(30, 60, 'Mỳ ý hải sản', 'https://placehold.co/200x200?text=Seafood+Pasta', 'Mỳ Ý Hải Sản', 65000, 14, 5),
(31, 50, 'Salad cá ngừ', 'https://placehold.co/200x200?text=Tuna+Salad', 'Salad Cá Ngừ', 45000, 15, 5),
(32, 50, 'Soup bí đỏ kem tươi', 'https://placehold.co/200x200?text=Pumpkin+Soup', 'Soup Bí Đỏ', 35000, 15, 5),

-- ===================================================
-- NHÀ HÀNG 6: Phở Bò 24h (Cat: 16-Bò, 17-Gà, 18-Kèm)
-- ===================================================
(33, 100, 'Phở bò tái', 'https://placehold.co/200x200?text=Pho+Tai', 'Phở Tái', 45000, 16, 6),
(34, 100, 'Phở bò chín', 'https://placehold.co/200x200?text=Pho+Chin', 'Phở Chín', 45000, 16, 6),
(35, 100, 'Phở tái nạm', 'https://placehold.co/200x200?text=Pho+Nam', 'Phở Tái Nạm', 50000, 16, 6),
(36, 100, 'Phở gà ta xé', 'https://placehold.co/200x200?text=Pho+Ga', 'Phở Gà Ta', 45000, 17, 6),
(37, 200, 'Quẩy giòn', 'https://placehold.co/200x200?text=Quay', 'Quẩy', 5000, 18, 6),
(38, 100, 'Trứng gà trần', 'https://placehold.co/200x200?text=Trung+Tran', 'Trứng Trần', 8000, 18, 6),

-- ===================================================
-- NHÀ HÀNG 7: Cơm Mẹ Nấu B3 (Cat: 19-Mặn, 20-Canh, 21-Tráng miệng)
-- ===================================================
(39, 80, 'Thịt kho tàu nước dừa', 'https://placehold.co/200x200?text=Thit+Kho', 'Cơm Thịt Kho Tàu', 35000, 19, 7),
(40, 80, 'Cá lóc kho tộ', 'https://placehold.co/200x200?text=Ca+Kho', 'Cơm Cá Kho Tộ', 35000, 19, 7),
(41, 80, 'Gà xào sả ớt', 'https://placehold.co/200x200?text=Ga+Xa+Ot', 'Cơm Gà Xào Sả Ớt', 35000, 19, 7),
(42, 80, 'Đậu hũ nhồi thịt sốt cà', 'https://placehold.co/200x200?text=Dau+Hu+Sot', 'Cơm Đậu Hũ Nhồi Thịt', 30000, 19, 7),
(43, 100, 'Canh chua cá hú', 'https://placehold.co/200x200?text=Canh+Chua', 'Canh Chua', 15000, 20, 7),
(44, 100, 'Canh rau ngót nấu tôm', 'https://placehold.co/200x200?text=Canh+Rau', 'Canh Rau Ngót', 10000, 20, 7),

-- ===================================================
-- NHÀ HÀNG 8: Gà Rán Oppa (Cat: 22-Sốt, 23-Cơm, 24-Panchan)
-- ===================================================
(45, 80, 'Gà sốt cay Hàn Quốc', 'https://placehold.co/200x200?text=Yangnyeom', 'Gà Sốt Cay Yangnyeom', 50000, 22, 8),
(46, 80, 'Gà sốt mật ong', 'https://placehold.co/200x200?text=Honey+Chicken', 'Gà Sốt Mật Ong', 50000, 22, 8),
(47, 80, 'Gà sốt tương tỏi', 'https://placehold.co/200x200?text=Soy+Garlic', 'Gà Sốt Tương Tỏi', 50000, 22, 8),
(48, 80, 'Cơm đùi gà sốt Teriyaki', 'https://placehold.co/200x200?text=Com+Ga+Teriyaki', 'Cơm Gà Teriyaki', 45000, 23, 8),
(49, 100, 'Bánh gạo cay', 'https://placehold.co/200x200?text=Tokbokki', 'Tokbokki', 35000, 24, 8),
(50, 100, 'Canh rong biển', 'https://placehold.co/200x200?text=Seaweed+Soup', 'Canh Rong Biển', 15000, 24, 8),

-- ===================================================
-- NHÀ HÀNG 9: The Pizza House (Cat: 25-Hải sản, 26-Bò Gà, 27-Khai vị)
-- ===================================================
(51, 80, 'Pizza tôm và mực', 'https://placehold.co/200x200?text=Seafood+Pizza', 'Pizza Hải Sản', 110000, 25, 9),
(52, 80, 'Pizza cá ngừ đại dương', 'https://placehold.co/200x200?text=Tuna+Pizza', 'Pizza Cá Ngừ', 100000, 25, 9),
(53, 80, 'Pizza gà nướng nấm', 'https://placehold.co/200x200?text=Chicken+Mushroom', 'Pizza Gà Nấm', 95000, 26, 9),
(54, 80, 'Khoai tây múi cau', 'https://placehold.co/200x200?text=Wedges', 'Khoai Tây Múi Cau', 35000, 27, 9),
(55, 80, 'Mực vòng chiên giòn', 'https://placehold.co/200x200?text=Calamari', 'Mực Vòng Chiên', 55000, 27, 9),

-- ===================================================
-- NHÀ HÀNG 10: Bánh Mì Chảo 3 Ngon (Cat: 28-Chảo, 29-Né, 30-Nước)
-- ===================================================
(56, 100, 'Pate, trứng, chả lụa, xíu mại', 'https://placehold.co/200x200?text=Chao+Thap+Cam', 'Bánh Mì Chảo Thập Cẩm', 35000, 28, 10),
(57, 100, 'Xíu mại sốt cà', 'https://placehold.co/200x200?text=Chao+Xiu+Mai', 'Bánh Mì Chảo Xíu Mại', 30000, 28, 10),
(58, 100, 'Bò né trứng ốp la', 'https://placehold.co/200x200?text=Bo+Ne', 'Bò Né', 45000, 29, 10),
(59, 100, 'Sữa đậu nành', 'https://placehold.co/200x200?text=Sua+Dau', 'Sữa Đậu Nành', 10000, 30, 10),
(60, 100, 'Sữa bắp', 'https://placehold.co/200x200?text=Sua+Bap', 'Sữa Bắp', 12000, 30, 10),

-- ===================================================
-- NHÀ HÀNG 11: Bún Bò O Nở (Cat: 31-Đặc biệt, 32-Truyền thống, 33-Nước)
-- ===================================================
(61, 100, 'Đầy đủ giò, nạm, gân, chả', 'https://placehold.co/200x200?text=Bun+Bo+Dac+Biet', 'Bún Bò Đặc Biệt', 50000, 31, 11),
(62, 100, 'Bún bò giò heo', 'https://placehold.co/200x200?text=Bun+Gio', 'Bún Bò Giò Heo', 40000, 32, 11),
(63, 100, 'Bún bò nạm', 'https://placehold.co/200x200?text=Bun+Nam', 'Bún Bò Nạm', 40000, 32, 11),
(64, 100, 'Bún bò tái', 'https://placehold.co/200x200?text=Bun+Tai', 'Bún Bò Tái', 40000, 32, 11),
(65, 100, 'Nước mía tắc', 'https://placehold.co/200x200?text=Nuoc+Mia', 'Nước Mía', 10000, 33, 11),

-- ===================================================
-- NHÀ HÀNG 12: Ăn Vặt E1 (Cat: 34-Trà sữa, 35-Xiên, 36-Gà lắc)
-- ===================================================
(66, 100, 'Trà sữa trân châu', 'https://placehold.co/200x200?text=Tra+Sua', 'Trà Sữa', 20000, 34, 12),
(67, 100, 'Trà đào cam sả', 'https://placehold.co/200x200?text=Tra+Dao', 'Trà Đào Cam Sả', 25000, 34, 12),
(68, 100, 'Cá viên chiên', 'https://placehold.co/200x200?text=Ca+Vien', 'Cá Viên Chiên', 15000, 35, 12),
(69, 100, 'Bò viên chiên', 'https://placehold.co/200x200?text=Bo+Vien', 'Bò Viên Chiên', 15000, 35, 12),
(70, 100, 'Xúc xích đức', 'https://placehold.co/200x200?text=Xuc+Xich', 'Xúc Xích Đức', 15000, 35, 12),
(71, 100, 'Gà lắc phô mai', 'https://placehold.co/200x200?text=Ga+Lac', 'Gà Lắc Phô Mai', 25000, 36, 12),

-- ===================================================
-- NHÀ HÀNG 13: Hủ Tiếu Mì (Cat: 37-Hủ tiếu, 38-Mì, 39-Khô)
-- ===================================================
(72, 100, 'Hủ tiếu Nam Vang', 'https://placehold.co/200x200?text=Hu+Tieu', 'Hủ Tiếu Nam Vang', 35000, 37, 13),
(73, 100, 'Hủ tiếu xương ống', 'https://placehold.co/200x200?text=Hu+Tieu+Xuong', 'Hủ Tiếu Xương', 30000, 37, 13),
(74, 100, 'Mì hoành thánh xá xíu', 'https://placehold.co/200x200?text=Mi+Hoanh+Thanh', 'Mì Hoành Thánh', 35000, 38, 13),
(75, 100, 'Hủ tiếu khô sốt dầu hào', 'https://placehold.co/200x200?text=Hu+Tieu+Kho', 'Hủ Tiếu Khô', 35000, 39, 13),

-- ===================================================
-- NHÀ HÀNG 14: Big Size Pizza (Cat: 40-XXL, 41-Khai vị, 42-Nước)
-- ===================================================
(76, 50, 'Pizza khổng lồ mix 2 vị', 'https://placehold.co/200x200?text=Pizza+XXL', 'Pizza Khổng Lồ', 250000, 40, 14),
(77, 50, 'Salad Ceasar', 'https://placehold.co/200x200?text=Caesar+Salad', 'Salad Ceasar', 50000, 41, 14),
(78, 50, 'Cánh gà nướng BBQ', 'https://placehold.co/200x200?text=BBQ+Wings', 'Cánh Gà BBQ', 60000, 41, 14),
(79, 100, 'Coca Cola 1.5L', 'https://placehold.co/200x200?text=Coke', 'Coca Cola Chai Lớn', 20000, 42, 14),

-- ===================================================
-- NHÀ HÀNG 15: Cơm Gà 123 (Cat: 43-Gà, 44-Bò, 45-Canh)
-- ===================================================
(80, 100, 'Cơm gà xối mỡ', 'https://placehold.co/200x200?text=Com+Ga+Xoi+Mo', 'Cơm Gà Xối Mỡ', 40000, 43, 15),
(81, 100, 'Cơm chiên dương châu', 'https://placehold.co/200x200?text=Com+Chien', 'Cơm Chiên Dương Châu', 35000, 43, 15),
(82, 100, 'Cơm rang dưa bò', 'https://placehold.co/200x200?text=Com+Rang+Dua+Bo', 'Cơm Rang Dưa Bò', 45000, 44, 15),
(83, 100, 'Cơm bò lúc lắc', 'https://placehold.co/200x200?text=Com+Bo+Luc+Lac', 'Cơm Bò Lúc Lắc', 50000, 44, 15),
(84, 100, 'Canh rong biển thịt bằm', 'https://placehold.co/200x200?text=Canh+Rong+Bien', 'Canh Rong Biển', 10000, 45, 15);

-- Xóa dữ liệu cũ để làm mới hoàn toàn
-- TRUNCATE TABLE menu_option_groups;

INSERT INTO menu_option_groups (id, dish_id, group_name, min_choices, max_choices) VALUES
-- =================================================================
-- NHÀ HÀNG 1: Cơm Tấm B4 (Dishes 1-8)
-- =================================================================
-- Món Cơm (1,2,3,4)
(1, 1, 'Chọn loại cơm', 1, 1), (2, 1, 'Canh ăn kèm', 0, 1),
(3, 2, 'Chọn loại cơm', 1, 1), (4, 2, 'Canh ăn kèm', 0, 1),
(5, 3, 'Chọn loại cơm', 1, 1), (6, 3, 'Canh ăn kèm', 0, 1),
(7, 4, 'Chọn loại cơm', 1, 1), (8, 4, 'Canh ăn kèm', 0, 1),
-- Món Thêm (5,6,7) & Canh (8)
(9, 5, 'Yêu cầu', 0, 1), -- Chả trứng (Lấy nước mắm/Không)
(10, 6, 'Yêu cầu', 0, 1), -- Bì heo
(11, 7, 'Độ chín trứng', 1, 1), -- Trứng ốp la (Chín/Lòng đào)
(12, 8, 'Dụng cụ ăn uống', 1, 1), -- Canh khổ qua

-- =================================================================
-- NHÀ HÀNG 2: Bún Đậu Cô Hai (Dishes 9-15)
-- =================================================================
-- Bún đậu (9,10,11)
(13, 9, 'Loại mắm chấm', 1, 1), (14, 9, 'Rau sống', 1, 1),
(15, 10, 'Loại mắm chấm', 1, 1), (16, 10, 'Rau sống', 1, 1),
(17, 11, 'Loại mắm chấm', 1, 1), (18, 11, 'Rau sống', 1, 1),
-- Món thêm (12,13,14) & Nước (15)
(19, 12, 'Tương chấm', 0, 1),
(20, 13, 'Tương chấm', 0, 1),
(21, 14, 'Tương chấm', 0, 1),
(22, 15, 'Lượng đá', 1, 1), -- Nước sấu

-- =================================================================
-- NHÀ HÀNG 3: Pizza Sinh Viên (Dishes 16-21)
-- =================================================================
-- Pizza (16,17,18,19)
(23, 16, 'Kích thước', 1, 1), (24, 16, 'Đế bánh', 1, 1),
(25, 17, 'Kích thước', 1, 1), (26, 17, 'Đế bánh', 1, 1),
(27, 18, 'Kích thước', 1, 1), (28, 18, 'Đế bánh', 1, 1),
(29, 19, 'Kích thước', 1, 1), (30, 19, 'Đế bánh', 1, 1),
-- Mỳ Ý (20) & Combo (21)
(31, 20, 'Phô mai bột', 0, 1),
(32, 21, 'Chọn nước ngọt', 1, 1),

-- =================================================================
-- NHÀ HÀNG 4: Gà Rán KTX (Dishes 22-26)
-- =================================================================
-- Gà rán (22,23,24)
(33, 22, 'Phần gà', 1, 1), (34, 22, 'Tương chấm', 1, 2),
(35, 23, 'Độ cay', 1, 1),
(36, 24, 'Bột phô mai', 0, 1),
-- Khoai (25,26)
(37, 25, 'Tương chấm', 1, 1),
(38, 26, 'Lượng bột phô mai', 1, 1),

-- =================================================================
-- NHÀ HÀNG 5: Bistro Âu Lạc (Dishes 27-32)
-- =================================================================
-- Beefsteak (27,28)
(39, 27, 'Mức độ chín', 1, 1), (40, 27, 'Loại sốt', 1, 1),
(41, 28, 'Bánh mì/Khoai tây', 1, 1),
-- Pasta (29,30)
(42, 29, 'Loại sợi mỳ', 1, 1),
(43, 30, 'Độ cay', 1, 1),
-- Salad/Soup (31,32)
(44, 31, 'Sốt trộn', 1, 1),
(45, 32, 'Bánh mì đi kèm', 0, 1),

-- =================================================================
-- NHÀ HÀNG 6: Phở Bò 24h (Dishes 33-38)
-- =================================================================
-- Phở (33,34,35,36)
(46, 33, 'Bánh phở', 1, 1), (47, 33, 'Hành & Rau', 1, 1),
(48, 34, 'Bánh phở', 1, 1), (49, 34, 'Hành & Rau', 1, 1),
(50, 35, 'Bánh phở', 1, 1), (51, 35, 'Hành & Rau', 1, 1),
(52, 36, 'Loại da gà', 1, 1), -- Da giòn/Không da
-- Quẩy/Trứng (37,38)
(53, 37, 'Số lượng', 1, 1),
(54, 38, 'Độ chín', 1, 1),

-- =================================================================
-- NHÀ HÀNG 7: Cơm Mẹ Nấu B3 (Dishes 39-44)
-- =================================================================
-- Cơm món mặn (39,40,41,42)
(55, 39, 'Canh hôm nay', 0, 1),
(56, 40, 'Canh hôm nay', 0, 1),
(57, 41, 'Canh hôm nay', 0, 1),
(58, 42, 'Canh hôm nay', 0, 1),
-- Canh riêng (43,44)
(59, 43, 'Dụng cụ', 1, 1),
(60, 44, 'Dụng cụ', 1, 1),

-- =================================================================
-- NHÀ HÀNG 8: Gà Rán Oppa (Dishes 45-50)
-- =================================================================
-- Gà sốt (45,46,47)
(61, 45, 'Cấp độ cay', 1, 1), (62, 45, 'Đồ chua ăn kèm', 1, 1),
(63, 46, 'Đồ chua ăn kèm', 1, 1),
(64, 47, 'Đồ chua ăn kèm', 1, 1),
-- Cơm/Tok (48,49,50)
(65, 48, 'Sốt thêm', 0, 1),
(66, 49, 'Topping thêm', 0, 3), -- Phô mai/Chả cá
(67, 50, 'Dụng cụ', 1, 1),

-- =================================================================
-- NHÀ HÀNG 9: The Pizza House (Dishes 51-55)
-- =================================================================
-- Pizza (51,52,53)
(68, 51, 'Size bánh', 1, 1), (69, 51, 'Viền bánh', 0, 1),
(70, 52, 'Size bánh', 1, 1), (71, 52, 'Viền bánh', 0, 1),
(72, 53, 'Size bánh', 1, 1), (73, 53, 'Viền bánh', 0, 1),
-- Sides (54,55)
(74, 54, 'Sốt chấm', 1, 1),
(75, 55, 'Sốt chấm', 1, 1),

-- =================================================================
-- NHÀ HÀNG 10: Bánh Mì Chảo 3 Ngon (Dishes 56-60)
-- =================================================================
-- Chảo (56,57,58)
(76, 56, 'Độ chín trứng', 1, 1), (77, 56, 'Bánh mì thêm', 0, 2),
(78, 57, 'Độ cay', 1, 1),
(79, 58, 'Độ chín bò', 1, 1), (80, 58, 'Độ chín trứng', 1, 1),
-- Nước (59,60)
(81, 59, 'Đá/Nóng', 1, 1),
(82, 60, 'Đá/Nóng', 1, 1),

-- =================================================================
-- NHÀ HÀNG 11: Bún Bò O Nở (Dishes 61-65)
-- =================================================================
-- Bún bò (61,62,63,64)
(83, 61, 'Rau ăn kèm', 1, 1), (84, 61, 'Sa tế', 1, 1),
(85, 62, 'Rau ăn kèm', 1, 1),
(86, 63, 'Rau ăn kèm', 1, 1),
(87, 64, 'Độ tái', 1, 1),
-- Nước mía (65)
(88, 65, 'Lượng đá', 1, 1),

-- =================================================================
-- NHÀ HÀNG 12: Ăn Vặt E1 (Dishes 66-71)
-- =================================================================
-- Trà (66,67)
(89, 66, 'Mức đường', 1, 1), (90, 66, 'Mức đá', 1, 1), (91, 66, 'Topping', 0, 3),
(92, 67, 'Mức đá', 1, 1),
-- Xiên que/Gà lắc (68-71)
(93, 68, 'Tương chấm', 1, 1),
(94, 69, 'Tương chấm', 1, 1),
(95, 70, 'Tương chấm', 1, 1),
(96, 71, 'Vị bột', 1, 1), -- Phô mai/Xí muội

-- =================================================================
-- NHÀ HÀNG 13: Hủ Tiếu Mì Đêm (Dishes 72-75)
-- =================================================================
(97, 72, 'Sợi bánh', 1, 1), (98, 72, 'Khô/Nước', 1, 1),
(99, 73, 'Sợi bánh', 1, 1),
(100, 74, 'Hành hẹ', 1, 1),
(101, 75, 'Loại nước sốt', 1, 1),

-- =================================================================
-- NHÀ HÀNG 14: Big Size Pizza (Dishes 76-79)
-- =================================================================
(102, 76, 'Vị một nửa trái', 1, 1), (103, 76, 'Vị một nửa phải', 1, 1),
(104, 77, 'Sốt salad', 1, 1),
(105, 78, 'Độ cay', 1, 1),
(106, 79, 'Ly đá', 0, 1),

-- =================================================================
-- NHÀ HÀNG 15: Cơm Gà Xối Mỡ 123 (Dishes 80-84)
-- =================================================================
(107, 80, 'Phần thịt', 1, 1), (108, 80, 'Nước chấm', 1, 1),
(109, 81, 'Tương ớt', 0, 1),
(110, 82, 'Canh kèm', 0, 1),
(111, 83, 'Độ chín bò', 1, 1),
(112, 84, 'Dụng cụ', 1, 1);

-- TRUNCATE TABLE menu_options;

INSERT INTO menu_options (id, is_available, name, price_adjustment, group_id) VALUES
-- =================================================================
-- 1. CƠM TẤM B4 (Groups 1-12)
-- =================================================================
-- Group 1, 3, 5, 7: Chọn loại cơm (Cho các món cơm chính)
(1, 1, 'Cơm tấm thường', 0, 1), (2, 1, 'Cơm thêm (+5k)', 5000, 1), (3, 1, 'Ít cơm', 0, 1),
(4, 1, 'Cơm tấm thường', 0, 3), (5, 1, 'Cơm thêm (+5k)', 5000, 3), (6, 1, 'Ít cơm', 0, 3),
(7, 1, 'Cơm tấm thường', 0, 5), (8, 1, 'Cơm thêm (+5k)', 5000, 5), (9, 1, 'Ít cơm', 0, 5),
(10, 1, 'Cơm tấm thường', 0, 7), (11, 1, 'Cơm thêm (+5k)', 5000, 7), (12, 1, 'Ít cơm', 0, 7),

-- Group 2, 4, 6, 8: Canh ăn kèm
(13, 1, 'Không lấy canh', 0, 2), (14, 1, 'Canh rong biển (+5k)', 5000, 2), (15, 1, 'Canh khổ qua (+10k)', 10000, 2),
(16, 1, 'Không lấy canh', 0, 4), (17, 1, 'Canh rong biển (+5k)', 5000, 4), (18, 1, 'Canh khổ qua (+10k)', 10000, 4),
(19, 1, 'Không lấy canh', 0, 6), (20, 1, 'Canh rong biển (+5k)', 5000, 6), (21, 1, 'Canh khổ qua (+10k)', 10000, 6),
(22, 1, 'Không lấy canh', 0, 8), (23, 1, 'Canh rong biển (+5k)', 5000, 8), (24, 1, 'Canh khổ qua (+10k)', 10000, 8),

-- Group 9, 10: Yêu cầu cho món thêm
(25, 1, 'Lấy nước mắm chua ngọt', 0, 9), (26, 1, 'Lấy nước mắm mặn', 0, 9),
(27, 1, 'Nhiều thính', 0, 10), (28, 1, 'Ít thính', 0, 10),

-- Group 11: Độ chín trứng ốp la
(29, 1, 'Trứng chín kỹ', 0, 11), (30, 1, 'Lòng đào', 0, 11),

-- Group 12: Dụng cụ canh
(31, 1, 'Lấy muỗng', 0, 12), (32, 1, 'Không lấy muỗng', 0, 12),

-- =================================================================
-- 2. BÚN ĐẬU CÔ HAI (Groups 13-22)
-- =================================================================
-- Group 13, 15, 17: Mắm chấm
(33, 1, 'Mắm tôm (Pha sẵn)', 0, 13), (34, 1, 'Nước mắm chua ngọt', 0, 13), (35, 1, 'Nước tương', 0, 13),
(36, 1, 'Mắm tôm (Pha sẵn)', 0, 15), (37, 1, 'Nước mắm chua ngọt', 0, 15), (38, 1, 'Nước tương', 0, 15),
(39, 1, 'Mắm tôm (Pha sẵn)', 0, 17), (40, 1, 'Nước mắm chua ngọt', 0, 17), (41, 1, 'Nước tương', 0, 17),

-- Group 14, 16, 18: Rau sống
(42, 1, 'Lấy rau đầy đủ', 0, 14), (43, 1, 'Không lấy rau', 0, 14),
(44, 1, 'Lấy rau đầy đủ', 0, 16), (45, 1, 'Không lấy rau', 0, 16),
(46, 1, 'Lấy rau đầy đủ', 0, 18), (47, 1, 'Không lấy rau', 0, 18),

-- Group 19, 20, 21: Tương chấm món thêm
(48, 1, 'Tương ớt', 0, 19), (49, 1, 'Tương cà', 0, 19),
(50, 1, 'Tương ớt', 0, 20), (51, 1, 'Tương cà', 0, 20),
(52, 1, 'Tương ớt', 0, 21), (53, 1, 'Tương đen', 0, 21),

-- Group 22: Lượng đá nước sấu
(54, 1, '100% Đá', 0, 22), (55, 1, '50% Đá', 0, 22), (56, 1, 'Không đá (Ly nhỏ)', 0, 22),

-- =================================================================
-- 3. PIZZA SINH VIÊN (Groups 23-32)
-- =================================================================
-- Group 23, 25, 27, 29: Kích thước
(57, 1, 'Size M (24cm)', 0, 23), (58, 1, 'Size L (30cm) (+50k)', 50000, 23),
(59, 1, 'Size M (24cm)', 0, 25), (60, 1, 'Size L (30cm) (+50k)', 50000, 25),
(61, 1, 'Size M (24cm)', 0, 27), (62, 1, 'Size L (30cm) (+50k)', 50000, 27),
(63, 1, 'Size M (24cm)', 0, 29), (64, 1, 'Size L (30cm) (+50k)', 50000, 29),

-- Group 24, 26, 28, 30: Đế bánh
(65, 1, 'Đế dày xốp', 0, 24), (66, 1, 'Đế mỏng giòn', 0, 24),
(67, 1, 'Đế dày xốp', 0, 26), (68, 1, 'Đế mỏng giòn', 0, 26),
(69, 1, 'Đế dày xốp', 0, 28), (70, 1, 'Đế mỏng giòn', 0, 28),
(71, 1, 'Đế dày xốp', 0, 30), (72, 1, 'Đế mỏng giòn', 0, 30),

-- Group 31: Phô mai bột mỳ ý
(73, 1, 'Lấy phô mai bột', 0, 31), (74, 1, 'Không lấy', 0, 31),

-- Group 32: Nước ngọt combo
(75, 1, 'Coca Cola', 0, 32), (76, 1, 'Pepsi', 0, 32), (77, 1, '7Up', 0, 32),

-- =================================================================
-- 4. GÀ RÁN KTX (Groups 33-38)
-- =================================================================
-- Group 33: Phần gà
(78, 1, 'Gà ngẫu nhiên', 0, 33), (79, 1, 'Đùi gà', 0, 33), (80, 1, 'Má đùi', 0, 33), (81, 1, 'Cánh gà', 0, 33),

-- Group 34: Tương chấm
(82, 1, 'Tương ớt', 0, 34), (83, 1, 'Tương cà', 0, 34),

-- Group 35: Độ cay gà
(84, 1, 'Không cay', 0, 35), (85, 1, 'Cay vừa', 0, 35), (86, 1, 'Cay xé lưỡi', 0, 35),

-- Group 36, 38: Bột phô mai
(87, 1, 'Rắc bột phô mai (+5k)', 5000, 36), (88, 1, 'Không rắc', 0, 36),
(89, 1, 'Nhiều bột phô mai (+5k)', 5000, 38), (90, 1, 'Bình thường', 0, 38),

-- Group 37: Tương chấm khoai
(91, 1, 'Tương ớt', 0, 37), (92, 1, 'Tương cà', 0, 37), (93, 1, 'Sốt Mayonnaise', 0, 37),

-- =================================================================
-- 5. BISTRO ÂU LẠC (Groups 39-45)
-- =================================================================
-- Group 39: Độ chín Steak
(94, 1, 'Rare (Tái)', 0, 39), (95, 1, 'Medium Rare (Tái chín)', 0, 39),
(96, 1, 'Medium (Chín vừa)', 0, 39), (97, 1, 'Well Done (Chín kỹ)', 0, 39),

-- Group 40: Sốt Steak
(98, 1, 'Sốt tiêu đen', 0, 40), (99, 1, 'Sốt nấm', 0, 40), (100, 1, 'Sốt rượu vang', 0, 40),

-- Group 41: Ăn kèm Steak
(101, 1, 'Khoai tây chiên', 0, 41), (102, 1, 'Bánh mì tươi', 0, 41), (103, 1, 'Khoai nghiền', 0, 41),

-- Group 42: Loại sợi mỳ
(104, 1, 'Sợi tròn (Spaghetti)', 0, 42), (105, 1, 'Sợi dẹt (Fettuccine)', 0, 42),

-- Group 43: Độ cay Mỳ
(106, 1, 'Không cay', 0, 43), (107, 1, 'Cay ít', 0, 43), (108, 1, 'Cay nhiều', 0, 43),

-- Group 44, 45: Salad/Soup
(109, 1, 'Sốt mè rang', 0, 44), (110, 1, 'Sốt dấm chua ngọt', 0, 44),
(111, 1, 'Lấy bánh mì bơ tỏi (+5k)', 5000, 45),

-- =================================================================
-- 6. PHỞ BÒ 24H (Groups 46-54)
-- =================================================================
-- Group 46, 48, 50: Bánh phở
(112, 1, 'Bánh phở nhỏ (Truyền thống)', 0, 46), (113, 1, 'Bánh phở to', 0, 46),
(114, 1, 'Bánh phở nhỏ', 0, 48), (115, 1, 'Bánh phở to', 0, 48),
(116, 1, 'Bánh phở nhỏ', 0, 50), (117, 1, 'Bánh phở to', 0, 50),

-- Group 47, 49, 51: Hành rau
(118, 1, 'Bình thường', 0, 47), (119, 1, 'Không hành', 0, 47), (120, 1, 'Nước béo', 0, 47),
(121, 1, 'Bình thường', 0, 49), (122, 1, 'Không hành', 0, 49),
(123, 1, 'Bình thường', 0, 51), (124, 1, 'Không hành', 0, 51),

-- Group 52: Da gà
(125, 1, 'Lấy da', 0, 52), (126, 1, 'Không lấy da', 0, 52),

-- Group 53, 54: Quẩy/Trứng
(127, 1, '1 Chén (3 cái)', 0, 53), (128, 1, '2 Chén (6 cái) (+5k)', 5000, 53),
(129, 1, 'Trần lòng đào', 0, 54), (130, 1, 'Trần chín', 0, 54),

-- =================================================================
-- 7. CƠM MẸ NẤU B3 (Groups 55-60)
-- =================================================================
-- Group 55, 56, 57, 58: Canh hôm nay
(131, 1, 'Canh rau ngót', 0, 55), (132, 1, 'Canh chua', 0, 55),
(133, 1, 'Canh rau ngót', 0, 56), (134, 1, 'Canh chua', 0, 56),
(135, 1, 'Canh rau ngót', 0, 57), (136, 1, 'Canh chua', 0, 57),
(137, 1, 'Canh rau ngót', 0, 58), (138, 1, 'Canh chua', 0, 58),

-- Group 59, 60: Dụng cụ canh
(139, 1, 'Lấy muỗng', 0, 59), (140, 1, 'Không lấy muỗng', 0, 59),
(141, 1, 'Lấy muỗng', 0, 60), (142, 1, 'Không lấy muỗng', 0, 60),

-- =================================================================
-- 8. GÀ RÁN OPPA (Groups 61-67)
-- =================================================================
-- Group 61: Cấp độ cay
(143, 1, 'Cay ít', 0, 61), (144, 1, 'Cay vừa', 0, 61), (145, 1, 'Siêu cay', 0, 61),

-- Group 62, 63, 64: Đồ chua
(146, 1, 'Củ cải muối', 0, 62), (147, 1, 'Kimchi', 0, 62),
(148, 1, 'Củ cải muối', 0, 63), (149, 1, 'Kimchi', 0, 63),
(150, 1, 'Củ cải muối', 0, 64), (151, 1, 'Kimchi', 0, 64),

-- Group 65: Sốt thêm cơm
(152, 1, 'Thêm sốt Teriyaki', 0, 65), (153, 1, 'Không thêm', 0, 65),

-- Group 66: Topping Tokbokki
(154, 1, 'Phủ phô mai (+10k)', 10000, 66), (155, 1, 'Chả cá Hàn Quốc (+10k)', 10000, 66), (156, 1, 'Trứng luộc (+5k)', 5000, 66),

-- =================================================================
-- 9. THE PIZZA HOUSE (Groups 68-75)
-- =================================================================
-- Group 68, 70, 72: Size
(157, 1, 'Size 9 inch (M)', 0, 68), (158, 1, 'Size 12 inch (L) (+60k)', 60000, 68),
(159, 1, 'Size 9 inch (M)', 0, 70), (160, 1, 'Size 12 inch (L) (+60k)', 60000, 70),
(161, 1, 'Size 9 inch (M)', 0, 72), (162, 1, 'Size 12 inch (L) (+60k)', 60000, 72),

-- Group 69, 71, 73: Viền
(163, 1, 'Viền phô mai (+30k)', 30000, 69), (164, 1, 'Viền xúc xích (+35k)', 35000, 69),
(165, 1, 'Viền phô mai (+30k)', 30000, 71), (166, 1, 'Viền xúc xích (+35k)', 35000, 71),
(167, 1, 'Viền phô mai (+30k)', 30000, 73), (168, 1, 'Viền xúc xích (+35k)', 35000, 73),

-- Group 74, 75: Sốt chấm
(169, 1, 'Tương cà', 0, 74), (170, 1, 'Tương ớt', 0, 74), (171, 1, 'Sốt Cocktail', 0, 74),
(172, 1, 'Tương cà', 0, 75), (173, 1, 'Tương ớt', 0, 75), (174, 1, 'Sốt TarTar', 0, 75),

-- =================================================================
-- 10. BÁNH MÌ CHẢO 3 NGON (Groups 76-82)
-- =================================================================
-- Group 76, 80: Độ chín trứng
(175, 1, 'Lòng đào', 0, 76), (176, 1, 'Chín kỹ', 0, 76),
(177, 1, 'Lòng đào', 0, 80), (178, 1, 'Chín kỹ', 0, 80),

-- Group 77: Bánh mì thêm
(179, 1, 'Thêm 1 ổ (+4k)', 4000, 77), (180, 1, 'Thêm 2 ổ (+8k)', 8000, 77),

-- Group 78, 79: Độ chín bò/cay
(181, 1, 'Không cay', 0, 78), (182, 1, 'Có cay (Sa tế)', 0, 78),
(183, 1, 'Bò tái', 0, 79), (184, 1, 'Bò chín', 0, 79),

-- Group 81, 82: Nước uống
(185, 1, 'Dùng lạnh (Đá)', 0, 81), (186, 1, 'Dùng nóng', 0, 81),
(187, 1, 'Dùng lạnh (Đá)', 0, 82), (188, 1, 'Dùng nóng', 0, 82),

-- =================================================================
-- 11. BÚN BÒ O NỞ (Groups 83-88)
-- =================================================================
-- Group 83, 85, 86: Rau
(189, 1, 'Rau sống', 0, 83), (190, 1, 'Rau trụng', 0, 83), (191, 1, 'Không rau', 0, 83),
(192, 1, 'Rau sống', 0, 85), (193, 1, 'Rau trụng', 0, 85),
(194, 1, 'Rau sống', 0, 86), (195, 1, 'Rau trụng', 0, 86),

-- Group 84: Sa tế
(196, 1, 'Để sa tế trong tô', 0, 84), (197, 1, 'Để sa tế riêng', 0, 84), (198, 1, 'Không lấy sa tế', 0, 84),

-- Group 87: Độ tái
(199, 1, 'Tái sống', 0, 87), (200, 1, 'Tái chín', 0, 87),

-- Group 88: Nước mía
(201, 1, 'Nhiều đá', 0, 88), (202, 1, 'Ít đá', 0, 88),

-- =================================================================
-- 12. ĂN VẶT E1 (Groups 89-96)
-- =================================================================
-- Group 89: Đường
(203, 1, '100% Đường', 0, 89), (204, 1, '70% Đường', 0, 89), (205, 1, '50% Đường', 0, 89), (206, 1, '30% Đường', 0, 89),

-- Group 90, 92: Đá
(207, 1, '100% Đá', 0, 90), (208, 1, '50% Đá', 0, 90), (209, 1, 'Không đá (đầy ly) (+5k)', 5000, 90),
(210, 1, '100% Đá', 0, 92), (211, 1, '50% Đá', 0, 92),

-- Group 91: Topping
(212, 1, 'Trân châu đen (+5k)', 5000, 91), (213, 1, 'Trân châu trắng (+7k)', 7000, 91), (214, 1, 'Pudding trứng (+5k)', 5000, 91),

-- Group 93, 94, 95: Tương chấm xiên
(215, 1, 'Tương ớt', 0, 93), (216, 1, 'Tương đen', 0, 93),
(217, 1, 'Tương ớt', 0, 94), (218, 1, 'Tương đen', 0, 94),
(219, 1, 'Tương ớt', 0, 95), (220, 1, 'Tương cà', 0, 95),

-- Group 96: Vị gà lắc
(221, 1, 'Phô mai', 0, 96), (222, 1, 'Xí muội', 0, 96),

-- =================================================================
-- 13. HỦ TIẾU MÌ ĐÊM (Groups 97-101)
-- =================================================================
-- Group 97, 99: Sợi
(223, 1, 'Hủ tiếu dai', 0, 97), (224, 1, 'Hủ tiếu mềm', 0, 97), (225, 1, 'Mì tươi', 0, 97),
(226, 1, 'Hủ tiếu dai', 0, 99), (227, 1, 'Hủ tiếu mềm', 0, 99),

-- Group 98: Khô nước
(228, 1, 'Ăn nước', 0, 98), (229, 1, 'Ăn khô (kèm chén súp)', 0, 98),

-- Group 100, 101: Hanh/Sốt
(230, 1, 'Có hành hẹ', 0, 100), (231, 1, 'Không hành hẹ', 0, 100),
(232, 1, 'Sốt dầu hào', 0, 101), (233, 1, 'Sốt tương đen', 0, 101),

-- =================================================================
-- 14. BIG SIZE PIZZA (Groups 102-106)
-- =================================================================
-- Group 102, 103: Mix vị
(234, 1, 'Bò BBQ', 0, 102), (235, 1, 'Gà nướng nấm', 0, 102), (236, 1, 'Hải sản', 0, 102),
(237, 1, 'Xúc xích', 0, 103), (238, 1, 'Thập cẩm', 0, 103), (239, 1, 'Phô mai', 0, 103),

-- Group 104: Sốt Salad
(240, 1, 'Sốt Ceasar', 0, 104), (241, 1, 'Sốt dầu giấm', 0, 104),

-- Group 105: Độ cay cánh gà
(242, 1, 'Không cay', 0, 105), (243, 1, 'Cay vừa', 0, 105),

-- Group 106: Ly đá
(244, 1, 'Lấy 2 ly đá', 0, 106), (245, 1, 'Lấy 4 ly đá', 0, 106), (246, 1, 'Không lấy ly', 0, 106),

-- =================================================================
-- 15. CƠM GÀ XỐI MỠ 123 (Groups 107-112)
-- =================================================================
-- Group 107: Phần thịt
(247, 1, 'Đùi gà góc tư (+5k)', 5000, 107), (248, 1, 'Cánh gà', 0, 107), (249, 1, 'Ức gà', 0, 107),

-- Group 108: Nước chấm
(250, 1, 'Nước mắm gừng', 0, 108), (251, 1, 'Tương ớt', 0, 108), (252, 1, 'Nước tương', 0, 108),

-- Group 109, 110, 111, 112: Linh tinh
(253, 1, 'Lấy tương ớt', 0, 109), (254, 1, 'Không lấy', 0, 109),
(255, 1, 'Lấy canh rong biển', 0, 110), (256, 1, 'Không lấy canh', 0, 110),
(257, 1, 'Bò chín kỹ', 0, 111), (258, 1, 'Bò vừa chín tới', 0, 111),
(259, 1, 'Lấy muỗng đũa', 0, 112), (260, 1, 'Không lấy', 0, 112);