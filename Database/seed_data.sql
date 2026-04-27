-- =====================================================================
--  E-COMMERCE - DỮ LIỆU MẪU
--  Lưu ý: chạy SAU schema.sql
--  Mật khẩu mẫu: '123456' (đã hash bằng bcrypt - chỉ minh hoạ)
-- =====================================================================

USE ecommerce_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. USERS
-- ---------------------------------------------------------------------
INSERT INTO users (id, email, phone, password_hash, full_name, gender, role, status, email_verified_at) VALUES
(1, 'admin@shop.vn',     '0901000001', '$2y$10$abcdefghijklmnopqrstuv1', 'Nguyễn Quản Trị',   'male',   'admin',    'active', NOW()),
(2, 'staff01@shop.vn',   '0901000002', '$2y$10$abcdefghijklmnopqrstuv2', 'Trần Nhân Viên',    'female', 'staff',    'active', NOW()),
(3, 'an.nguyen@gmail.com',  '0912345678', '$2y$10$abcdefghijklmnopqrstuv3', 'Nguyễn Văn An',     'male',   'customer', 'active', NOW()),
(4, 'binh.tran@gmail.com',  '0922345678', '$2y$10$abcdefghijklmnopqrstuv4', 'Trần Thị Bình',     'female', 'customer', 'active', NOW()),
(5, 'cuong.le@gmail.com',   '0932345678', '$2y$10$abcdefghijklmnopqrstuv5', 'Lê Văn Cường',      'male',   'customer', 'active', NULL);

-- ---------------------------------------------------------------------
-- 2. USER_ADDRESSES
-- ---------------------------------------------------------------------
INSERT INTO user_addresses (user_id, recipient_name, recipient_phone, province, district, ward, street_address, address_type, is_default) VALUES
(3, 'Nguyễn Văn An',  '0912345678', 'Hà Nội',         'Cầu Giấy',  'Dịch Vọng',     '123 Trần Thái Tông',          'home',   1),
(3, 'Nguyễn Văn An',  '0912345678', 'Hà Nội',         'Đống Đa',   'Láng Hạ',       'Tầng 5, 99 Láng Hạ',          'office', 0),
(4, 'Trần Thị Bình',  '0922345678', 'TP. Hồ Chí Minh','Quận 1',    'Bến Nghé',      '12 Nguyễn Huệ',               'home',   1),
(4, 'Mẹ chị Bình',    '0922345670', 'TP. Hồ Chí Minh','Quận 7',    'Tân Phong',     '88 Nguyễn Thị Thập',          'home',   0),
(5, 'Lê Văn Cường',   '0932345678', 'Đà Nẵng',        'Hải Châu',  'Thạch Thang',   '45 Bạch Đằng',                'home',   1);

-- ---------------------------------------------------------------------
-- 3. CATEGORIES
-- ---------------------------------------------------------------------
INSERT INTO categories (id, parent_id, name, slug, description, display_order, status) VALUES
(1, NULL, 'Thời trang',           'thoi-trang',           'Thời trang nam nữ', 1, 'active'),
(2, NULL, 'Điện tử',              'dien-tu',              'Đồ điện tử công nghệ', 2, 'active'),
(3, NULL, 'Đồ gia dụng',          'do-gia-dung',          'Vật dụng cho gia đình', 3, 'active'),
(4, 1,    'Áo thun',              'ao-thun',              'Áo thun nam nữ', 1, 'active'),
(5, 1,    'Quần jeans',           'quan-jeans',           'Quần jean cao cấp', 2, 'active'),
(6, 2,    'Điện thoại di động',   'dien-thoai-di-dong',   'Smartphone các hãng', 1, 'active'),
(7, 2,    'Laptop',               'laptop',               'Laptop văn phòng / gaming', 2, 'active');

