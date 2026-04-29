-- =====================================================================
--  E-COMMERCE DATABASE SCHEMA
--  MySQL 8.0+ | InnoDB | utf8mb4
--  Tác giả: E-commerce Project
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. TẠO DATABASE
-- ---------------------------------------------------------------------
DROP DATABASE IF EXISTS ecommerce_db;
CREATE DATABASE ecommerce_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- =====================================================================
-- 1. NGƯỜI DÙNG & ĐỊA CHỈ
-- =====================================================================

-- 1.1. Bảng users: lưu mọi loại người dùng (admin / staff / customer)
CREATE TABLE users (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email           VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20)     DEFAULT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    avatar_url      VARCHAR(2048)   DEFAULT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    gender          ENUM('male','female','other') DEFAULT NULL,
    date_of_birth   DATE            DEFAULT NULL,
    role            ENUM('admin','staff','customer') NOT NULL DEFAULT 'customer',
    status          ENUM('active','inactive','banned') NOT NULL DEFAULT 'active',
    email_verified_at TIMESTAMP     NULL DEFAULT NULL,
    last_login_at   TIMESTAMP       NULL DEFAULT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_phone (phone),
    KEY idx_users_role (role),
    KEY idx_users_status (status),
    KEY idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.2. Bảng user_addresses: hỗ trợ đa địa chỉ giao hàng (1-N với users)
