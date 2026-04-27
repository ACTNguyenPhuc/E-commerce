-- =====================================================================
-- V2__seed_basic_data.sql
-- Dữ liệu khởi tạo: admin + shipping methods + categories + brands
-- Mật khẩu admin: 'Admin@123' (bcrypt strength 12)
-- =====================================================================

INSERT INTO users (id, email, phone, password_hash, full_name, role, status, email_verified_at) VALUES
(1, 'admin@shop.vn',   '0901000001', '$2a$12$VxL5Vh3eZ8TCxJpOQUjuEel8w/3xRi5L5tjFw5L4yBflKvHnnY8B6', 'Quản trị viên', 'admin', 'active', NOW()),
(2, 'staff01@shop.vn', '0901000002', '$2a$12$VxL5Vh3eZ8TCxJpOQUjuEel8w/3xRi5L5tjFw5L4yBflKvHnnY8B6', 'Nhân viên 01',  'staff', 'active', NOW());

INSERT INTO shipping_methods (code, name, description, base_fee, estimated_days_min, estimated_days_max, status) VALUES
('GHN_FAST',  'Giao hàng nhanh (GHN)',     'Giao trong 1-2 ngày',       35000, 1, 2, 'active'),
('GHN_SAVER', 'Giao hàng tiết kiệm (GHN)', 'Giao trong 3-5 ngày',       22000, 3, 5, 'active'),
('GHTK',      'Giao hàng tiết kiệm',       'GHTK chuẩn',                 25000, 2, 4, 'active'),
('EXPRESS',   'Hoả tốc',                   'Giao trong ngày nội thành',  60000, 0, 1, 'active'),
('PICKUP',    'Nhận tại cửa hàng',         'Khách tự đến lấy',               0, 0, 1, 'active');

INSERT INTO categories (id, parent_id, name, slug, description, display_order, status) VALUES
(1, NULL, 'Thời trang',         'thoi-trang',          'Thời trang nam nữ',     1, 'active'),
(2, NULL, 'Điện tử',            'dien-tu',             'Đồ điện tử công nghệ',  2, 'active'),
(3, NULL, 'Đồ gia dụng',        'do-gia-dung',         'Vật dụng cho gia đình', 3, 'active'),
(4, 1,    'Áo thun',             'ao-thun',             'Áo thun nam nữ',        1, 'active'),
(5, 1,    'Quần jeans',          'quan-jeans',          'Quần jean cao cấp',     2, 'active'),
(6, 2,    'Điện thoại di động',  'dien-thoai-di-dong',  'Smartphone các hãng',   1, 'active'),
(7, 2,    'Laptop',              'laptop',              'Laptop văn phòng / gaming', 2, 'active');

INSERT INTO brands (name, slug, description, status) VALUES
('Uniqlo',  'uniqlo',  'Thương hiệu thời trang Nhật Bản', 'active'),
('Levi''s', 'levis',   'Thương hiệu jeans nổi tiếng',     'active'),
('Apple',   'apple',   'Thương hiệu công nghệ Mỹ',        'active'),
('Samsung', 'samsung', 'Thương hiệu công nghệ Hàn Quốc',  'active'),
('Dell',    'dell',    'Thương hiệu máy tính Mỹ',         'active');

INSERT INTO product_attributes (id, name, slug) VALUES
(1, 'Màu sắc',    'mau-sac'),
(2, 'Kích thước', 'kich-thuoc'),
(3, 'Dung lượng', 'dung-luong');