-- ---------------------------------------------------------------------
-- 4. BRANDS
-- ---------------------------------------------------------------------
INSERT INTO brands (id, name, slug, description, status) VALUES
(1, 'Uniqlo',  'uniqlo',  'Thương hiệu thời trang Nhật Bản', 'active'),
(2, 'Levi''s', 'levis',   'Thương hiệu jeans nổi tiếng',     'active'),
(3, 'Apple',   'apple',   'Thương hiệu công nghệ Mỹ',        'active'),
(4, 'Samsung', 'samsung', 'Thương hiệu công nghệ Hàn Quốc',  'active'),
(5, 'Dell',    'dell',    'Thương hiệu máy tính Mỹ',         'active');

-- ---------------------------------------------------------------------
-- 5. PRODUCTS
-- ---------------------------------------------------------------------
INSERT INTO products (id, category_id, brand_id, sku, name, slug, short_description, description, base_price, sale_price, thumbnail_url, status, is_featured) VALUES
(1, 4, 1, 'TS-UNQ-001', 'Áo thun Uniqlo Cotton',     'ao-thun-uniqlo-cotton',    'Áo thun nam Uniqlo chất cotton 100%',     'Áo thun Uniqlo cotton mềm mại, thoáng mát, phù hợp mặc hàng ngày.', 250000, 199000, '/images/products/ts-uniqlo.jpg',     'active', 1),
(2, 5, 2, 'JN-LV-501',  'Quần jeans Levis 501',      'quan-jeans-levis-501',     'Quần jeans Levis 501 huyền thoại',        'Quần jeans Levis 501 form straight cổ điển.',                       890000, NULL,   '/images/products/jeans-levis.jpg',   'active', 1),
(3, 6, 3, 'SP-IP15-PRO','iPhone 15 Pro 256GB',       'iphone-15-pro-256gb',      'iPhone 15 Pro chip A17 Pro, RAM 8GB',     'iPhone 15 Pro với khung Titan, camera 48MP, chip A17 Pro mạnh mẽ.', 28990000, 27990000, '/images/products/iphone15pro.jpg', 'active', 1),
(4, 6, 4, 'SP-SS-S24',  'Samsung Galaxy S24 Ultra',  'samsung-galaxy-s24-ultra', 'Galaxy S24 Ultra với S-Pen',              'Galaxy S24 Ultra camera 200MP, màn AMOLED 6.8 inch.',               31990000, 29990000, '/images/products/galaxys24.jpg',   'active', 0),
(5, 7, 5, 'LP-DELL-XPS','Dell XPS 13 9340',          'dell-xps-13-9340',         'Laptop Dell XPS 13 cao cấp',              'Dell XPS 13 màn 13.4" OLED, Core Ultra 7, RAM 16GB.',               35990000, 33990000, '/images/products/dell-xps13.jpg',  'active', 0);

-- ---------------------------------------------------------------------
-- 6. PRODUCT_IMAGES
-- ---------------------------------------------------------------------
INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary) VALUES
(1, '/images/products/ts-uniqlo-1.jpg', 'Áo thun Uniqlo mặt trước', 1, 1),
(1, '/images/products/ts-uniqlo-2.jpg', 'Áo thun Uniqlo mặt sau',   2, 0),
(2, '/images/products/jeans-levis-1.jpg', 'Levis 501',              1, 1),
(3, '/images/products/iphone15pro-1.jpg', 'iPhone 15 Pro Front',    1, 1),
(3, '/images/products/iphone15pro-2.jpg', 'iPhone 15 Pro Back',     2, 0),
(4, '/images/products/galaxys24-1.jpg', 'Galaxy S24 Ultra',         1, 1),
(5, '/images/products/dell-xps13-1.jpg', 'Dell XPS 13',             1, 1);

-- ---------------------------------------------------------------------
-- 7. PRODUCT_ATTRIBUTES & VALUES
-- ---------------------------------------------------------------------
INSERT INTO product_attributes (id, name, slug) VALUES
(1, 'Màu sắc',     'mau-sac'),
(2, 'Kích thước',  'kich-thuoc'),
(3, 'Dung lượng',  'dung-luong');