CREATE TABLE user_addresses (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED NOT NULL,
    recipient_name  VARCHAR(100)    NOT NULL,
    recipient_phone VARCHAR(20)     NOT NULL,
    province        VARCHAR(100)    NOT NULL,
    district        VARCHAR(100)    NOT NULL,
    ward            VARCHAR(100)    NOT NULL,
    street_address  VARCHAR(255)    NOT NULL,
    address_type    ENUM('home','office','other') NOT NULL DEFAULT 'home',
    is_default      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_addr_user (user_id),
    KEY idx_addr_default (user_id, is_default),
    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 2. DANH MỤC, THƯƠNG HIỆU, SẢN PHẨM
-- =====================================================================

-- 2.1. categories: tự tham chiếu để hỗ trợ danh mục con (parent_id)
CREATE TABLE categories (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT UNSIGNED DEFAULT NULL,
    name            VARCHAR(150)    NOT NULL,
    slug            VARCHAR(180)    NOT NULL,
    description     TEXT,
    image_url       VARCHAR(2048)   DEFAULT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    display_order   INT             NOT NULL DEFAULT 0,
    status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug),
    KEY idx_cat_parent (parent_id),
    KEY idx_cat_status (status),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2. brands
CREATE TABLE brands (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    slug            VARCHAR(180)    NOT NULL,
    logo_url        VARCHAR(2048)   DEFAULT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    description     TEXT,
    status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_brands_slug (slug),
    KEY idx_brand_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3. products: thông tin sản phẩm cha
CREATE TABLE products (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id         BIGINT UNSIGNED NOT NULL,
    brand_id            BIGINT UNSIGNED DEFAULT NULL,
    sku                 VARCHAR(100)    NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    slug                VARCHAR(220)    NOT NULL,
    short_description   VARCHAR(500)    DEFAULT NULL,
    description         LONGTEXT,
    base_price          DECIMAL(15,2)   NOT NULL,
    sale_price          DECIMAL(15,2)   DEFAULT NULL,
    thumbnail_url       VARCHAR(2048)   DEFAULT NULL,
    use_file_upload     TINYINT(1)      NOT NULL DEFAULT 0,
    status              ENUM('draft','active','inactive','out_of_stock') NOT NULL DEFAULT 'active',
    is_featured         TINYINT(1)      NOT NULL DEFAULT 0,
    view_count          INT UNSIGNED    NOT NULL DEFAULT 0,
    sold_count          INT UNSIGNED    NOT NULL DEFAULT 0,
    rating_avg          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    rating_count        INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_sku (sku),
    UNIQUE KEY uk_products_slug (slug),
    KEY idx_prod_category (category_id),
    KEY idx_prod_brand (brand_id),
    KEY idx_prod_status (status),
    KEY idx_prod_featured (is_featured),
    KEY idx_prod_created_at (created_at),
    CONSTRAINT fk_prod_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prod_brand    FOREIGN KEY (brand_id)    REFERENCES brands(id)     ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.4. product_images
CREATE TABLE product_images (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id      BIGINT UNSIGNED NOT NULL,
    image_url       VARCHAR(2048)   NOT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    alt_text        VARCHAR(200)    DEFAULT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_primary      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_img_product (product_id),
    CONSTRAINT fk_img_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5. product_attributes: định nghĩa kiểu thuộc tính (Màu sắc, Kích thước...)
CREATE TABLE product_attributes (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    slug            VARCHAR(120)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attr_slug (slug),
    UNIQUE KEY uk_attr_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.6. product_attribute_values: giá trị cụ thể (Đỏ, Xanh, S, M, L)
CREATE TABLE product_attribute_values (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    attribute_id    BIGINT UNSIGNED NOT NULL,
    value           VARCHAR(100)    NOT NULL,
    color_code      VARCHAR(20)     DEFAULT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attr_value (attribute_id, value),
    KEY idx_attr_value_attr (attribute_id),
    CONSTRAINT fk_av_attr FOREIGN KEY (attribute_id) REFERENCES product_attributes(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.7. product_variants: SKU cụ thể của sản phẩm (Áo Đỏ size M)
CREATE TABLE product_variants (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id      BIGINT UNSIGNED NOT NULL,
    sku             VARCHAR(100)    NOT NULL,
    price           DECIMAL(15,2)   NOT NULL,
    sale_price      DECIMAL(15,2)   DEFAULT NULL,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    image_url       VARCHAR(2048)   DEFAULT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    weight_gram     DECIMAL(10,2)   DEFAULT NULL,
    status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_variant_sku (sku),
    KEY idx_var_product (product_id),
    KEY idx_var_status (status),
    CONSTRAINT fk_var_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.8. product_variant_values: bảng nối N-N giữa variant và attribute_value
CREATE TABLE product_variant_values (
    variant_id          BIGINT UNSIGNED NOT NULL,
    attribute_value_id  BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (variant_id, attribute_value_id),
    KEY idx_pvv_value (attribute_value_id),
    CONSTRAINT fk_pvv_variant FOREIGN KEY (variant_id)         REFERENCES product_variants(id)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pvv_value   FOREIGN KEY (attribute_value_id) REFERENCES product_attribute_values(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 3. GIỎ HÀNG (hỗ trợ guest qua session_id)
-- =====================================================================

-- 3.1. carts
CREATE TABLE carts (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED DEFAULT NULL,
    session_id      VARCHAR(100)    DEFAULT NULL,
    status          ENUM('active','converted','abandoned') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_cart_user (user_id),
    KEY idx_cart_session (session_id),
    KEY idx_cart_status (status),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2. cart_items
CREATE TABLE cart_items (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    cart_id         BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    variant_id      BIGINT UNSIGNED DEFAULT NULL,
    quantity        INT UNSIGNED    NOT NULL DEFAULT 1,
    unit_price      DECIMAL(15,2)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_product_variant (cart_id, product_id, variant_id),
    KEY idx_ci_cart (cart_id),
    KEY idx_ci_product (product_id),
    KEY idx_ci_variant (variant_id),
    CONSTRAINT fk_ci_cart    FOREIGN KEY (cart_id)    REFERENCES carts(id)            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(id)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ci_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 4. KHUYẾN MÃI / VOUCHER
-- =====================================================================

CREATE TABLE vouchers (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code                    VARCHAR(50)     NOT NULL,
    name                    VARCHAR(150)    NOT NULL,
    description             TEXT,
    discount_type           ENUM('percentage','fixed_amount') NOT NULL,
    discount_value          DECIMAL(15,2)   NOT NULL,
    min_order_amount        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    max_discount_amount     DECIMAL(15,2)   DEFAULT NULL,
    usage_limit             INT UNSIGNED    DEFAULT NULL,
    usage_limit_per_user    INT UNSIGNED    DEFAULT NULL,
    used_count              INT UNSIGNED    NOT NULL DEFAULT 0,
    start_date              DATETIME        NOT NULL,
    end_date                DATETIME        NOT NULL,
    status                  ENUM('active','inactive','expired') NOT NULL DEFAULT 'active',
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_voucher_code (code),
    KEY idx_voucher_status (status),
    KEY idx_voucher_period (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 5. VẬN CHUYỂN
-- =====================================================================

CREATE TABLE shipping_methods (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code                VARCHAR(50)     NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    description         TEXT,
    base_fee            DECIMAL(15,2)   NOT NULL DEFAULT 0,
    estimated_days_min  INT UNSIGNED    DEFAULT NULL,
    estimated_days_max  INT UNSIGNED    DEFAULT NULL,
    status              ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ship_code (code),
    KEY idx_ship_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 6. ĐƠN HÀNG
-- =====================================================================

-- 6.1. orders
CREATE TABLE orders (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_code          VARCHAR(50)     NOT NULL,
    user_id             BIGINT UNSIGNED NOT NULL,
    voucher_id          BIGINT UNSIGNED DEFAULT NULL,
    shipping_method_id  BIGINT UNSIGNED NOT NULL,
    -- Thông tin giao hàng (snapshot tại thời điểm đặt)
    recipient_name      VARCHAR(100)    NOT NULL,
    recipient_phone     VARCHAR(20)     NOT NULL,
    shipping_address    VARCHAR(500)    NOT NULL,
    -- Tiền
    subtotal            DECIMAL(15,2)   NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_amount        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    -- Thanh toán
    payment_method      ENUM('cod','bank_transfer','credit_card','momo','vnpay','zalopay') NOT NULL DEFAULT 'cod',
    payment_status      ENUM('pending','paid','failed','refunded') NOT NULL DEFAULT 'pending',
    -- Trạng thái đơn
    status              ENUM('pending','confirmed','processing','shipping','delivered','completed','cancelled','returned') NOT NULL DEFAULT 'pending',
    note                TEXT,
    placed_at           TIMESTAMP       NULL DEFAULT NULL,
    confirmed_at        TIMESTAMP       NULL DEFAULT NULL,
    delivered_at        TIMESTAMP       NULL DEFAULT NULL,
    cancelled_at        TIMESTAMP       NULL DEFAULT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_order_user (user_id),
    KEY idx_order_voucher (voucher_id),
    KEY idx_order_ship_method (shipping_method_id),
    KEY idx_order_status (status),
    KEY idx_order_payment_status (payment_status),
    KEY idx_order_created_at (created_at),
    CONSTRAINT fk_order_user        FOREIGN KEY (user_id)            REFERENCES users(id)            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_voucher     FOREIGN KEY (voucher_id)         REFERENCES vouchers(id)         ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_order_ship_method FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.2. order_items
CREATE TABLE order_items (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id        BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    variant_id      BIGINT UNSIGNED DEFAULT NULL,
    -- Snapshot dữ liệu sản phẩm tại thời điểm mua
    product_name    VARCHAR(200)    NOT NULL,
    variant_name    VARCHAR(200)    DEFAULT NULL,
    sku             VARCHAR(100)    NOT NULL,
    unit_price      DECIMAL(15,2)   NOT NULL,
    quantity        INT UNSIGNED    NOT NULL,
    subtotal        DECIMAL(15,2)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_oi_order (order_id),
    KEY idx_oi_product (product_id),
    KEY idx_oi_variant (variant_id),
    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES orders(id)           ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(id)         ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_oi_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.3. order_status_history: lịch sử thay đổi trạng thái đơn hàng
CREATE TABLE order_status_history (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id        BIGINT UNSIGNED NOT NULL,
    from_status     VARCHAR(50)     DEFAULT NULL,
    to_status       VARCHAR(50)     NOT NULL,
    note            TEXT,
    changed_by      BIGINT UNSIGNED DEFAULT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_osh_order (order_id),
    KEY idx_osh_user (changed_by),
    KEY idx_osh_created_at (created_at),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_osh_user  FOREIGN KEY (changed_by) REFERENCES users(id)  ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 7. THANH TOÁN, GIAO VẬN, ĐÁNH GIÁ
-- =====================================================================

-- 7.1. payments: ghi nhận giao dịch thanh toán (1 đơn có thể có N lần thanh toán)
CREATE TABLE payments (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id            BIGINT UNSIGNED NOT NULL,
    payment_method      ENUM('cod','bank_transfer','credit_card','momo','vnpay','zalopay') NOT NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    transaction_code    VARCHAR(150)    DEFAULT NULL,
    status              ENUM('pending','success','failed','refunded') NOT NULL DEFAULT 'pending',
    gateway_response    TEXT,
    paid_at             TIMESTAMP       NULL DEFAULT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_pay_order (order_id),
    KEY idx_pay_status (status),
    KEY idx_pay_txn (transaction_code),
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.2. shipments: thông tin vận chuyển cụ thể của đơn hàng
CREATE TABLE shipments (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id            BIGINT UNSIGNED NOT NULL,
    shipping_method_id  BIGINT UNSIGNED NOT NULL,
    tracking_number     VARCHAR(100)    DEFAULT NULL,
    carrier             VARCHAR(100)    DEFAULT NULL,
    status              ENUM('preparing','picked_up','in_transit','out_for_delivery','delivered','failed','returned') NOT NULL DEFAULT 'preparing',
    shipping_fee        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    shipped_at          TIMESTAMP       NULL DEFAULT NULL,
    delivered_at        TIMESTAMP       NULL DEFAULT NULL,
    note                TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shipment_order (order_id),
    KEY idx_ship_method (shipping_method_id),
    KEY idx_shipment_tracking (tracking_number),
    KEY idx_shipment_status (status),
    CONSTRAINT fk_shipment_order  FOREIGN KEY (order_id)           REFERENCES orders(id)           ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_shipment_method FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7.3. reviews: đánh giá sản phẩm (chỉ user đã mua mới được đánh giá -> ràng buộc bằng order_item_id)
CREATE TABLE reviews (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id      BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    order_item_id   BIGINT UNSIGNED DEFAULT NULL,
    rating          TINYINT UNSIGNED NOT NULL,
    title           VARCHAR(200)    DEFAULT NULL,
    content         TEXT,
    status          ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
    helpful_count   INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_user_orderitem (user_id, order_item_id),
    KEY idx_review_product (product_id),
    KEY idx_review_user (user_id),
    KEY idx_review_status (status),
    KEY idx_review_rating (rating),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_review_product    FOREIGN KEY (product_id)    REFERENCES products(id)    ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_review_user       FOREIGN KEY (user_id)       REFERENCES users(id)       ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_review_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 8. LỊCH SỬ DÙNG VOUCHER, WISHLIST
-- =====================================================================

-- 8.1. voucher_usages: ghi nhận voucher đã được dùng cho đơn nào
CREATE TABLE voucher_usages (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    voucher_id          BIGINT UNSIGNED NOT NULL,
    user_id             BIGINT UNSIGNED NOT NULL,
    order_id            BIGINT UNSIGNED NOT NULL,
    discount_amount     DECIMAL(15,2)   NOT NULL,
    used_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vu_order (order_id),
    KEY idx_vu_voucher (voucher_id),
    KEY idx_vu_user (user_id),
    CONSTRAINT fk_vu_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vu_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vu_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8.2. wishlists: sản phẩm yêu thích
CREATE TABLE wishlists (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wl_user_product (user_id, product_id),
    KEY idx_wl_product (product_id),
    CONSTRAINT fk_wl_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_wl_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 9. BANNERS
-- =====================================================================

CREATE TABLE banners (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200)    DEFAULT NULL,
    subtitle        VARCHAR(255)    DEFAULT NULL,
    image_url       VARCHAR(2048)   NOT NULL,
    use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
    link_url        VARCHAR(2048)   DEFAULT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
    start_at        DATETIME        DEFAULT NULL,
    end_at          DATETIME        DEFAULT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_banner_status (status),
    KEY idx_banner_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 10. SEED DATA MẪU (đúng ràng buộc khóa ngoại)
-- =====================================================================

-- 10.1 users
INSERT INTO users
    (id, email, phone, password_hash, full_name, avatar_url, use_file_upload, gender, date_of_birth, role, status, email_verified_at, last_login_at)
VALUES
    (1, 'admin@shopco.vn', '0901000001', '$2b$12$admin.hash.example', 'System Admin', 'https://cdn.shopco.vn/users/admin.jpg', 0, 'other', '1990-01-01', 'admin', 'active', '2026-01-01 08:00:00', '2026-04-20 09:00:00'),
    (2, 'staff.catalog@shopco.vn', '0901000002', '$2b$12$staff.hash.example', 'Catalog Staff', 'https://cdn.shopco.vn/users/staff.jpg', 0, 'female', '1995-06-12', 'staff', 'active', '2026-01-03 09:00:00', '2026-04-18 10:00:00'),
    (3, 'nguyenvana@gmail.com', '0901000003', '$2b$12$customer1.hash', 'Nguyen Van A', 'https://cdn.shopco.vn/users/cus-a.jpg', 0, 'male', '1998-03-21', 'customer', 'active', '2026-02-01 10:00:00', '2026-04-22 11:00:00'),
    (4, 'tranthib@gmail.com', '0901000004', '$2b$12$customer2.hash', 'Tran Thi B', 'https://cdn.shopco.vn/users/cus-b.jpg', 0, 'female', '2000-08-09', 'customer', 'active', '2026-02-10 09:00:00', '2026-04-21 14:00:00'),
    (5, 'guestbuyer@gmail.com', '0901000005', '$2b$12$customer3.hash', 'Le Van C', NULL, 0, 'male', '1997-11-15', 'customer', 'inactive', NULL, NULL);

-- 10.2 user_addresses
INSERT INTO user_addresses
    (id, user_id, recipient_name, recipient_phone, province, district, ward, street_address, address_type, is_default)
VALUES
    (1, 3, 'Nguyen Van A', '0901000003', 'Ho Chi Minh', 'Thu Duc', 'Linh Trung', '123 Vo Van Ngan', 'home', 1),
    (2, 3, 'Nguyen Van A', '0901000003', 'Ho Chi Minh', 'Quan 1', 'Ben Nghe', '45 Le Loi', 'office', 0),
    (3, 4, 'Tran Thi B', '0901000004', 'Ha Noi', 'Cau Giay', 'Dich Vong', '89 Tran Thai Tong', 'home', 1),
    (4, 5, 'Le Van C', '0901000005', 'Da Nang', 'Hai Chau', 'Hai Chau 1', '12 Bach Dang', 'other', 1);

-- 10.3 categories (self reference parent_id)
INSERT INTO categories
    (id, parent_id, name, slug, description, image_url, use_file_upload, display_order, status)
VALUES
    (1, NULL, 'Men', 'men', 'Men fashion category', 'https://cdn.shopco.vn/categories/men.jpg', 0, 1, 'active'),
    (2, NULL, 'Women', 'women', 'Women fashion category', 'https://cdn.shopco.vn/categories/women.jpg', 0, 2, 'active'),
    (3, NULL, 'Accessories', 'accessories', 'Accessories category', 'https://cdn.shopco.vn/categories/accessories.jpg', 0, 3, 'active'),
    (4, 1, 'Men T-Shirts', 'men-tshirts', 'Men t-shirts', 'https://cdn.shopco.vn/categories/men-tshirts.jpg', 0, 1, 'active'),
    (5, 2, 'Women Dresses', 'women-dresses', 'Women dresses', 'https://cdn.shopco.vn/categories/women-dresses.jpg', 0, 1, 'active'),
    (6, 3, 'Shoes', 'shoes', 'Shoes and sneakers', 'https://cdn.shopco.vn/categories/shoes.jpg', 0, 1, 'active');

-- 10.4 brands
INSERT INTO brands
    (id, name, slug, logo_url, use_file_upload, description, status)
VALUES
    (1, 'Nike', 'nike', 'https://cdn.shopco.vn/brands/nike.png', 0, 'Global sportswear brand', 'active'),
    (2, 'Adidas', 'adidas', 'https://cdn.shopco.vn/brands/adidas.png', 0, 'Sports and lifestyle brand', 'active'),
    (3, 'LocalBrand', 'localbrand', 'https://cdn.shopco.vn/brands/localbrand.png', 0, 'Vietnamese streetwear brand', 'active');

-- 10.5 products
INSERT INTO products
    (id, category_id, brand_id, sku, name, slug, short_description, description, base_price, sale_price, thumbnail_url, use_file_upload, status, is_featured, view_count, sold_count, rating_avg, rating_count)
VALUES
    (1, 4, 1, 'PROD-TS-NIKE-001', 'Nike Basic Tee', 'nike-basic-tee', 'Soft cotton daily t-shirt', 'Comfortable regular fit t-shirt for everyday use.', 350000.00, 299000.00, 'https://cdn.shopco.vn/products/nike-tee-thumb.jpg', 0, 'active', 1, 320, 45, 4.50, 2),
    (2, 5, 2, 'PROD-DR-ADI-001', 'Adidas Summer Dress', 'adidas-summer-dress', 'Lightweight summer dress', 'Breathable fabric dress with modern silhouette.', 890000.00, 790000.00, 'https://cdn.shopco.vn/products/adidas-dress-thumb.jpg', 0, 'active', 1, 220, 18, 5.00, 1),
    (3, 6, 3, 'PROD-SH-LB-001', 'LocalBrand Sneaker X', 'localbrand-sneaker-x', 'Unisex street sneaker', 'Chunky sole sneaker designed for daily movement.', 1200000.00, NULL, 'https://cdn.shopco.vn/products/lb-sneaker-thumb.jpg', 0, 'active', 0, 150, 9, 0.00, 0);

-- 10.6 product_images
INSERT INTO product_images
    (id, product_id, image_url, use_file_upload, alt_text, display_order, is_primary)
VALUES
    (1, 1, 'https://cdn.shopco.vn/products/nike-tee-1.jpg', 0, 'Nike Basic Tee Front', 1, 1),
    (2, 1, 'https://cdn.shopco.vn/products/nike-tee-2.jpg', 0, 'Nike Basic Tee Back', 2, 0),
    (3, 2, 'https://cdn.shopco.vn/products/adidas-dress-1.jpg', 0, 'Adidas Dress Front', 1, 1),
    (4, 2, 'https://cdn.shopco.vn/products/adidas-dress-2.jpg', 0, 'Adidas Dress Detail', 2, 0),
    (5, 3, 'https://cdn.shopco.vn/products/lb-sneaker-1.jpg', 0, 'Sneaker Side', 1, 1);

-- 10.7 product_attributes
INSERT INTO product_attributes
    (id, name, slug)
VALUES
    (1, 'Color', 'color'),
    (2, 'Size', 'size');

-- 10.8 product_attribute_values
INSERT INTO product_attribute_values
    (id, attribute_id, value, color_code, display_order)
VALUES
    (1, 1, 'Black', '#000000', 1),
    (2, 1, 'White', '#FFFFFF', 2),
    (3, 1, 'Red', '#FF3333', 3),
    (4, 2, 'S', NULL, 1),
    (5, 2, 'M', NULL, 2),
    (6, 2, 'L', NULL, 3),
    (7, 2, '42', NULL, 4),
    (8, 2, '43', NULL, 5);

-- 10.9 product_variants
INSERT INTO product_variants
    (id, product_id, sku, price, sale_price, stock_quantity, image_url, use_file_upload, weight_gram, status)
VALUES
    (1, 1, 'VAR-TS-NIKE-BLK-M', 350000.00, 299000.00, 50, 'https://cdn.shopco.vn/products/nike-tee-black.jpg', 0, 220.00, 'active'),
    (2, 1, 'VAR-TS-NIKE-WHT-L', 350000.00, 305000.00, 32, 'https://cdn.shopco.vn/products/nike-tee-white.jpg', 0, 230.00, 'active'),
    (3, 2, 'VAR-DR-ADI-RED-S', 890000.00, 790000.00, 20, 'https://cdn.shopco.vn/products/adidas-dress-red.jpg', 0, 380.00, 'active'),
    (4, 3, 'VAR-SH-LB-BLK-42', 1200000.00, NULL, 15, 'https://cdn.shopco.vn/products/lb-sneaker-black.jpg', 0, 760.00, 'active'),
    (5, 3, 'VAR-SH-LB-WHT-43', 1200000.00, NULL, 12, 'https://cdn.shopco.vn/products/lb-sneaker-white.jpg', 0, 770.00, 'active');

-- 10.10 product_variant_values (N-N)
INSERT INTO product_variant_values
    (variant_id, attribute_value_id)
VALUES
    (1, 1), (1, 5), -- Black, M
    (2, 2), (2, 6), -- White, L
    (3, 3), (3, 4), -- Red, S
    (4, 1), (4, 7), -- Black, 42
    (5, 2), (5, 8); -- White, 43

-- 10.11 carts
INSERT INTO carts
    (id, user_id, session_id, status)
VALUES
    (1, 3, NULL, 'active'),
    (2, NULL, 'guest-20260429-abc123', 'active'),
    (3, 4, NULL, 'converted');

-- 10.12 cart_items
INSERT INTO cart_items
    (id, cart_id, product_id, variant_id, quantity, unit_price)
VALUES
    (1, 1, 1, 1, 2, 299000.00),
    (2, 1, 3, 4, 1, 1200000.00),
    (3, 2, 2, 3, 1, 790000.00),
    (4, 3, 1, 2, 1, 305000.00);

-- 10.13 vouchers
INSERT INTO vouchers
    (id, code, name, description, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, usage_limit_per_user, used_count, start_date, end_date, status)
VALUES
    (1, 'WELCOME10', 'Welcome 10%', 'Discount 10% for first purchases', 'percentage', 10.00, 500000.00, 100000.00, 1000, 1, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'active'),
    (2, 'FREESHIP50', 'Shipping support 50K', 'Fixed discount for shipping support', 'fixed_amount', 50000.00, 300000.00, NULL, 5000, 3, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'active'),
    (3, 'SPRINGSALE', 'Spring campaign', 'Seasonal promotion', 'percentage', 15.00, 1000000.00, 200000.00, 300, 2, 0, '2026-03-01 00:00:00', '2026-05-31 23:59:59', 'active');

-- 10.14 shipping_methods
INSERT INTO shipping_methods
    (id, code, name, description, base_fee, estimated_days_min, estimated_days_max, status)
VALUES
    (1, 'STANDARD', 'Standard Delivery', 'Delivery via standard route', 30000.00, 3, 5, 'active'),
    (2, 'EXPRESS', 'Express Delivery', 'Fast delivery in major cities', 50000.00, 1, 2, 'active'),
    (3, 'SAME_DAY', 'Same Day Delivery', 'Inner-city same day service', 70000.00, 0, 1, 'inactive');

-- 10.15 orders
INSERT INTO orders
    (id, order_code, user_id, voucher_id, shipping_method_id, recipient_name, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, payment_method, payment_status, status, note, placed_at, confirmed_at, delivered_at, cancelled_at)
VALUES
    (1, 'ORD-20260401-0001', 3, 1, 1, 'Nguyen Van A', '0901000003', '123 Vo Van Ngan, Linh Trung, Thu Duc, Ho Chi Minh', 1804000.00, 30000.00, 100000.00, 1734000.00, 'vnpay', 'paid', 'delivered', 'Leave at front desk', '2026-04-01 08:45:00', '2026-04-01 09:00:00', '2026-04-04 16:30:00', NULL),
    (2, 'ORD-20260410-0002', 4, NULL, 2, 'Tran Thi B', '0901000004', '89 Tran Thai Tong, Dich Vong, Cau Giay, Ha Noi', 790000.00, 50000.00, 0.00, 840000.00, 'cod', 'pending', 'shipping', 'Call before delivery', '2026-04-10 11:20:00', '2026-04-10 11:45:00', NULL, NULL),
    (3, 'ORD-20260415-0003', 3, NULL, 1, 'Nguyen Van A', '0901000003', '45 Le Loi, Ben Nghe, Quan 1, Ho Chi Minh', 305000.00, 30000.00, 0.00, 335000.00, 'momo', 'failed', 'cancelled', 'Payment timeout', '2026-04-15 14:10:00', NULL, NULL, '2026-04-15 14:30:00');

-- 10.16 order_items
INSERT INTO order_items
    (id, order_id, product_id, variant_id, product_name, variant_name, sku, unit_price, quantity, subtotal)
VALUES
    (1, 1, 1, 1, 'Nike Basic Tee', 'Black / M', 'VAR-TS-NIKE-BLK-M', 299000.00, 1, 299000.00),
    (2, 1, 3, 4, 'LocalBrand Sneaker X', 'Black / 42', 'VAR-SH-LB-BLK-42', 1200000.00, 1, 1200000.00),
    (3, 1, 1, 2, 'Nike Basic Tee', 'White / L', 'VAR-TS-NIKE-WHT-L', 305000.00, 1, 305000.00),
    (4, 2, 2, 3, 'Adidas Summer Dress', 'Red / S', 'VAR-DR-ADI-RED-S', 790000.00, 1, 790000.00),
    (5, 3, 1, 2, 'Nike Basic Tee', 'White / L', 'VAR-TS-NIKE-WHT-L', 305000.00, 1, 305000.00);

-- 10.17 order_status_history
INSERT INTO order_status_history
    (id, order_id, from_status, to_status, note, changed_by, created_at)
VALUES
    (1, 1, NULL, 'pending', 'Order created', 3, '2026-04-01 08:45:00'),
    (2, 1, 'pending', 'confirmed', 'Staff confirmed stock', 2, '2026-04-01 09:00:00'),
    (3, 1, 'confirmed', 'shipping', 'Handed to carrier', 2, '2026-04-02 10:00:00'),
    (4, 1, 'shipping', 'delivered', 'Delivered successfully', 2, '2026-04-04 16:30:00'),
    (5, 2, NULL, 'pending', 'Order created', 4, '2026-04-10 11:20:00'),
    (6, 2, 'pending', 'confirmed', 'Order confirmed', 2, '2026-04-10 11:45:00'),
    (7, 2, 'confirmed', 'shipping', 'Courier picked up', 2, '2026-04-11 09:00:00'),
    (8, 3, NULL, 'pending', 'Order created', 3, '2026-04-15 14:10:00'),
    (9, 3, 'pending', 'cancelled', 'Payment failed', 3, '2026-04-15 14:30:00');

-- 10.18 payments
INSERT INTO payments
    (id, order_id, payment_method, amount, transaction_code, status, gateway_response, paid_at)
VALUES
    (1, 1, 'vnpay', 1734000.00, 'VNPAY-TRX-20260401-001', 'success', '{"bank":"VCB","code":"00"}', '2026-04-01 08:47:00'),
    (2, 2, 'cod', 840000.00, NULL, 'pending', NULL, NULL),
    (3, 3, 'momo', 335000.00, 'MOMO-TRX-20260415-001', 'failed', '{"message":"timeout"}', NULL);

-- 10.19 shipments
INSERT INTO shipments
    (id, order_id, shipping_method_id, tracking_number, carrier, status, shipping_fee, shipped_at, delivered_at, note)
VALUES
    (1, 1, 1, 'GHN123456789', 'GHN', 'delivered', 30000.00, '2026-04-02 10:15:00', '2026-04-04 16:30:00', 'Delivered to recipient'),
    (2, 2, 2, 'GHTK987654321', 'GHTK', 'in_transit', 50000.00, '2026-04-11 09:00:00', NULL, 'On route to destination'),
    (3, 3, 1, NULL, NULL, 'failed', 30000.00, NULL, NULL, 'Order cancelled before shipment');

-- 10.20 reviews
INSERT INTO reviews
    (id, product_id, user_id, order_item_id, rating, title, content, status, helpful_count)
VALUES
    (1, 1, 3, 1, 5, 'Ao mac rat thoai mai', 'Chat vai mem, dung size, giao nhanh.', 'approved', 4),
    (2, 3, 3, 2, 4, 'Giay dep', 'Form dep, de di, se ung ho tiep.', 'approved', 2),
    (3, 2, 4, 4, 5, 'Vay dep', 'Mau dep, chat lieu on.', 'pending', 0);

-- 10.21 voucher_usages
INSERT INTO voucher_usages
    (id, voucher_id, user_id, order_id, discount_amount, used_at)
VALUES
    (1, 1, 3, 1, 100000.00, '2026-04-01 08:46:00');

-- 10.22 wishlists
INSERT INTO wishlists
    (id, user_id, product_id)
VALUES
    (1, 3, 2),
    (2, 3, 3),
    (3, 4, 1);

-- 10.23 banners
INSERT INTO banners
    (id, title, subtitle, image_url, use_file_upload, link_url, display_order, status, start_at, end_at)
VALUES
    (1, 'Summer Sale 2026', 'Up to 50% for selected items', 'https://cdn.shopco.vn/banners/summer-sale-2026.jpg', 0, '/catalog?sale=true', 1, 'active', '2026-04-01 00:00:00', '2026-06-30 23:59:59'),
    (2, 'New Arrival', 'Latest collection just landed', 'https://cdn.shopco.vn/banners/new-arrival-2026.jpg', 0, '/catalog?sort=new', 2, 'active', '2026-04-10 00:00:00', '2026-12-31 23:59:59'),
    (3, 'Member Day', 'Voucher for loyal customers', 'https://cdn.shopco.vn/banners/member-day.jpg', 0, '/account/vouchers', 3, 'inactive', '2026-05-01 00:00:00', '2026-05-31 23:59:59');

-- =====================================================================
-- KẾT THÚC SCHEMA
-- =====================================================================
