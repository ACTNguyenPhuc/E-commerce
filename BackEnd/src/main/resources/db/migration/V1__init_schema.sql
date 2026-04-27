-- =====================================================================
-- V1__init_schema.sql
-- Khởi tạo toàn bộ schema cho ecommerce_db
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1.1. users
CREATE TABLE users (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email           VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20)     DEFAULT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    avatar_url      VARCHAR(255)    DEFAULT NULL,
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

-- 1.2. user_addresses
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

-- 2.1. categories
CREATE TABLE categories (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT UNSIGNED DEFAULT NULL,
    name            VARCHAR(150)    NOT NULL,
    slug            VARCHAR(180)    NOT NULL,
    description     TEXT,
    image_url       VARCHAR(255)    DEFAULT NULL,
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
    logo_url        VARCHAR(255)    DEFAULT NULL,
    description     TEXT,
    status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_brands_slug (slug),
    KEY idx_brand_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3. products
CREATE TABLE products (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id         BIGINT UNSIGNED NOT NULL,
    brand_id            BIGINT UNSIGNED DEFAULT NULL,
    sku                 VARCHAR(100)    NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    slug                VARCHAR(220)    NOT NULL,
    short_description   VARCHAR(500)    DEFAULT NULL,
    description         TEXT,
    base_price          DECIMAL(15,2)   NOT NULL,
    sale_price          DECIMAL(15,2)   DEFAULT NULL,
    thumbnail_url       VARCHAR(255)    DEFAULT NULL,
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
    image_url       VARCHAR(255)    NOT NULL,
    alt_text        VARCHAR(200)    DEFAULT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_primary      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_img_product (product_id),
    CONSTRAINT fk_img_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5. product_attributes
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

-- 2.6. product_attribute_values
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

-- 2.7. product_variants
CREATE TABLE product_variants (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id      BIGINT UNSIGNED NOT NULL,
    sku             VARCHAR(100)    NOT NULL,
    price           DECIMAL(15,2)   NOT NULL,
    sale_price      DECIMAL(15,2)   DEFAULT NULL,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    image_url       VARCHAR(255)    DEFAULT NULL,
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

-- 2.8. product_variant_values
CREATE TABLE product_variant_values (
    variant_id          BIGINT UNSIGNED NOT NULL,
    attribute_value_id  BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (variant_id, attribute_value_id),
    KEY idx_pvv_value (attribute_value_id),
    CONSTRAINT fk_pvv_variant FOREIGN KEY (variant_id)         REFERENCES product_variants(id)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pvv_value   FOREIGN KEY (attribute_value_id) REFERENCES product_attribute_values(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

-- 4. vouchers
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

-- 5. shipping_methods
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

-- 6.1. orders
CREATE TABLE orders (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_code          VARCHAR(50)     NOT NULL,
    user_id             BIGINT UNSIGNED NOT NULL,
    voucher_id          BIGINT UNSIGNED DEFAULT NULL,
    shipping_method_id  BIGINT UNSIGNED NOT NULL,
    recipient_name      VARCHAR(100)    NOT NULL,
    recipient_phone     VARCHAR(20)     NOT NULL,
    shipping_address    VARCHAR(500)    NOT NULL,
    subtotal            DECIMAL(15,2)   NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_amount        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    payment_method      ENUM('cod','bank_transfer','credit_card','momo','vnpay','zalopay') NOT NULL DEFAULT 'cod',
    payment_status      ENUM('pending','paid','failed','refunded') NOT NULL DEFAULT 'pending',
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

-- 6.3. order_status_history
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

-- 7.1. payments
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

-- 7.2. shipments
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

-- 7.3. reviews
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

-- 8.1. voucher_usages
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

-- 8.2. wishlists
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

SET FOREIGN_KEY_CHECKS = 1;
