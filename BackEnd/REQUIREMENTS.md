# BackEnd Requirements – E-commerce Spring Boot Server

> Tài liệu mô tả chi tiết yêu cầu kỹ thuật, kiến trúc và đặc tả nghiệp vụ cho **Backend Server** của hệ thống thương mại điện tử, dựa trên schema MySQL trong `Database/schema.sql`.

---

## 1. Tổng quan dự án

| Hạng mục | Giá trị |
|---|---|
| Tên dự án | `ecommerce-backend` |
| Mục tiêu | Cung cấp REST API cho **ClientShop** (khách hàng) và **Dashboard** (admin/staff) |
| Database | MySQL 8.0+ (`ecommerce_db`) |
| Style API | RESTful JSON, version qua URL (`/api/v1/...`) |
| Đối tượng người dùng | `admin`, `staff`, `customer`, `guest` (chưa đăng nhập) |
| Ngôn ngữ giao diện trả về | Hỗ trợ **vi-VN** (mặc định) và **en-US** (header `Accept-Language`) |

---

## 2. Tech Stack

| Lớp | Công nghệ | Phiên bản đề xuất |
|---|---|---|
| Ngôn ngữ | **Java** | 17 LTS (hoặc 21 LTS) |
| Framework | **Spring Boot** | 3.3.x |
| Build tool | **Maven** (hoặc Gradle) | 3.9+ |
| ORM | **Spring Data JPA + Hibernate** | theo Spring Boot |
| Database | **MySQL** | 8.0+ |
| Database migration | **Flyway** | 10.x |
| Bảo mật | **Spring Security + JWT** (`jjwt` 0.12.x) | – |
| Mapping DTO | **MapStruct** | 1.5.x |
| Validation | `jakarta.validation` (Hibernate Validator) | – |
| Documentation | **springdoc-openapi** (Swagger UI) | 2.x |
| Caching | **Redis** (Spring Data Redis) – tuỳ chọn | 7.x |
| File storage | **Local FS** (dev) → **AWS S3 / MinIO** (prod) | – |
| Mail | **Spring Mail** (SMTP) hoặc SendGrid | – |
| Payment SDK | VNPay, MoMo, ZaloPay, Stripe | – |
| Logging | **SLF4J + Logback** | – |
| Test | **JUnit 5**, **Mockito**, **Testcontainers** | – |
| Containerization | **Docker** + `docker-compose` | – |

---

## 3. Cấu trúc thư mục dự án

```
BackEnd/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── src/
    ├── main/
    │   ├── java/com/ecommerce/
    │   │   ├── EcommerceApplication.java
    │   │   ├── config/              # Security, JPA, Swagger, CORS, Redis...
    │   │   ├── common/              # Base classes, constants, utils
    │   │   │   ├── exception/       # Custom exceptions
    │   │   │   ├── response/        # ApiResponse, PageResponse
    │   │   │   └── util/
    │   │   ├── security/            # JWT, UserDetails, filters
    │   │   ├── modules/
    │   │   │   ├── auth/            # Đăng ký, đăng nhập, refresh token
    │   │   │   ├── user/            # Profile, addresses
    │   │   │   ├── catalog/         # category, brand, product, variant, attribute
    │   │   │   ├── cart/
    │   │   │   ├── voucher/
    │   │   │   ├── order/           # order, order-item, status-history
    │   │   │   ├── payment/         # gateway integration
    │   │   │   ├── shipping/
    │   │   │   ├── review/
    │   │   │   ├── wishlist/
    │   │   │   ├── upload/          # ảnh sản phẩm, avatar
    │   │   │   └── admin/           # report, dashboard
    │   │   └── infrastructure/      # mail, storage, payment SDK clients
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── db/migration/        # Flyway: V1__init.sql, V2__seed.sql ...
    │       └── i18n/messages_*.properties
    └── test/
        └── java/com/ecommerce/...
```

> **Quy ước module**: mỗi module gồm `entity/`, `repository/`, `service/`, `controller/`, `dto/`, `mapper/`.

---

## 4. Kiến trúc hệ thống

### 4.1. Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│   Client (Web/Mobile)  ──HTTPS──▶  Backend Server   │
└─────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┴─────────────────────┐
        │   Controller (REST)                       │  ← @RestController, @Valid
        ├───────────────────────────────────────────┤
        │   Service (Business Logic)                │  ← @Service, @Transactional
        ├───────────────────────────────────────────┤
        │   Repository (Data Access)                │  ← Spring Data JPA
        ├───────────────────────────────────────────┤
        │   Entity (Domain Model)                   │  ← @Entity, JPA mapping
        └───────────────────────────────────────────┘
                              │
                       MySQL · Redis · S3 · SMTP · Payment Gateway