INSERT INTO product_attribute_values (id, attribute_id, value, color_code, display_order) VALUES
-- Màu sắc
(1, 1, 'Đen',         '#000000', 1),
(2, 1, 'Trắng',       '#FFFFFF', 2),
(3, 1, 'Xanh navy',   '#1E2A78', 3),
(4, 1, 'Titan tự nhiên','#8E8E93', 4),
-- Kích thước
(5, 2, 'S', NULL, 1),
(6, 2, 'M', NULL, 2),
(7, 2, 'L', NULL, 3),
(8, 2, 'XL', NULL, 4),
-- Dung lượng
(9,  3, '128GB',  NULL, 1),
(10, 3, '256GB',  NULL, 2),
(11, 3, '512GB',  NULL, 3);

-- ---------------------------------------------------------------------
-- 8. PRODUCT_VARIANTS (sản phẩm có nhiều biến thể)
-- ---------------------------------------------------------------------
-- Áo thun Uniqlo: Đen-S, Đen-M, Trắng-M, Trắng-L
INSERT INTO product_variants (id, product_id, sku, price, sale_price, stock_quantity, weight_gram, status) VALUES
(1, 1, 'TS-UNQ-001-BK-S', 250000, 199000, 50,  200, 'active'),
(2, 1, 'TS-UNQ-001-BK-M', 250000, 199000, 80,  220, 'active'),
(3, 1, 'TS-UNQ-001-WH-M', 250000, 199000, 60,  220, 'active'),
(4, 1, 'TS-UNQ-001-WH-L', 250000, 199000, 40,  240, 'active'),
-- Levi's 501: navy size 30, 32
(5, 2, 'JN-LV-501-NV-30', 890000, NULL,    25,  600, 'active'),
(6, 2, 'JN-LV-501-NV-32', 890000, NULL,    30,  620, 'active'),
-- iPhone 15 Pro: Titan tự nhiên 256GB & 512GB
(7, 3, 'SP-IP15-PRO-TT-256', 28990000, 27990000, 15, 187, 'active'),
(8, 3, 'SP-IP15-PRO-TT-512', 32990000, 31490000, 8,  187, 'active'),
-- Samsung S24 Ultra: Đen 256GB
(9, 4, 'SP-SS-S24-BK-256',   31990000, 29990000, 12, 232, 'active'),
-- Dell XPS 13: 1 biến thể duy nhất (vẫn dùng variant để chuẩn hoá)
(10, 5, 'LP-DELL-XPS-DEFAULT', 35990000, 33990000, 5, 1200, 'active');

-- Liên kết variant <-> attribute_value (N-N)
INSERT INTO product_variant_values (variant_id, attribute_value_id) VALUES
(1, 1), (1, 5),     -- Đen + S
(2, 1), (2, 6),     -- Đen + M
(3, 2), (3, 6),     -- Trắng + M
(4, 2), (4, 7),     -- Trắng + L
(5, 3),             -- Navy (chỉ 1 thuộc tính cho ví dụ)
(6, 3),             -- Navy
(7, 4), (7, 10),    -- Titan + 256GB
(8, 4), (8, 11),    -- Titan + 512GB
(9, 1), (9, 10);    -- Đen + 256GB

-- ---------------------------------------------------------------------
-- 9. CARTS & CART_ITEMS
-- ---------------------------------------------------------------------
-- Cart 1: của khách đăng ký user_id=4
-- Cart 2: của guest (chỉ session_id)
-- Cart 3: của user_id=5 đã chuyển thành đơn hàng
INSERT INTO carts (id, user_id, session_id, status) VALUES
(1, 4,    NULL,                   'active'),
(2, NULL, 'sess_guest_abc123xyz', 'active'),
(3, 5,    NULL,                   'converted');

