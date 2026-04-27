# E-commerce Backend (Spring Boot)

REST API cho hệ thống thương mại điện tử, được xây dựng theo `BackEnd/REQUIREMENTS.md` và bám sát schema trong `Database/schema.sql`.

## 1. Tech stack

- **Java 17**, **Spring Boot 3.3.4**
- **Spring Security** + **JWT** (`jjwt 0.12.6`)
- **Spring Data JPA** + **Hibernate**
- **MySQL 8.0+** + **Flyway** (DB migration)
- **springdoc-openapi** (Swagger UI)
- **Lombok**, **Maven**, **Docker**

## 2. Cấu trúc thư mục

```
BackEnd/
├── pom.xml
├── Dockerfile / docker-compose.yml / .env.example
└── src/main/
    ├── java/com/ecommerce/
    │   ├── EcommerceApplication.java
    │   ├── config/             SecurityConfig, OpenApiConfig, WebConfig, AsyncConfig
    │   ├── common/             BaseEntity, ApiResponse, PageResponse, exceptions
    │   ├── security/           JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal
    │   └── modules/
    │       ├── auth/           Đăng ký / đăng nhập / refresh / đổi mật khẩu
    │       ├── user/           Profile + đa địa chỉ
    │       ├── catalog/        Category, Brand, Product (+ images, attributes, variants)
    │       ├── cart/           Giỏ hàng (user + guest), merge
    │       ├── voucher/        Voucher + validate + admin CRUD
    │       ├── shipping/       Shipping methods + shipments
    │       ├── order/          Preview, place, status flow, scheduler
    │       ├── payment/        Checkout + webhook (VNPay stub)
    │       ├── review/         Review (chỉ user đã mua), admin moderate
    │       ├── wishlist/
    │       └── upload/         Upload local
    └── resources/
        ├── application.yml + application-dev.yml + application-prod.yml
        └── db/migration/V1__init_schema.sql, V2__seed_basic_data.sql
```

## 3. Yêu cầu môi trường

- **JDK 17+ (BẮT BUỘC)** – code dùng records, text blocks, pattern matching. Không build được trên Java 8/11.
  - Tải tại: https://adoptium.net/temurin/releases/?version=17
- Maven 3.9+ (hoặc dùng Maven trong IDE)
- MySQL 8.0+
- (Tuỳ chọn) Docker Desktop – nếu chỉ có Java cũ trên máy, vẫn build được qua Docker

> ✅ Đã verify build thành công với `maven:3.9-eclipse-temurin-17` (147 source files).

## 4. Cách chạy

### 4.1. Chạy bằng Docker (khuyên dùng)

```bash
cd BackEnd
cp .env.example .env             # nếu cần chỉnh biến môi trường
docker compose up -d --build
```

Các container:
- `ecommerce-mysql` (MySQL 8) – cổng 3306
- `ecommerce-backend` (Spring Boot) – cổng 8080
- `ecommerce-mailhog` – cổng 8025 (UI), 1025 (SMTP)

Flyway sẽ tự chạy `V1__init_schema.sql` và `V2__seed_basic_data.sql` lần đầu.

### 4.2. Chạy local (không Docker)

1. Tạo MySQL 8 và DB:
   ```sql
   CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Cấu hình biến môi trường (hoặc sửa `application.yml`):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=ecommerce_db
   export DB_USER=root
   export DB_PASSWORD=root
   export JWT_SECRET=your-256-bit-secret-please-change-in-prod
   ```
3. Chạy:
   ```bash
   mvn clean spring-boot:run
   # hoặc build jar:
   mvn clean package -DskipTests
   java -jar target/ecommerce-backend.jar
   ```

API sẵn sàng tại `http://localhost:8080/api`.

## 5. Truy cập Swagger / Tài liệu API

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## 6. Tài khoản mặc định (sau Flyway V2 seed)

| Email | Vai trò | Mật khẩu |
|---|---|---|
| `admin@shop.vn` | admin | `Admin@123` |
| `staff01@shop.vn` | staff | `Admin@123` |

> Hash trong seed là BCrypt strength 12 cho `Admin@123`. Hãy đổi password ngay sau lần đăng nhập đầu tiên.

## 7. Endpoint chính (tóm tắt)

Base URL: `http://localhost:8080/api/v1`

