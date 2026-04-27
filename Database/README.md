# E-commerce Database (MySQL)

Database hoàn chỉnh cho website thương mại điện tử (shop bán hàng online).
Engine: **InnoDB** • Charset: **utf8mb4** • MySQL **8.0+**.

## 1. Cấu trúc thư mục

```
Database/
├── schema.sql       # CREATE DATABASE + CREATE TABLE + INDEX + FK
├── seed_data.sql    # Dữ liệu mẫu (≥ 3-5 bản ghi/bảng)
└── README.md        # Tài liệu thiết kế (file này)
```

## 2. Cách chạy

```bash
# Tạo cấu trúc DB
mysql -u root -p < schema.sql

# Nạp dữ liệu mẫu
mysql -u root -p < seed_data.sql
```

Hoặc trong MySQL CLI / Workbench:
```sql
SOURCE /duong/dan/Database/schema.sql;
SOURCE /duong/dan/Database/seed_data.sql;
```

## 3. Phân tích nghiệp vụ

Hệ thống quản lý các thực thể chính:

| # | Nhóm nghiệp vụ | Bảng chính |
|---|---|---|
| 1 | **Người dùng & phân quyền** | `users` (role: admin / staff / customer) |
| 2 | **Đa địa chỉ giao hàng** | `user_addresses` |
| 3 | **Danh mục sản phẩm** (đa cấp) | `categories` (self-reference) |
| 4 | **Thương hiệu** | `brands` |
| 5 | **Sản phẩm & biến thể** (màu, size...) | `products`, `product_images`, `product_attributes`, `product_attribute_values`, `product_variants`, `product_variant_values` |
| 6 | **Giỏ hàng** (hỗ trợ guest) | `carts`, `cart_items` |
| 7 | **Khuyến mãi / voucher** | `vouchers`, `voucher_usages` |
| 8 | **Vận chuyển** | `shipping_methods`, `shipments` |
| 9 | **Đơn hàng & lịch sử trạng thái** | `orders`, `order_items`, `order_status_history` |
| 10 | **Thanh toán** | `payments` |
| 11 | **Đánh giá sản phẩm** | `reviews` |
| 12 | **Yêu thích** | `wishlists` |

## 4. Sơ đồ ERD (ASCII)

```
                     ┌──────────────────┐
                     │      users       │ 1 ─┐
                     │ id, email, role  │    │
                     └────────┬─────────┘    │ 1
                              │ 1            │
              ┌───────────────┼───────────┐  │
              │ 1             │ 1         │  │
              ▼ N             ▼ N         ▼ N
    ┌───────────────────┐ ┌─────────┐ ┌──────────┐
    │  user_addresses   │ │  carts  │ │  orders  │◄──────┐
    └───────────────────┘ └────┬────┘ └────┬─────┘       │
                               │ 1         │ 1           │ N
                               ▼ N         ├─────────────┤
                          ┌──────────┐     │ 1           │
                          │cart_items│     ▼ N           │ 1
                          └─────┬────┘ ┌──────────────┐  │
                                │      │ order_items  │──┘ N
                                │      └──────┬───────┘
                                │ N           │ N
                                ▼             ▼
                           ┌─────────┐ ┌──────────────┐
                           │products │◄─┤              │
                           └────┬────┘ └─────┬────────┘
                                │ 1          │ N
                                ▼ N          ▼
                ┌────────────────────┐  ┌─────────┐
                │ product_variants   │  │ reviews │
                │ (SKU / màu / size) │  └─────────┘
                └─────────┬──────────┘
                          │ N
                          ▼ N (qua product_variant_values)
              ┌──────────────────────────┐
              │ product_attribute_values │
              └────────────┬─────────────┘
                           │ N
                           ▼ 1
                   ┌──────────────────┐
                   │product_attributes│  (Màu sắc, Kích thước...)
                   └──────────────────┘

   orders ─1──N─► order_status_history (lịch sử pending → confirmed → shipping → delivered)
   orders ─1──N─► payments       (giao dịch thanh toán)
   orders ─1──1─► shipments      (thông tin vận chuyển cụ thể)
   orders ─N──1─► shipping_methods
   orders ─N──1─► vouchers ─1──N─► voucher_usages
   users  ─1──N─► wishlists ─N──1─► products
   categories ─self─ parent_id (cây danh mục đa cấp)
```