INSERT INTO cart_items (cart_id, product_id, variant_id, quantity, unit_price) VALUES
(1, 1, 2, 2, 199000),
(1, 2, 6, 1, 890000),
(2, 1, 3, 1, 199000),
(2, 4, 9, 1, 29990000),
(3, 5, 10, 1, 33990000);

-- ---------------------------------------------------------------------
-- 10. VOUCHERS
-- ---------------------------------------------------------------------
INSERT INTO vouchers (id, code, name, description, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, usage_limit_per_user, start_date, end_date, status) VALUES
(1, 'WELCOME10',   'Giảm 10% cho khách mới',   'Áp dụng đơn đầu tiên', 'percentage',  10.00,    200000,  100000, 1000, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'active'),
(2, 'FREESHIP',    'Miễn phí ship 30K',         'Free ship cho đơn từ 300K', 'fixed_amount', 30000,    300000,  NULL,    NULL, 1, '2026-01-01 00:00:00', '2026-06-30 23:59:59', 'active'),
(3, 'SUMMER50',    'Giảm 50K hè 2026',          'Áp dụng đơn từ 500K',       'fixed_amount', 50000,    500000,  NULL,    500,  2, '2026-05-01 00:00:00', '2026-08-31 23:59:59', 'active'),
(4, 'VIP15',       'Giảm 15% cho VIP',          'Tối đa 500K cho 1 đơn',     'percentage',  15.00,   1000000,  500000, 100,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'active'),
(5, 'EXPIRED20',   'Voucher đã hết hạn',        'Voucher mẫu đã hết hạn',    'percentage',  20.00,   100000,  50000,  100,  1, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'expired');

-- ---------------------------------------------------------------------
-- 11. SHIPPING_METHODS
-- ---------------------------------------------------------------------
INSERT INTO shipping_methods (id, code, name, description, base_fee, estimated_days_min, estimated_days_max, status) VALUES
(1, 'GHN_FAST',    'Giao hàng nhanh (GHN)',     'Giao trong 1-2 ngày',     35000, 1, 2, 'active'),
(2, 'GHN_SAVER',   'Giao hàng tiết kiệm (GHN)', 'Giao trong 3-5 ngày',     22000, 3, 5, 'active'),
(3, 'GHTK',        'Giao hàng tiết kiệm',       'GHTK chuẩn',              25000, 2, 4, 'active'),
(4, 'EXPRESS',     'Hoả tốc',                   'Giao trong ngày nội thành', 60000, 0, 1, 'active'),
(5, 'PICKUP',      'Nhận tại cửa hàng',         'Khách tự đến lấy',           0, 0, 1, 'active');

-- ---------------------------------------------------------------------
-- 12. ORDERS, ORDER_ITEMS & STATUS HISTORY
-- ---------------------------------------------------------------------
INSERT INTO orders (id, order_code, user_id, voucher_id, shipping_method_id, recipient_name, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, payment_method, payment_status, status, note, placed_at, confirmed_at, delivered_at) VALUES
(1, 'ORD-20260415-0001', 3, 1,    1, 'Nguyễn Văn An', '0912345678', '123 Trần Thái Tông, Dịch Vọng, Cầu Giấy, Hà Nội',           1288000,  35000,  100000, 1223000, 'cod',         'paid',    'delivered', 'Giao giờ hành chính', '2026-04-15 10:00:00', '2026-04-15 10:30:00', '2026-04-16 15:00:00'),
(2, 'ORD-20260418-0002', 4, 2,    2, 'Trần Thị Bình', '0922345678', '12 Nguyễn Huệ, Bến Nghé, Quận 1, TP. Hồ Chí Minh',           890000,  22000,   30000,  882000, 'momo',        'paid',    'shipping',  NULL,                  '2026-04-18 09:15:00', '2026-04-18 09:45:00', NULL),
(3, 'ORD-20260420-0003', 5, NULL, 1, 'Lê Văn Cường',  '0932345678', '45 Bạch Đằng, Thạch Thang, Hải Châu, Đà Nẵng',             33990000,  35000,       0, 34025000, 'vnpay',       'paid',    'completed', 'Đã đóng hộp gỗ',      '2026-04-20 14:00:00', '2026-04-20 14:10:00', '2026-04-22 10:00:00'),
(4, 'ORD-20260422-0004', 3, 4,    4, 'Nguyễn Văn An', '0912345678', '123 Trần Thái Tông, Dịch Vọng, Cầu Giấy, Hà Nội',          27990000,  60000, 4198500, 23851500, 'credit_card', 'pending', 'confirmed', NULL,                  '2026-04-22 16:00:00', '2026-04-22 16:30:00', NULL),
(5, 'ORD-20260424-0005', 4, NULL, 3, 'Trần Thị Bình', '0922345678', '12 Nguyễn Huệ, Bến Nghé, Quận 1, TP. Hồ Chí Minh',           199000,  25000,       0,  224000, 'cod',         'pending', 'pending',   NULL,                  '2026-04-24 11:30:00', NULL,                NULL);