```

### 4.2. Nguyên tắc

- **DTO** ↔ **Entity** dùng MapStruct, không trả Entity trực tiếp ra controller.
- **Service** chứa logic nghiệp vụ; transaction khai báo bằng `@Transactional` ở service.
- **Repository** chỉ chứa truy vấn (JPQL / native / Specification / QueryDSL).
- **Controller** mỏng, chỉ validate input và uỷ quyền cho service.
- **Idempotent**: các API tạo đơn / thanh toán hỗ trợ `Idempotency-Key` header.

---

## 5. Module nghiệp vụ (mapping từ schema)

| # | Module | Bảng liên quan | Vai trò chính |
|---|---|---|---|
| 1 | **auth** | `users` | Đăng ký, đăng nhập, refresh token, đổi mật khẩu, quên mật khẩu, xác thực email |
| 2 | **user** | `users`, `user_addresses` | Profile, đa địa chỉ giao hàng |
| 3 | **catalog** | `categories`, `brands`, `products`, `product_images`, `product_attributes`, `product_attribute_values`, `product_variants`, `product_variant_values` | Quản lý sản phẩm, biến thể, danh mục, thương hiệu |
| 4 | **cart** | `carts`, `cart_items` | Giỏ hàng (user + guest qua `session_id`) |
| 5 | **voucher** | `vouchers`, `voucher_usages` | Mã giảm giá, kiểm tra điều kiện áp dụng |
| 6 | **order** | `orders`, `order_items`, `order_status_history` | Đặt đơn, theo dõi trạng thái, huỷ đơn |
| 7 | **payment** | `payments` | Tích hợp cổng thanh toán (VNPay, MoMo, ZaloPay, COD, thẻ) |
| 8 | **shipping** | `shipping_methods`, `shipments` | Phương thức vận chuyển, tính phí, theo dõi |
| 9 | **review** | `reviews` | Đánh giá sản phẩm (chỉ user đã mua) |
| 10 | **wishlist** | `wishlists` | Danh sách yêu thích |
| 11 | **upload** | – | Upload ảnh sản phẩm / avatar (multipart) |
| 12 | **admin** | tổng hợp | Dashboard: doanh thu, top SP, tồn kho |

---

## 6. Authentication & Authorization

### 6.1. Chiến lược

- **JWT Bearer Token**: 
  - `accessToken` – TTL 15 phút
  - `refreshToken` – TTL 7 ngày, lưu hash trong Redis (hoặc DB) để có thể revoke.
- **Stateless** – không dùng session HTTP.
- Header: `Authorization: Bearer <accessToken>`.

### 6.2. Phân quyền (Role-Based)

| Role | Quyền chính |
|---|---|
| `admin` | Toàn quyền: CRUD mọi resource, xem báo cáo, quản lý user/staff |
| `staff` | Quản lý đơn hàng (xác nhận, đổi trạng thái), xem sản phẩm, chăm sóc KH; **không** xoá user, không sửa cấu hình thanh toán |
| `customer` | Mua hàng, quản lý profile/đơn hàng/đánh giá của chính mình |
| `guest` | Xem sản phẩm, dùng giỏ hàng (qua `session_id`), KHÔNG đặt đơn |

Sử dụng `@PreAuthorize("hasRole('ADMIN')")` ở method, kết hợp `@AuthenticationPrincipal` để lấy user hiện tại.

### 6.3. Luồng đăng nhập

```
POST /api/v1/auth/login   { email, password }
   ↓
   Spring Security → AuthenticationManager → UserDetailsService
   ↓
   Verify BCrypt password
   ↓
   Sinh JWT (accessToken + refreshToken)
   ↓
   Cập nhật users.last_login_at
   ↓
   Trả về { accessToken, refreshToken, user }