## 5. Mối quan hệ giữa các bảng

| Quan hệ | Loại | Mô tả |
|---|---|---|
| `users` ↔ `user_addresses` | 1 - N | 1 user có nhiều địa chỉ giao hàng |
| `users` ↔ `orders` | 1 - N | 1 user có nhiều đơn hàng |
| `users` ↔ `carts` | 1 - N | 1 user có thể có nhiều giỏ (active / converted) |
| `users` ↔ `reviews` | 1 - N | 1 user viết nhiều review |
| `users` ↔ `wishlists` ↔ `products` | N - N | Wishlist là bảng nối |
| `categories` ↔ `categories` | 1 - N (self) | `parent_id` cho danh mục đa cấp |
| `categories` ↔ `products` | 1 - N | 1 danh mục có nhiều sản phẩm |
| `brands` ↔ `products` | 1 - N | 1 thương hiệu có nhiều sản phẩm |
| `products` ↔ `product_images` | 1 - N | Nhiều ảnh / 1 sản phẩm |
| `products` ↔ `product_variants` | 1 - N | 1 sản phẩm có nhiều biến thể (SKU) |
| `product_variants` ↔ `product_attribute_values` | N - N | Bảng nối `product_variant_values` (vd: variant "Áo đỏ M" = Đỏ + M) |
| `product_attributes` ↔ `product_attribute_values` | 1 - N | Thuộc tính `Màu sắc` có giá trị Đỏ, Xanh… |
| `carts` ↔ `cart_items` | 1 - N | Giỏ chứa nhiều sản phẩm |
| `cart_items` ↔ `products` / `product_variants` | N - 1 | Item trỏ đến product + variant |
| `orders` ↔ `order_items` | 1 - N | Đơn có nhiều dòng hàng |
| `orders` ↔ `order_status_history` | 1 - N | Lưu lịch sử thay đổi trạng thái |
| `orders` ↔ `payments` | 1 - N | 1 đơn có thể có nhiều giao dịch (retry, refund) |
| `orders` ↔ `shipments` | 1 - 1 | Mỗi đơn có 1 bản ghi vận chuyển |
| `orders` ↔ `vouchers` | N - 1 | Đơn áp dụng tối đa 1 voucher |
| `vouchers` ↔ `voucher_usages` | 1 - N | Theo dõi mỗi lần voucher được dùng |
| `shipping_methods` ↔ `orders` / `shipments` | 1 - N | Phương thức vận chuyển |
| `products` ↔ `reviews` | 1 - N | Sản phẩm có nhiều review |

## 6. Tính năng nghiệp vụ được phản ánh

| Yêu cầu | Cách thiết kế |
|---|---|
| **Sản phẩm có nhiều biến thể** (màu, size…) | `products` chỉ chứa thông tin chung; `product_variants` lưu SKU cụ thể; `product_attributes` + `product_attribute_values` định nghĩa thuộc tính; bảng nối `product_variant_values` (N-N) cho phép 1 variant kết hợp nhiều thuộc tính |
| **Giỏ hàng cho khách chưa đăng nhập** | `carts.user_id` *NULLABLE*, dùng `session_id` để gắn cho guest. Khi đăng nhập có thể merge giỏ guest vào user và đổi `status='converted'` |
| **Voucher / mã giảm giá** | `vouchers` (percentage / fixed_amount, min_order, max_discount, usage_limit, hạn dùng). `voucher_usages` ghi nhận từng lần áp dụng để kiểm soát `usage_limit_per_user` |
| **Lịch sử trạng thái đơn hàng** | `order_status_history` lưu mỗi lần đổi (`from_status` → `to_status`, `changed_by`, `note`). Cột `orders.status` luôn lưu trạng thái hiện tại để truy vấn nhanh |
| **Phân quyền người dùng** | `users.role ENUM('admin','staff','customer')` |
| **Đa địa chỉ giao hàng** | `user_addresses` 1-N với `users`; cờ `is_default` đánh dấu địa chỉ mặc định; khi đặt đơn, dữ liệu địa chỉ được **snapshot** vào `orders` để không bị thay đổi về sau |

## 7. Lưu ý kỹ thuật đã áp dụng