INSERT INTO order_items (order_id, product_id, variant_id, product_name, variant_name, sku, unit_price, quantity, subtotal) VALUES
(1, 1, 2, 'Áo thun Uniqlo Cotton',    'Đen / M',           'TS-UNQ-001-BK-M',  199000, 2,  398000),
(1, 2, 6, 'Quần jeans Levis 501',     'Xanh navy / 32',    'JN-LV-501-NV-32',  890000, 1,  890000),
(2, 2, 6, 'Quần jeans Levis 501',     'Xanh navy / 32',    'JN-LV-501-NV-32',  890000, 1,  890000),
(3, 5, 10,'Dell XPS 13 9340',         'Mặc định',          'LP-DELL-XPS-DEFAULT', 33990000, 1, 33990000),
(4, 3, 7, 'iPhone 15 Pro 256GB',      'Titan tự nhiên / 256GB', 'SP-IP15-PRO-TT-256', 27990000, 1, 27990000),
(5, 1, 3, 'Áo thun Uniqlo Cotton',    'Trắng / M',         'TS-UNQ-001-WH-M',  199000, 1,  199000);

-- Lịch sử trạng thái đơn hàng
INSERT INTO order_status_history (order_id, from_status, to_status, note, changed_by, created_at) VALUES
(1, NULL,         'pending',   'Khách đặt đơn',        3, '2026-04-15 10:00:00'),
(1, 'pending',    'confirmed', 'Admin xác nhận đơn',   1, '2026-04-15 10:30:00'),
(1, 'confirmed',  'processing','Đang đóng gói',        2, '2026-04-15 13:00:00'),
(1, 'processing', 'shipping',  'Đã giao đơn vị vận chuyển', 2, '2026-04-15 17:00:00'),
(1, 'shipping',   'delivered', 'Khách đã nhận hàng',   NULL, '2026-04-16 15:00:00'),

(2, NULL,         'pending',   'Khách đặt đơn',        4, '2026-04-18 09:15:00'),
(2, 'pending',    'confirmed', 'Xác nhận tự động',     1, '2026-04-18 09:45:00'),
(2, 'confirmed',  'processing','Đóng gói',             2, '2026-04-18 10:30:00'),
(2, 'processing', 'shipping',  'Bàn giao GHN',         2, '2026-04-19 08:00:00'),

(3, NULL,         'pending',   'Khách đặt đơn',        5, '2026-04-20 14:00:00'),
(3, 'pending',    'confirmed', 'VNPay xác nhận thanh toán', 1, '2026-04-20 14:10:00'),
(3, 'confirmed',  'processing','Đóng gói cẩn thận',    2, '2026-04-20 16:00:00'),
(3, 'processing', 'shipping',  'Đã ship',              2, '2026-04-21 09:00:00'),
(3, 'shipping',   'delivered', 'Đã giao',              NULL, '2026-04-22 10:00:00'),
(3, 'delivered',  'completed', 'Hoàn tất sau 1 ngày',  NULL, '2026-04-23 10:00:00'),