```

### 6.4. Xử lý mật khẩu

- Hash bằng **BCrypt** (`strength = 12`) khi đăng ký / đổi mật khẩu.
- Quên mật khẩu: gửi email chứa OTP/link reset có TTL 15 phút.
- Email chưa xác thực vẫn login được nhưng không thể đặt đơn.

---

## 7. Đặc tả REST API

> Quy ước:
> - Base URL: `/api/v1`
> - Trả response chuẩn:
>   ```json
>   { "success": true, "code": 200, "message": "OK", "data": ..., "timestamp": "..." }
>   ```
> - Lỗi:
>   ```json
>   { "success": false, "code": 400, "message": "...", "errors": [...] }
>   ```
> - Pagination: `?page=0&size=20&sort=createdAt,desc`
> - Trả `PageResponse<T>` chứa `content`, `page`, `size`, `totalElements`, `totalPages`.

### 7.1. Auth (`/auth`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/auth/register` | – | Đăng ký tài khoản customer |
| POST | `/auth/login` | – | Đăng nhập, trả accessToken + refreshToken |
| POST | `/auth/refresh` | – | Cấp accessToken mới từ refreshToken |
| POST | `/auth/logout` | user | Revoke refreshToken hiện tại |
| POST | `/auth/forgot-password` | – | Gửi email reset |
| POST | `/auth/reset-password` | – | Đặt mật khẩu mới qua token |
| POST | `/auth/verify-email` | – | Xác thực email qua token |
| POST | `/auth/change-password` | user | Đổi mật khẩu (cần old + new) |

### 7.2. User (`/users`, `/me`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/me` | user | Lấy profile hiện tại |
| PUT | `/me` | user | Cập nhật profile (full_name, gender, dob, avatar) |
| GET | `/me/addresses` | user | Liệt kê địa chỉ |
| POST | `/me/addresses` | user | Thêm địa chỉ |
| PUT | `/me/addresses/{id}` | user | Sửa địa chỉ |
| DELETE | `/me/addresses/{id}` | user | Xoá địa chỉ |
| PATCH | `/me/addresses/{id}/default` | user | Đặt mặc định |
| GET | `/admin/users` | admin | Liệt kê user (filter role/status, search) |
| PATCH | `/admin/users/{id}/status` | admin | Active/inactive/ban |

### 7.3. Catalog – Public

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/categories` | Cây danh mục (gộp parent/children) |
| GET | `/categories/{slug}` | Chi tiết + sản phẩm thuộc danh mục |
| GET | `/brands` | Danh sách thương hiệu |
| GET | `/products` | Danh sách sản phẩm – filter `categoryId`, `brandId`, `minPrice`, `maxPrice`, `q`, `sort` |
| GET | `/products/{slug}` | Chi tiết sản phẩm + biến thể + ảnh |
| GET | `/products/{id}/variants` | Danh sách variant |
| GET | `/products/featured` | Sản phẩm nổi bật |
| GET | `/search?q=...` | Full-text search (tên, mô tả) |

### 7.4. Catalog – Admin

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST/PUT/DELETE | `/admin/categories[/{id}]` | admin | CRUD danh mục |
| POST/PUT/DELETE | `/admin/brands[/{id}]` | admin | CRUD thương hiệu |
| POST/PUT/DELETE | `/admin/products[/{id}]` | admin/staff | CRUD sản phẩm |
| POST/PUT/DELETE | `/admin/products/{id}/variants[/{vid}]` | admin/staff | Quản lý biến thể |
| POST/DELETE | `/admin/products/{id}/images` | admin/staff | Quản lý ảnh sản phẩm |
| GET/POST/PUT/DELETE | `/admin/attributes[/{id}]` | admin | Quản lý thuộc tính (Màu, Size...) |
| GET/POST/PUT/DELETE | `/admin/attributes/{id}/values` | admin | Quản lý giá trị thuộc tính |

### 7.5. Cart (`/cart`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/cart` | user/guest | Lấy giỏ hàng hiện tại (header `X-Session-Id` cho guest) |
| POST | `/cart/items` | user/guest | Thêm sản phẩm vào giỏ `{ productId, variantId, quantity }` |
| PUT | `/cart/items/{itemId}` | user/guest | Cập nhật số lượng |
| DELETE | `/cart/items/{itemId}` | user/guest | Xoá sản phẩm khỏi giỏ |
| DELETE | `/cart` | user/guest | Xoá toàn bộ giỏ |
| POST | `/cart/merge` | user | Merge giỏ guest (`sessionId`) sau khi đăng nhập |