- ✅ **InnoDB** cho mọi bảng → hỗ trợ transaction & FK.
- ✅ **utf8mb4 / utf8mb4_unicode_ci** → tiếng Việt có dấu + emoji.
- ✅ Tiền tệ luôn dùng **`DECIMAL(15,2)`** (`base_price`, `total_amount`, `discount_amount`…).
- ✅ Mọi bảng (trừ bảng nối thuần `product_variant_values`) đều có **`created_at`** và **`updated_at`** kiểu `TIMESTAMP` (auto-update).
- ✅ **snake_case** cho tên bảng/cột.
- ✅ Index trên các cột thường truy vấn:
  - `email`, `phone`, `slug`, `sku` (UNIQUE)
  - `status`, `payment_status`, `is_featured`, `created_at` (filter / sort)
  - `parent_id`, `category_id`, `brand_id`, `product_id`, `variant_id`, `user_id`, `order_id` (FK)
  - `(start_date, end_date)` cho voucher.
- ✅ **Snapshot dữ liệu** trong `orders` / `order_items` (giá, tên sản phẩm, địa chỉ) để lịch sử không bị thay đổi khi sản phẩm/giá thay đổi sau này.
- ✅ **CHECK constraint** trên `reviews.rating` (1–5).
- ✅ Cascade rule hợp lý: `ON DELETE CASCADE` cho dữ liệu phụ thuộc (cart_items, order_status_history, voucher_usages); `ON DELETE RESTRICT` cho các bảng có ý nghĩa kế toán (orders → users / shipping_methods); `ON DELETE SET NULL` cho FK mềm (orders.voucher_id).

## 8. Tài khoản & dữ liệu mẫu

| Email | Vai trò | Mật khẩu (mẫu) |
|---|---|---|
| `admin@shop.vn` | admin | `123456` |
| `staff01@shop.vn` | staff | `123456` |
| `an.nguyen@gmail.com` | customer | `123456` |
| `binh.tran@gmail.com` | customer | `123456` |
| `cuong.le@gmail.com` | customer | `123456` |

> Hash mật khẩu trong seed chỉ là chuỗi minh họa. Khi tích hợp backend hãy thay bằng hash bcrypt/argon2 thật.

Bao gồm 5 đơn hàng mẫu trải đều các trạng thái: `pending`, `confirmed`, `shipping`, `delivered`, `completed` — kèm lịch sử trạng thái, payment & shipment tương ứng.

## 9. Một số truy vấn ví dụ

```sql
-- Top 5 sản phẩm bán chạy
SELECT p.id, p.name, SUM(oi.quantity) AS total_sold
FROM order_items oi
JOIN products p   ON p.id = oi.product_id
JOIN orders o     ON o.id = oi.order_id
WHERE o.status IN ('delivered','completed')
GROUP BY p.id, p.name
ORDER BY total_sold DESC
LIMIT 5;

-- Doanh thu theo tháng
SELECT DATE_FORMAT(placed_at, '%Y-%m') AS month,
       SUM(total_amount) AS revenue,
       COUNT(*) AS order_count
FROM orders
WHERE status IN ('delivered','completed')
GROUP BY month
ORDER BY month;

-- Kiểm tra tồn kho theo biến thể
SELECT p.name, pv.sku, pv.stock_quantity
FROM product_variants pv
JOIN products p ON p.id = pv.product_id
WHERE pv.stock_quantity < 20
ORDER BY pv.stock_quantity ASC;

-- Lịch sử trạng thái 1 đơn
SELECT osh.created_at, osh.from_status, osh.to_status, osh.note, u.full_name AS changed_by
FROM order_status_history osh
LEFT JOIN users u ON u.id = osh.changed_by
WHERE osh.order_id = 1
ORDER BY osh.created_at;
```

## 10. Mở rộng tương lai (gợi ý)

- Bảng `coupon_categories` / `coupon_products` nếu voucher giới hạn theo danh mục/sản phẩm cụ thể.
- Bảng `product_inventory_logs` để theo dõi nhập/xuất kho.
- Bảng `notifications`, `audit_logs` cho dashboard admin.
- Tách `payment_methods` thành bảng riêng nếu cần cấu hình động.
- Full-text index trên `products.name`, `description` để search.