### Auth (`/auth`)
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/change-password`
- `POST /auth/logout`

### Profile / Address (`/me`)
- `GET / PUT /me`
- `GET / POST /me/addresses`
- `PUT / DELETE /me/addresses/{id}`
- `PATCH /me/addresses/{id}/default`

### Catalog (public)
- `GET /categories` (cây), `GET /categories/{slug}`
- `GET /brands`
- `GET /products` (filter: `categoryId`, `brandId`, `q`, `minPrice`, `maxPrice`, `page`, `size`, `sort`)
- `GET /products/featured`
- `GET /products/{slug}` (auto increment view_count)
- `GET /search?q=`

### Catalog (admin)
- `GET /admin/products` – danh sách phân trang (lọc `status`, `categoryId`, `brandId`, `q`, `minPrice`, `maxPrice`, `page`, `size`, `sort`)
- `POST/PUT/DELETE /admin/categories[/{id}]`
- `POST/PUT/DELETE /admin/brands[/{id}]`
- `POST/PUT/DELETE /admin/products[/{id}]`
- `POST /admin/products/{id}/variants`, `DELETE /admin/products/{id}/variants/{vid}`

### Cart (`/cart`) — user + guest (header `X-Session-Id`)
- `GET /cart`
- `POST /cart/items`, `PUT /cart/items/{id}`, `DELETE /cart/items/{id}`
- `DELETE /cart`
- `POST /cart/merge` (sau khi login)

### Voucher
- `GET /vouchers/active`
- `POST /vouchers/validate`
- `GET / POST / PUT / DELETE /admin/vouchers`

### Shipping
- `GET /shipping/methods`

### Order (customer)
- `POST /orders/preview` – tính trước subtotal/ship/discount
- `POST /orders` – đặt đơn (transaction trừ kho, ghi history, tạo payment + shipment)
- `GET /orders` – đơn của tôi
- `GET /orders/{code}` – chi tiết
- `POST /orders/{code}/cancel` – huỷ đơn (chỉ pending/confirmed)

### Order (admin)
- `GET /admin/orders`, `GET /admin/orders/{code}`
- `PATCH /admin/orders/{code}/status` – validate state machine
- `POST /admin/orders/{code}/cancel`

### Payment
- `POST /payments/checkout/{orderCode}` – sinh URL gateway
- `GET /payments/{orderCode}` – list giao dịch
- `POST /payments/webhook/{provider}` – callback gateway

### Review / Wishlist / Upload
- `GET /products/{id}/reviews`, `POST /products/{id}/reviews`
- `PATCH /admin/reviews/{id}/status?status=approved|rejected`
- `GET /wishlist`, `POST /wishlist/{productId}`, `DELETE /wishlist/{productId}`
- `POST /uploads/image` (multipart)

## 8. Quy ước response

```json
{
  "success": true,
  "code": 200,
  "message": "OK",
  "data": { ... },
  "timestamp": "2026-04-25T14:30:00+07:00"
}
```

Lỗi:
```json
{
  "success": false,
  "code": 422,
  "message": "Sản phẩm không đủ tồn kho",
  "errors": [
    { "field": "items[0].variantId", "code": "OUT_OF_STOCK", "message": "Variant chỉ còn 1 sản phẩm" }
  ]
}
```

## 9. Authentication

Mọi endpoint không public yêu cầu header:
```
Authorization: Bearer <accessToken>
```

Refresh token TTL 7 ngày, access token TTL 15 phút (cấu hình trong `application.yml`).

## 10. Tính năng nghiệp vụ đặc biệt

| Tính năng | Triển khai |
|---|---|
| **Sản phẩm có nhiều biến thể** | `ProductVariant` + `ProductAttributeValue` (N-N qua `ProductVariantValue`) |
| **Giỏ hàng guest** | `Cart.user_id` nullable, dùng header `X-Session-Id`. `POST /cart/merge` khi login |
| **Voucher** | Validate đầy đủ: status, period, `min_order_amount`, `usage_limit`, `usage_limit_per_user`, cap `max_discount_amount` |
| **Lịch sử đơn hàng** | `order_status_history` tự động ghi khi place / update status / cancel |
| **State machine đơn** | `OrderService.validateTransition()` với map allowed transitions |
| **Race condition tồn kho** | `@Lock(PESSIMISTIC_WRITE)` trên `findByIdForUpdate` + `decreaseStock` UPDATE … WHERE stock >= qty |
| **Auto-cancel** | `OrderScheduler` chạy mỗi 5 phút, huỷ đơn pending online > 30 phút và hoàn kho |
| **Auto-complete** | Cron 1h sáng, auto chuyển `delivered → completed` sau 7 ngày |

## 11. Hardening cho production

- [ ] Đổi `JWT_SECRET` thành chuỗi 256-bit random.
- [ ] Đặt `SPRING_PROFILES_ACTIVE=prod`.
- [ ] Tắt Swagger UI hoặc bảo vệ bằng basic auth.
- [ ] Triển khai HTTPS (reverse proxy nginx/Traefik).
- [ ] Thay local upload bằng S3/MinIO.
- [ ] Tích hợp Redis cho rate-limit + refresh-token blacklist.
- [ ] Triển khai full HMAC verify cho VNPay/MoMo webhook.
- [ ] Setup CI/CD + monitoring (Prometheus, Sentry, ELK).

## 12. Test với cURL

```bash
# Đăng ký
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@gmail.com","phone":"0900000000","password":"12345678","fullName":"Test User"}'

# Đăng nhập admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@shop.vn","password":"Admin@123"}'

# Lấy danh sách category (public)
curl http://localhost:8080/api/v1/categories

# Lấy giỏ hàng cho guest
curl http://localhost:8080/api/v1/cart -H "X-Session-Id: my-guest-session-001"

# Đặt đơn (sau khi login)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"addressId":1,"shippingMethodId":1,"paymentMethod":"cod","items":[{"productId":1,"variantId":1,"quantity":2}]}'
```

## 13. Khắc phục sự cố

| Triệu chứng | Cách xử lý |
|---|---|
| `Schema validation: missing column ...` | Flyway chưa chạy hoặc DB cũ. Drop database + chạy lại migration |
| `JWT secret too short` | Đặt biến `JWT_SECRET` ≥ 32 ký tự |
| Lỗi connect MySQL trong Docker | Đợi MySQL `healthy` (`docker compose logs mysql`) |
| `Address already in use 8080` | Đổi `PORT` trong `.env` |

---

> Các module **đã có** đầy đủ controller / service / repository / DTO. Một số phần như **forgot-password email**, **MapStruct**, **Redis**, **Stripe SDK thật** được mô tả trong REQUIREMENTS.md và để TODO mở rộng.