### 7.6. Voucher (`/vouchers`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/vouchers/active` | user | Liệt kê voucher đang khả dụng |
| POST | `/vouchers/validate` | user | Validate voucher với giỏ hiện tại `{ code, subtotal }` |
| GET | `/admin/vouchers` | admin | Liệt kê toàn bộ voucher |
| POST/PUT/DELETE | `/admin/vouchers[/{id}]` | admin | CRUD voucher |

### 7.7. Order (`/orders`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/orders/preview` | user | Tính toán trước (subtotal, ship, discount, total) |
| POST | `/orders` | user | Đặt đơn từ giỏ hàng |
| GET | `/orders` | user | Đơn hàng của tôi (filter status) |
| GET | `/orders/{code}` | user | Chi tiết đơn (kèm items, history, payment, shipment) |
| POST | `/orders/{code}/cancel` | user | Huỷ đơn (chỉ khi `pending`/`confirmed`) |
| GET | `/admin/orders` | admin/staff | Liệt kê tất cả đơn (filter, search) |
| PATCH | `/admin/orders/{code}/status` | admin/staff | Đổi trạng thái + ghi `order_status_history` |
| POST | `/admin/orders/{code}/refund` | admin | Hoàn tiền |

### 7.8. Payment (`/payments`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/payments/checkout` | user | Tạo session thanh toán cho đơn (trả về URL redirect) |
| GET | `/payments/{orderCode}/status` | user | Tra cứu trạng thái |
| POST | `/payments/webhook/{provider}` | – | Webhook gateway (vnpay/momo/zalopay/stripe) – verify chữ ký |
| POST | `/payments/{id}/refund` | admin | Hoàn tiền |

### 7.9. Shipping (`/shipping`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/shipping/methods` | – | Liệt kê phương thức vận chuyển |
| POST | `/shipping/quote` | user | Tính phí ship `{ methodId, address, items }` |
| GET | `/admin/shipments` | admin/staff | Liệt kê shipment |
| PATCH | `/admin/shipments/{id}` | admin/staff | Cập nhật `tracking_number`, `status` |

### 7.10. Review (`/reviews`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/products/{id}/reviews` | – | Liệt kê review (status=approved) – paging |
| POST | `/products/{id}/reviews` | user | Tạo review (kiểm tra đã mua qua `order_item_id`) |
| PATCH | `/admin/reviews/{id}/status` | admin/staff | Duyệt / từ chối |

### 7.11. Wishlist (`/wishlist`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/wishlist` | user | Lấy danh sách |
| POST | `/wishlist/{productId}` | user | Thêm |
| DELETE | `/wishlist/{productId}` | user | Bỏ yêu thích |

### 7.12. Upload (`/uploads`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/uploads/image` | user/admin | Upload 1 ảnh (multipart), trả về URL |
| POST | `/uploads/images` | admin | Upload nhiều ảnh |

### 7.13. Admin Dashboard (`/admin/reports`)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/admin/reports/overview` | admin | Tổng quan: doanh thu, đơn, user, sản phẩm |
| GET | `/admin/reports/revenue?from=&to=&groupBy=day\|month` | admin | Doanh thu theo thời gian |
| GET | `/admin/reports/top-products` | admin | Top sản phẩm bán chạy |
| GET | `/admin/reports/low-stock` | admin/staff | SP sắp hết hàng |

---

## 8. Quy tắc nghiệp vụ (Business Rules)

### 8.1. Đặt đơn (`POST /orders`)

Trong **một transaction**:
1. Validate user đã verify email.
2. Validate địa chỉ giao hàng thuộc về user.
3. Validate giỏ hàng còn item; mỗi item phải có `variant.stock_quantity >= quantity`.
4. Tính `subtotal = SUM(unit_price * quantity)`.
5. Nếu có voucher:
   - Kiểm tra trạng thái `active`, thời gian, `min_order_amount`, `usage_limit`, `usage_limit_per_user`.
   - Tính `discount_amount` (cap theo `max_discount_amount`).
6. Tính `shipping_fee` theo `shipping_method` + địa chỉ.
7. `total_amount = subtotal + shipping_fee - discount_amount`.
8. Tạo `orders`, `order_items` (snapshot tên, giá, sku, variant_name).
9. Trừ `stock_quantity` từng variant; tăng `sold_count` của product.
10. Ghi `order_status_history` (`null → pending`).
11. Tạo `payments` (status=pending) và `shipments` (status=preparing).
12. Nếu dùng voucher: tạo `voucher_usages`, `vouchers.used_count++`.
13. Đổi cart sang `status=converted`.
14. Sinh `order_code` theo format `ORD-YYYYMMDD-XXXX`.
15. Gửi email xác nhận (async).