(4, NULL,         'pending',   'Khách đặt đơn',        3, '2026-04-22 16:00:00'),
(4, 'pending',    'confirmed', 'Đã xác nhận',          1, '2026-04-22 16:30:00'),

(5, NULL,         'pending',   'Khách đặt đơn',        4, '2026-04-24 11:30:00');

-- ---------------------------------------------------------------------
-- 13. PAYMENTS
-- ---------------------------------------------------------------------
INSERT INTO payments (order_id, payment_method, amount, transaction_code, status, gateway_response, paid_at) VALUES
(1, 'cod',         1223000,  NULL,                   'success', NULL,                                          '2026-04-16 15:00:00'),
(2, 'momo',         882000,  'MOMO_TXN_20260418_001','success', '{"resultCode":0,"message":"Success"}',        '2026-04-18 09:30:00'),
(3, 'vnpay',     34025000,  'VNP_TXN_20260420_999', 'success', '{"vnp_ResponseCode":"00","vnp_Amount":"34025000"}','2026-04-20 14:08:00'),
(4, 'credit_card',23851500, 'STRIPE_pi_3OoXY...',   'pending', '{"status":"requires_capture"}',               NULL),
(5, 'cod',          224000,  NULL,                   'pending', NULL,                                          NULL);

-- ---------------------------------------------------------------------
-- 14. SHIPMENTS
-- ---------------------------------------------------------------------
INSERT INTO shipments (order_id, shipping_method_id, tracking_number, carrier, status, shipping_fee, shipped_at, delivered_at) VALUES
(1, 1, 'GHN-VN-1000001', 'GHN',   'delivered',         35000, '2026-04-15 17:00:00', '2026-04-16 15:00:00'),
(2, 2, 'GHN-VN-1000002', 'GHN',   'in_transit',        22000, '2026-04-19 08:00:00', NULL),
(3, 1, 'GHN-VN-1000003', 'GHN',   'delivered',         35000, '2026-04-21 09:00:00', '2026-04-22 10:00:00'),
(4, 4, NULL,             NULL,    'preparing',         60000, NULL,                  NULL),
(5, 3, NULL,             NULL,    'preparing',         25000, NULL,                  NULL);

-- ---------------------------------------------------------------------
-- 15. REVIEWS
-- ---------------------------------------------------------------------
INSERT INTO reviews (product_id, user_id, order_item_id, rating, title, content, status) VALUES
(1, 3, 1, 5, 'Áo đẹp, vừa vặn',     'Vải mềm, mặc thoải mái, đáng tiền.',         'approved'),
(2, 3, 2, 4, 'Quần jean ổn',         'Form đẹp, hơi cứng nhưng giặt vài lần là mềm.', 'approved'),
(2, 4, 3, 5, 'Đúng hàng chính hãng', 'Mua đúng dịp sale, rất hài lòng.',           'approved'),
(5, 5, 4, 5, 'Laptop quá xịn',       'Màn hình OLED đẹp, máy mỏng nhẹ.',           'approved'),
(1, 5, NULL, 3, 'Bình thường',       'Mua tặng bạn, chưa thử trực tiếp.',          'pending');

-- ---------------------------------------------------------------------
-- 16. VOUCHER_USAGES (đồng bộ với orders đã dùng voucher)
-- ---------------------------------------------------------------------
INSERT INTO voucher_usages (voucher_id, user_id, order_id, discount_amount) VALUES
(1, 3, 1,  100000),
(2, 4, 2,   30000),
(4, 3, 4, 4198500);

UPDATE vouchers SET used_count = 1 WHERE id IN (1, 2, 4);

-- ---------------------------------------------------------------------
-- 17. WISHLISTS
-- ---------------------------------------------------------------------
INSERT INTO wishlists (user_id, product_id) VALUES
(3, 3),
(3, 5),
(4, 1),
(4, 4),
(5, 2);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- KẾT THÚC SEED
-- =====================================================================