> Mọi bước rollback nếu fail. Dùng **`@Transactional(rollbackFor = Exception.class)`**.

### 8.2. Chuyển trạng thái đơn

| Từ → Đến | Ai được phép |
|---|---|
| `pending → confirmed` | admin/staff (sau khi xác minh) hoặc auto (paid online) |
| `confirmed → processing` | staff |
| `processing → shipping` | staff (khi bàn giao đơn vị vận chuyển) |
| `shipping → delivered` | webhook ĐVVC hoặc staff |
| `delivered → completed` | auto sau N ngày không khiếu nại (mặc định 7) |
| `pending/confirmed → cancelled` | customer (huỷ đơn) hoặc admin |
| `delivered → returned` | admin (sau khi xác minh) |

Mỗi lần đổi → ghi `order_status_history` (`from_status`, `to_status`, `changed_by`, `note`). Khi `cancelled` / `returned` → **hoàn lại tồn kho**.

### 8.3. Voucher

- Kiểm tra `start_date <= now <= end_date`, `status='active'`.
- `used_count < usage_limit` (nếu set).
- Số lần user đã dùng (`voucher_usages` GROUP BY user) `< usage_limit_per_user`.
- `subtotal >= min_order_amount`.
- Với `percentage`: `discount = min(subtotal * value/100, max_discount_amount)`.
- Với `fixed_amount`: `discount = min(value, subtotal)`.

### 8.4. Tồn kho & race condition

- Dùng **pessimistic lock** (`SELECT ... FOR UPDATE` hoặc `@Lock(PESSIMISTIC_WRITE)`) khi trừ kho.
- Hoặc kiểm tra optimistic bằng cột `version` (thêm `@Version`).
- Cron job tự huỷ đơn `pending` quá 30 phút chưa thanh toán online → hoàn kho.

### 8.5. Review

- Chỉ user có `order_items.order_id` ở trạng thái `delivered`/`completed` mới được review.
- Mỗi `order_item_id` chỉ review 1 lần (`UNIQUE(user_id, order_item_id)`).
- Review tạo mới ở status `pending`, admin/staff duyệt → `approved`/`rejected`.
- Khi `approved` → cập nhật `products.rating_avg`, `rating_count`.

### 8.6. Giỏ hàng guest

- Header `X-Session-Id` (UUID do FE sinh) định danh giỏ guest.
- Khi user login → gọi `POST /cart/merge` với sessionId cũ; hệ thống merge item theo (productId, variantId), cộng quantity.
- Cron dọn cart abandoned > 30 ngày.

---

## 9. Validation rules (input)

Dùng `jakarta.validation` annotations + custom validator:

| Field | Rule |
|---|---|
| `email` | `@Email`, max 150 |
| `password` | min 8, có chữ + số (regex) |
| `phone` | `^(0[0-9]{9})$` |
| `quantity` | `@Min(1)`, `@Max(999)` |
| `rating` | `@Min(1)`, `@Max(5)` |
| `discountValue` | `> 0`, nếu `percentage` thì `<= 100` |
| `endDate` | `> startDate` (custom @ValidVoucherPeriod) |
| `slug` | regex `^[a-z0-9-]+$` |

---

## 10. Exception handling

`@RestControllerAdvice` xử lý tập trung:

| Exception | HTTP | Code |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `BadCredentialsException` | 401 | `INVALID_CREDENTIALS` |
| `AccessDeniedException` | 403 | `FORBIDDEN` |
| `ResourceNotFoundException` | 404 | `NOT_FOUND` |
| `BusinessException` (custom) | 409/422 | code động (vd: `OUT_OF_STOCK`, `VOUCHER_EXPIRED`, `INVALID_ORDER_STATUS`) |
| `DataIntegrityViolationException` | 409 | `DUPLICATE_RESOURCE` |
| `Exception` | 500 | `INTERNAL_ERROR` (log full stack, hide details ở prod) |

Tất cả lỗi trả format chuẩn `ApiResponse` ở mục 7.

---

## 11. Tích hợp ngoài

### 11.1. Cổng thanh toán

| Provider | Tài liệu cần | Triển khai |
|---|---|---|
| **VNPay** | `vnp_TmnCode`, `vnp_HashSecret` | Sinh URL redirect, verify checksum khi callback `vnp_ResponseCode='00'` → update `payments.status='success'` |
| **MoMo** | `partnerCode`, `accessKey`, `secretKey` | Tương tự, IPN webhook |
| **ZaloPay** | `app_id`, `key1`, `key2` | – |
| **Stripe** (thẻ quốc tế) | `sk_xxx`, `webhook_secret` | – |
| **COD** | – | Thu tiền lúc giao, set `payments.status='success'` khi đơn `delivered` |

> Webhook **bắt buộc verify chữ ký** trước khi trust dữ liệu. Idempotent theo `transaction_code`.

### 11.2. Đơn vị vận chuyển

- GHN, GHTK – cung cấp adapter để tạo đơn ship + lấy tracking. Kết quả lưu vào `shipments.tracking_number`, `carrier`.

### 11.3. Email

- Template HTML (Thymeleaf) cho:
  - Welcome / verify email
  - Reset password
  - Xác nhận đơn hàng
  - Cập nhật trạng thái đơn (đang giao, đã giao)
- Gửi async qua `@Async` + thread pool.

### 11.4. Lưu trữ file

- Dev: lưu local `uploads/` và serve qua `/static/uploads/**`.
- Prod: AWS S3 / MinIO, trả về CDN URL.
- Validate MIME (`image/jpeg`, `image/png`, `image/webp`), dung lượng < 5 MB, tự sinh tên (UUID).

---

## 12. Cấu hình `application.yml`

```yaml
spring:
  application:
    name: ecommerce-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ecommerce_db}?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Ho_Chi_Minh
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: validate         # luôn validate, schema do Flyway quản
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        jdbc.time_zone: Asia/Ho_Chi_Minh
  flyway:
    enabled: true
    locations: classpath:db/migration
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379

server:
  port: ${PORT:8080}
  servlet:
    context-path: /api

app:
  jwt:
    secret: ${JWT_SECRET}
    access-ttl-minutes: 15
    refresh-ttl-days: 7
  cors:
    allowed-origins: http://localhost:5173,http://localhost:3000
  upload:
    dir: ./uploads
    max-size-mb: 5
  payment:
    vnpay:
      tmn-code: ${VNPAY_TMN_CODE}
      hash-secret: ${VNPAY_HASH_SECRET}
      url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
      return-url: ${APP_URL}/payment/vnpay/return
```

Biến môi trường nhạy cảm (`JWT_SECRET`, `DB_PASSWORD`, `*_SECRET`) **không** commit vào Git, đặt trong `.env`.

---

## 13. Bảo mật

- **HTTPS** bắt buộc ở production.
- **CORS** giới hạn theo whitelist origin của FE.
- **Rate limiting** (Bucket4j) cho `/auth/login`, `/auth/forgot-password`: 5 req/phút/IP.
- **CSRF** disable (vì JWT stateless), nhưng bật cho endpoint webhook nếu cần.
- **SQL Injection**: dùng JPA / parameter binding, không string-concat query.
- **XSS**: escape output, không trả HTML từ user input.
- **Mass assignment**: dùng DTO riêng cho request, không bind trực tiếp vào Entity.
- **Audit log**: ghi lại các hành động quan trọng của admin (đổi trạng thái đơn, refund, đổi role).

---

## 14. Logging & Monitoring

- Logback theo cấu trúc JSON ở prod, console ở dev.
- Mỗi request gắn `X-Request-Id` (MDC) để trace cross-service.
- Tích hợp:
  - **Spring Actuator**: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.
  - **Sentry / ELK / Grafana** (tuỳ môi trường).

---

## 15. Testing

| Loại | Phạm vi | Công cụ |
|---|---|---|
| Unit | Service, util | JUnit 5, Mockito |
| Repository | Custom query | `@DataJpaTest` + Testcontainers MySQL |
| Integration | Full flow (đặt đơn) | `@SpringBootTest` + Testcontainers |
| API contract | Endpoint shape | Spring REST Docs hoặc Swagger snapshot |
| Load | Stress / capacity | k6 / JMeter |

Coverage mục tiêu: **>= 70%** cho service layer.

---

## 16. CI/CD & Deployment

### 16.1. Docker

- `Dockerfile` build multi-stage (Maven build → JRE 17 slim).
- `docker-compose.yml` chạy: `app`, `mysql`, `redis`, `mailhog` (dev).

### 16.2. Pipeline (GitHub Actions)

1. Checkout
2. Setup JDK 17 + cache Maven
3. `mvn -B verify` (test + build)
4. Build Docker image, push registry
5. Deploy (SSH / k8s)

### 16.3. Environment

| Env | Profile | DB |
|---|---|---|
| Dev | `dev` | local MySQL |
| Staging | `staging` | RDS test |
| Prod | `prod` | RDS prod (replica đọc) |

---

## 17. Tài liệu API

- **Swagger UI**: `/swagger-ui.html` (dev/staging), tắt ở prod hoặc bảo vệ basic auth.
- Mọi endpoint có `@Operation`, `@ApiResponse`, ví dụ request/response.
- Export OpenAPI spec ra `docs/openapi.yaml` để FE tự generate client.

---

## 18. Checklist khởi tạo project

- [ ] Khởi tạo project bằng [Spring Initializr](https://start.spring.io) (Web, JPA, Validation, Security, MySQL, Lombok, Flyway, Mail).
- [ ] Cấu hình Flyway + viết migration `V1__init.sql` (dán nội dung từ `Database/schema.sql`).
- [ ] Tạo entity tương ứng từng bảng (đặt trong module phù hợp).
- [ ] Tạo `BaseEntity` (`createdAt`, `updatedAt`) bằng `@MappedSuperclass` + `@EntityListeners(AuditingEntityListener.class)`.
- [ ] Cấu hình Spring Security + JWT filter.
- [ ] Tạo Global Exception Handler + ApiResponse wrapper.
- [ ] Module **auth** + **user** trước.
- [ ] Module **catalog** (đọc) → cho FE chạy được trang sản phẩm.
- [ ] Module **cart** → **order** → **payment**.
- [ ] Tích hợp Swagger.
- [ ] Viết test cho `OrderService.placeOrder()` (case happy + out-of-stock + voucher invalid).
- [ ] Dockerize + docker-compose.
- [ ] Setup CI.

---

## 19. Lộ trình phát triển đề xuất

| Sprint | Phạm vi |
|---|---|
| 1 | Init project, Flyway, Auth, User, Address, Swagger |
| 2 | Catalog (Category, Brand, Product, Variant, Image, Attribute) – Public + Admin |
| 3 | Cart (user + guest), Wishlist |
| 4 | Voucher, Shipping methods, Order (preview + place) |
| 5 | Payment (VNPay/MoMo + COD), Order status flow, Email |
| 6 | Review, Admin dashboard reports, Upload S3 |
| 7 | Hardening: cache Redis, rate limit, audit log, monitoring |
| 8 | Test, performance, deploy production |

---

## 20. Phụ lục – Mẫu request/response chính

### 20.1. `POST /api/v1/auth/login`

**Request**
```json
{ "email": "an.nguyen@gmail.com", "password": "123456" }
```

**Response 200**
```json
{
  "success": true,
  "code": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 3, "email": "an.nguyen@gmail.com",
      "fullName": "Nguyễn Văn An", "role": "customer"
    }
  },
  "timestamp": "2026-04-25T13:45:00+07:00"
}
```

### 20.2. `POST /api/v1/orders`

**Request**
```json
{
  "addressId": 1,
  "shippingMethodId": 1,
  "paymentMethod": "vnpay",
  "voucherCode": "WELCOME10",
  "note": "Giao giờ hành chính",
  "items": [
    { "productId": 1, "variantId": 2, "quantity": 2 },
    { "productId": 2, "variantId": 6, "quantity": 1 }
  ]
}
```

**Response 201**
```json
{
  "success": true,
  "code": 201,
  "message": "Đặt đơn thành công",
  "data": {
    "orderCode": "ORD-20260425-0006",
    "subtotal": 1288000,
    "shippingFee": 35000,
    "discountAmount": 100000,
    "totalAmount": 1223000,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TmnCode=...",
    "status": "pending"
  }
}
```

### 20.3. Lỗi 422 – hết hàng

```json
{
  "success": false,
  "code": 422,
  "message": "Sản phẩm không đủ tồn kho",
  "errors": [
    { "field": "items[0].variantId", "code": "OUT_OF_STOCK", "message": "Variant 'TS-UNQ-001-BK-M' chỉ còn 1 sản phẩm" }
  ]
}
```

---

> Tài liệu này là **kim chỉ nam** cho team backend. Mọi thay đổi schema phải đi kèm migration Flyway và cập nhật module tương ứng. Mọi endpoint mới phải bổ sung Swagger + test.
