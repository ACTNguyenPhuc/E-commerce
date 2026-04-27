# Admin Dashboard – Thiết kế & Yêu cầu chi tiết

> Tài liệu mô tả chi tiết **giao diện Admin/Staff Dashboard** cho hệ thống thương mại điện tử. Bám sát:
> - Database: `Database/schema.sql`
> - Backend API: `BackEnd/README.md` & `BackEnd/REQUIREMENTS.md`

---

## 1. Tổng quan

| Hạng mục | Giá trị |
|---|---|
| Tên dự án | `ecommerce-admin-dashboard` |
| Mục tiêu | Cung cấp giao diện Single Page App cho **admin** và **staff** quản lý toàn bộ nghiệp vụ shop |
| Người dùng | `admin` (toàn quyền) và `staff` (quyền giới hạn) |
| Backend | `http://localhost:8080/api/v1/...` |
| Style | Dashboard hiện đại, responsive desktop-first (≥ 1280px), tablet OK, mobile dùng được nhưng không tối ưu |

---

## 2. Tech Stack

| Lớp | Công nghệ | Phiên bản |
|---|---|---|
| Framework | **React** + **TypeScript** | 18.x / TS 5.x |
| Build tool | **Vite** | 5.x |
| Routing | **React Router** | v6 |
| State global | **Zustand** | 4.x |
| Data fetching / cache | **TanStack Query (React Query)** | v5 |
| HTTP client | **Axios** (interceptors JWT + refresh) | 1.7+ |
| UI Library | **Ant Design** (recommended) hoặc **shadcn/ui + Tailwind CSS** | AntD 5.x / Tailwind 3.x |
| Form & validate | **React Hook Form** + **Zod** | RHF 7.x / Zod 3.x |
| Chart | **Recharts** | 2.x |
| Tables nâng cao | AntD `<Table>` hoặc **TanStack Table** | – |
| Date utils | **dayjs** (tiếng Việt) | 1.x |
| i18n | **react-i18next** | – |
| Icon | `@ant-design/icons` hoặc `lucide-react` | – |
| Notification | AntD `notification`, `message` (hoặc `sonner`) | – |
| Linter / format | ESLint + Prettier | – |
| Test (tuỳ chọn) | Vitest + React Testing Library | – |

> **Khuyến nghị mặc định trong tài liệu này: React + TypeScript + Vite + Ant Design 5 + TanStack Query + Zustand.** Lý do: AntD có sẵn rất nhiều component dashboard (Table, Form, Drawer, Tabs, DatePicker, Upload, Tree, Statistic) → giảm 60% công sức UI.

---

## 3. Cấu trúc thư mục

```
FrontEnd/Dashboard/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── .env.example                # VITE_API_URL=http://localhost:8080/api
├── index.html
├── public/
│   └── favicon.svg
└── src/
    ├── main.tsx                # entry: render <App />
    ├── App.tsx                 # router + theme + QueryClientProvider
    ├── router.tsx              # createBrowserRouter, route guards
    │
    ├── api/                    # API client + per-module API services
    │   ├── client.ts           # axios instance, interceptors, refresh-token logic
    │   ├── auth.api.ts
    │   ├── user.api.ts
    │   ├── catalog.api.ts
    │   ├── order.api.ts
    │   ├── voucher.api.ts
    │   ├── shipping.api.ts
    │   ├── payment.api.ts
    │   ├── review.api.ts
    │   ├── upload.api.ts
    │   └── report.api.ts
    │
    ├── stores/                 # Zustand stores
    │   ├── authStore.ts        # token, currentUser, login/logout
    │   ├── uiStore.ts          # sidebar collapsed, theme
    │   └── notifyStore.ts
    │
    ├── hooks/                  # custom hooks
    │   ├── useAuth.ts
    │   ├── useDebounce.ts
    │   ├── usePermission.ts    # canDoAction(role, resource, action)
    │   └── useTable.ts         # giúp với pagination/sort/filter
    │
    ├── types/                  # TypeScript types từ backend DTO
    │   ├── api.ts              # ApiResponse<T>, PageResponse<T>, ApiError
    │   ├── user.ts             # User, Role, Address, Gender
    │   ├── catalog.ts          # Category, Brand, Product, Variant...
    │   ├── order.ts            # Order, OrderItem, OrderStatus...
    │   ├── voucher.ts
    │   ├── payment.ts
    │   ├── shipping.ts
    │   ├── review.ts
    │   └── enums.ts
    │
    ├── components/             # shared UI components
    │   ├── layout/
    │   │   ├── AppLayout.tsx   # Header + Sidebar + Content + Footer
    │   │   ├── Sidebar.tsx     # menu chính, collapse được
    │   │   ├── Header.tsx      # search, notifications, profile menu
    │   │   ├── Breadcrumb.tsx
    │   │   └── PageHeader.tsx  # tiêu đề + action buttons
    │   ├── common/
    │   │   ├── DataTable.tsx           # wrapper Table với pagination, search, sort
    │   │   ├── StatusTag.tsx           # render trạng thái có màu
    │   │   ├── PriceDisplay.tsx        # format VND
    │   │   ├── EmptyState.tsx
    │   │   ├── ConfirmModal.tsx
    │   │   ├── ImageUploader.tsx
    │   │   ├── RichTextEditor.tsx      # TinyMCE/CKEditor cho description
    │   │   ├── DateRangeFilter.tsx
    │   │   ├── PermissionGate.tsx      # ẩn UI nếu không có quyền
    │   │   └── ErrorBoundary.tsx
    │   └── charts/
    │       ├── RevenueChart.tsx        # line/bar
    │       ├── OrderStatusPieChart.tsx
    │       └── KPICard.tsx
    │
    ├── pages/
    │   ├── auth/
    │   │   ├── LoginPage.tsx
    │   │   └── ForgotPasswordPage.tsx
    │   ├── dashboard/
    │   │   └── DashboardPage.tsx       # tổng quan KPI + charts
    │   ├── users/
    │   │   ├── UsersListPage.tsx
    │   │   └── UserDetailDrawer.tsx
    │   ├── catalog/
    │   │   ├── CategoriesPage.tsx      # tree + CRUD
    │   │   ├── BrandsPage.tsx          # table + CRUD
    │   │   ├── AttributesPage.tsx      # tab Thuộc tính + Giá trị
    │   │   ├── ProductsListPage.tsx
    │   │   ├── ProductFormPage.tsx     # tạo/sửa product (multi-tab)
    │   │   └── ProductVariantsTab.tsx  # tab quản lý variants
    │   ├── orders/
    │   │   ├── OrdersListPage.tsx
    │   │   ├── OrderDetailPage.tsx     # chi tiết + lịch sử + actions
    │   │   └── OrderStatusFlow.tsx     # component stepper
    │   ├── payments/
    │   │   └── PaymentsListPage.tsx
    │   ├── shipments/
    │   │   └── ShipmentsListPage.tsx
    │   ├── vouchers/
    │   │   ├── VouchersListPage.tsx
    │   │   └── VoucherFormDrawer.tsx
    │   ├── reviews/
    │   │   └── ReviewsListPage.tsx
    │   ├── shipping-methods/
    │   │   └── ShippingMethodsPage.tsx
    │   ├── reports/
    │   │   ├── RevenueReportPage.tsx
    │   │   ├── TopProductsPage.tsx
    │   │   └── LowStockPage.tsx
    │   ├── settings/
    │   │   ├── ProfilePage.tsx
    │   │   ├── ChangePasswordPage.tsx
    │   │   └── SystemSettingsPage.tsx
    │   └── errors/
    │       ├── NotFoundPage.tsx
    │       └── ForbiddenPage.tsx
    │
    ├── utils/
    │   ├── format.ts           # formatVnd, formatDate, formatPhone
    │   ├── status.ts           # mapping status → label/color
    │   ├── validators.ts       # Zod schemas
    │   └── constants.ts        # ROLES, ORDER_STATUSES, COLORS...
    │
    ├── styles/
    │   ├── theme.ts            # AntD ConfigProvider theme
    │   └── globals.css
    │
    └── i18n/
        ├── index.ts
        ├── vi.json
        └── en.json
```

---

## 4. Kiến trúc ứng dụng

### 4.1. Auth flow

```
┌─────────────┐  POST /v1/auth/login   ┌──────────────┐
│ LoginPage   │───────────────────────▶│ Backend      │
└─────────────┘  { accessToken, ... }  └──────────────┘
       │
       ▼
┌─────────────────────┐
│ authStore (Zustand) │  ←── lưu accessToken/refreshToken (localStorage)
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ axios interceptors  │  → tự gắn Authorization: Bearer <token>
│                     │  → 401 ⇒ gọi /auth/refresh, retry request
│                     │  → 403 ⇒ chuyển trang /forbidden
│                     │  → 5xx ⇒ notification.error
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Route guards        │  PrivateRoute, RoleRoute
└─────────────────────┘
```

### 4.2. Routing tree (đề xuất)

```
/login                                   (public)
/forgot-password                         (public)

/                                        (private layout)
├── /dashboard                           [admin, staff]
├── /users                               [admin]
│   └── /users/:id
├── /catalog
│   ├── /catalog/categories              [admin, staff]
│   ├── /catalog/brands                  [admin, staff]
│   ├── /catalog/attributes              [admin]
│   └── /catalog/products
│       ├── /catalog/products/new        [admin, staff]
│       └── /catalog/products/:id        [admin, staff]
├── /orders                              [admin, staff]
│   └── /orders/:code
├── /payments                            [admin, staff]
├── /shipments                           [admin, staff]
├── /vouchers                            [admin]
├── /reviews                             [admin, staff]
├── /shipping-methods                    [admin]
├── /reports
│   ├── /reports/revenue                 [admin]
│   ├── /reports/top-products            [admin, staff]
│   └── /reports/low-stock               [admin, staff]
├── /settings/profile                    [admin, staff]
├── /settings/change-password            [admin, staff]
└── /settings/system                     [admin]

/forbidden, /not-found                   (public)
```

### 4.3. State management

| Loại state | Lưu ở đâu | Ví dụ |
|---|---|---|
| **Server state** (data từ API) | TanStack Query cache | products, orders, users |
| **Auth** | Zustand `authStore` + localStorage | accessToken, currentUser |
| **UI** | Zustand `uiStore` | sidebar collapsed, theme, locale |
| **Form** | React Hook Form local | edit product, voucher |
| **URL** | React Router search params | filter status, page, q |

> **Quy tắc:** không lưu data từ API vào Zustand. Luôn dùng React Query để có cache + refetch + optimistic update.

---

## 5. Layout & Navigation

### 5.1. AppLayout (sau khi login)

```
┌─────────────────────────────────────────────────────────────────┐
│ Header (60px)                                                    │
│  Logo │ Breadcrumb         │   🔔  🔍  Avatar(role) ▾            │
├──────────────┬──────────────────────────────────────────────────┤
│              │                                                  │
│  Sidebar     │  Page content                                    │
│  (240px,     │   ┌──────────────────────────────────────┐       │
│   collapsi-  │   │ PageHeader: Title  + Action buttons  │       │
│   ble 80px)  │   ├──────────────────────────────────────┤       │
│              │   │                                       │       │
│  📊 Dashboard│   │  Content (table, form, chart, ...)   │       │
│  📦 Products │   │                                       │       │
│  🛒 Orders   │   └──────────────────────────────────────┘       │
│  💳 Payments │                                                  │
│  🚚 Shipments│                                                  │
│  🎟  Vouchers│                                                  │
│  ⭐ Reviews  │                                                  │
│  👥 Users    │                                                  │
│  📈 Reports  │                                                  │
│  ⚙  Settings │                                                  │
└──────────────┴──────────────────────────────────────────────────┘
```

### 5.2. Sidebar menu (cấu trúc cây)

```
📊 Dashboard
📦 Catalog
   ├─ Sản phẩm        (/catalog/products)
   ├─ Danh mục        (/catalog/categories)
   ├─ Thương hiệu     (/catalog/brands)
   └─ Thuộc tính      (/catalog/attributes)
🛒 Đơn hàng           (/orders) – có badge số đơn pending
💳 Thanh toán         (/payments)
🚚 Vận chuyển         (/shipments)
🎟  Khuyến mãi        (/vouchers)
⭐ Đánh giá           (/reviews) – badge số review pending
👥 Người dùng         (/users)
📈 Báo cáo
   ├─ Doanh thu       (/reports/revenue)
   ├─ Top sản phẩm    (/reports/top-products)
   └─ Tồn kho         (/reports/low-stock)
🚛 Phương thức ship   (/shipping-methods)
⚙  Cài đặt
   ├─ Profile         (/settings/profile)
   ├─ Đổi mật khẩu    (/settings/change-password)
   └─ Hệ thống        (/settings/system)
```

### 5.3. Header

- **Logo** (click → `/dashboard`)
- **Breadcrumb** auto theo route (`Catalog > Sản phẩm > Thêm mới`)
- **Search bar** (Ctrl+K) – tìm nhanh order code, product, user
- **Bell 🔔** – notifications (đơn mới, voucher sắp hết hạn, sản phẩm sắp hết kho)
- **Avatar dropdown**:
  - Profile
  - Đổi mật khẩu
  - Đăng xuất

---

## 6. Design System

### 6.1. Màu sắc

| Role | Hex | Dùng cho |
|---|---|---|
| Primary | `#1677ff` (AntD blue) | nút chính, link |
| Success | `#52c41a` | success state, paid, delivered |
| Warning | `#faad14` | pending, processing |
| Error | `#ff4d4f` | cancelled, failed, low stock |
| Neutral | `#f5f5f5` / `#fafafa` | background |

### 6.2. Typography

- Font: `Inter` hoặc `Roboto` (web), fallback `system-ui`.
- Cỡ: `12 / 14 (default) / 16 (h5) / 20 (h4) / 24 (h3) / 30 (h2)`.
- Line-height ≥ 1.5.

### 6.3. Status mapping (sử dụng nhất quán)

| Status | Label tiếng Việt | Tag color |
|---|---|---|
| **OrderStatus.pending** | Chờ xác nhận | gold |
| **OrderStatus.confirmed** | Đã xác nhận | blue |
| **OrderStatus.processing** | Đang chuẩn bị | cyan |
| **OrderStatus.shipping** | Đang giao | geekblue |
| **OrderStatus.delivered** | Đã giao | lime |
| **OrderStatus.completed** | Hoàn tất | green |
| **OrderStatus.cancelled** | Đã huỷ | red |
| **OrderStatus.returned** | Trả hàng | volcano |
| **PaymentStatus.pending** | Chờ thanh toán | gold |
| **PaymentStatus.paid** | Đã thanh toán | green |
| **PaymentStatus.failed** | Thất bại | red |
| **PaymentStatus.refunded** | Đã hoàn tiền | purple |
| **UserStatus.active** | Hoạt động | green |
| **UserStatus.inactive** | Tạm khoá | default |
| **UserStatus.banned** | Cấm | red |
| **VoucherStatus.active** | Đang chạy | green |
| **VoucherStatus.inactive** | Tắt | default |
| **VoucherStatus.expired** | Hết hạn | red |
| **ReviewStatus.pending** | Chờ duyệt | gold |
| **ReviewStatus.approved** | Đã duyệt | green |
| **ReviewStatus.rejected** | Từ chối | red |

> Triển khai trong `utils/status.ts` để tái sử dụng khắp app.

### 6.4. Bảng dữ liệu (DataTable component)

Tính năng chuẩn của `<DataTable>`:
- Pagination server-side: `page`, `size`, `sort`.
- Search box (debounce 300ms) → query param `q`.
- Filter dropdown theo cột (status, role, ...).
- Bulk action (chọn nhiều row → action: xoá, duyệt nhiều...).
- Sticky header.
- Export CSV (tuỳ chọn).
- Responsive: ẩn cột phụ trên màn nhỏ.
- Loading skeleton khi fetching.

### 6.5. Form chuẩn

- Layout: `vertical` cho form nhiều field, `horizontal` cho form ngắn.
- Validation: Zod + RHF, hiển thị lỗi dưới input.
- Submit button luôn ở **footer cố định** (form dài).
- Cancel + Save tách rõ.
- Disable Save khi form chưa dirty hoặc không hợp lệ.
- Auto save draft (tuỳ chọn cho product form).

---

## 7. Đặc tả chi tiết từng module

### 7.1. LoginPage (`/login`)

**Mục đích:** đăng nhập admin/staff.

**UI:**
```
┌──────────────────────────────────────────┐
│            E-commerce Admin              │
│                                          │
│         ┌─────────────────┐              │
│  Email  │ admin@shop.vn   │              │
│         └─────────────────┘              │
│         ┌─────────────────┐              │
│  Pass.  │ ********        │ 👁           │
│         └─────────────────┘              │
│  ☐ Ghi nhớ                Quên mật khẩu  │
│                                          │
│         ┌─────────────────┐              │
│         │   Đăng nhập     │              │
│         └─────────────────┘              │
└──────────────────────────────────────────┘
```

**Logic:**
- POST `/v1/auth/login` → lưu `accessToken`, `refreshToken`, `user` vào `authStore` + localStorage.
- Nếu `user.role` = `customer` → reject "Tài khoản không có quyền truy cập admin".
- Redirect về URL ban đầu (lưu trong `?redirect=` param) hoặc `/dashboard`.

**Validation:** email format, password ≥ 8.

### 7.2. DashboardPage (`/dashboard`)

**Mục đích:** tổng quan toàn hệ thống.

**Layout:**
```
┌────────────────────────────────────────────────────────────────┐
│ KPI cards (4 cột)                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │ Doanh thu│  │ Đơn hàng │  │ Khách    │  │ SP bán   │         │
│  │ hôm nay  │  │ chờ XN: 5│  │ mới: 12  │  │ chạy nhất│         │
│  │ 12.5M ▲  │  │          │  │          │  │ iPhone15 │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
├────────────────────────────────────────────────────────────────┤
│ Doanh thu 30 ngày qua (Line chart)                             │
│ ▁▃▅▇▅▃▁▃▅▇▅▃▁▃▅▇▅▃▁▃▅▇▅▃▁▃▅▇▅▃                                │
├──────────────────────────────────┬─────────────────────────────┤
│ Top 5 sản phẩm bán chạy           │ Phân bố trạng thái đơn      │
│ (Bar chart)                       │ (Pie chart)                 │
├──────────────────────────────────┴─────────────────────────────┤
│ Đơn hàng gần nhất (mini table 10 dòng)                         │
└────────────────────────────────────────────────────────────────┘
```

**API calls:**
- `GET /v1/admin/reports/overview`
- `GET /v1/admin/reports/revenue?from=...&to=...&groupBy=day`
- `GET /v1/admin/reports/top-products?limit=5`
- `GET /v1/admin/orders?size=10&sort=createdAt,desc`

**Tương tác:**
- Filter khoảng thời gian (DatePicker): hôm nay / 7 ngày / 30 ngày / tuỳ chọn.
- Click KPI card → điều hướng đến trang tương ứng.

### 7.3. UsersListPage (`/users`) – chỉ admin

**Bảng cột:**
| Cột | Nguồn |
|---|---|
| ID | `users.id` |
| Avatar + Họ tên | `users.full_name`, `avatar_url` |
| Email | `users.email` |
| Phone | `users.phone` |
| Role | `users.role` (Tag) |
| Status | `users.status` (Tag) |
| Đã xác minh email | `users.email_verified_at` (icon ✓/–) |
| Lần đăng nhập | `users.last_login_at` |
| Ngày tạo | `users.created_at` |
| Action | View, Khoá/Mở khoá, Đổi role |

**Filter:** keyword search, role, status, date range tạo.

**API:**
- `GET /v1/admin/users?q=&role=&status=&page=&size=&sort=`
- `PATCH /v1/admin/users/{id}/status` body `{ status: "banned" }`

**Drawer chi tiết user:** thông tin profile, danh sách địa chỉ, lịch sử đơn (10 đơn gần nhất), tổng giá trị mua, lần đăng nhập gần nhất.

### 7.4. CategoriesPage (`/catalog/categories`)

**UI:** bên trái cây (`<Tree>` AntD), bên phải form chi tiết.

```
┌─────────────────┬──────────────────────────────────────────┐
│ ▼ Thời trang   │  Tên: [_____________________________]    │
│   ├ Áo thun    │  Slug:[____________] (auto từ tên)       │
│   └ Quần jeans │  Danh mục cha: [Chọn hoặc trống]         │
│ ▼ Điện tử      │  Mô tả: [TextArea]                       │
│   ├ Điện thoại │  Ảnh: [Upload]                           │
│   └ Laptop     │  Thứ tự hiển thị: [0]                    │
│ ▶ Đồ gia dụng  │  Trạng thái: ⦿ Active  ○ Inactive        │
│ + Thêm danh mục│                                          │
│                 │  [Huỷ]  [Lưu]                            │
└─────────────────┴──────────────────────────────────────────┘
```

**API:**
- `GET /v1/categories` (trả tree)
- `POST/PUT/DELETE /v1/admin/categories[/{id}]`

**Validation:** name required, slug auto-generate khi gõ tên (toggle override), không cho chọn chính nó/con-cháu làm parent (cycle).

### 7.5. BrandsPage (`/catalog/brands`)

**UI:** Table + Drawer Form.

| Cột | Nguồn |
|---|---|
| Logo | `brands.logo_url` |
| Tên | `brands.name` |
| Slug | `brands.slug` |
| Số sản phẩm | `COUNT(products WHERE brand_id)` (cần endpoint hỗ trợ hoặc tính client) |
| Status | Tag |
| Action | Sửa, Xoá |

**API:**
- `GET /v1/brands`
- `POST/PUT/DELETE /v1/admin/brands[/{id}]`

### 7.6. AttributesPage (`/catalog/attributes`) – chỉ admin

**UI:** 2 tabs:
- **Tab "Thuộc tính"**: list `product_attributes` (Màu sắc, Kích thước...)
- **Tab "Giá trị"**: chọn 1 thuộc tính → list `product_attribute_values` của thuộc tính đó.

**Form value:** value text, color_code (color picker nếu attribute_id là Màu sắc), display_order.

### 7.7. ProductsListPage (`/catalog/products`)

**Bảng:**
| Cột | Nguồn |
|---|---|
| Thumbnail | `products.thumbnail_url` |
| SKU | `products.sku` |
| Tên | `products.name` |
| Danh mục | join `categories` |
| Thương hiệu | join `brands` |
| Giá gốc / Giá sale | `base_price` / `sale_price` |
| Tồn (tổng) | `SUM(variants.stock_quantity)` |
| Đã bán | `products.sold_count` |
| Status | Tag |
| Featured | Star icon |
| Action | View, Sửa, Xoá |

**Filter:** keyword, category (cascader 2 cấp), brand, status, price range, featured only.

**Hành động:**
- `POST /catalog/products/new` → ProductFormPage
- Click row → `/catalog/products/:id` → ProductFormPage edit mode

**API:**
- `GET /v1/products?...&size=20`
- `DELETE /v1/admin/products/{id}` (cần xác nhận – modal warning)

### 7.8. ProductFormPage (`/catalog/products/new`, `/catalog/products/:id`)

**Tabs:**
1. **Thông tin chung**
   - Tên, slug (auto), short_description, description (rich text).
   - Danh mục (cascader), thương hiệu (select).
   - SKU, base_price, sale_price.
   - Thumbnail (Upload).
   - Status (Select), Is featured (Switch).

2. **Ảnh sản phẩm**
   - Upload nhiều ảnh.
   - Drag & drop để sắp xếp display_order.
   - Đặt 1 ảnh là `is_primary`.
   - Alt text mỗi ảnh.

3. **Biến thể (Variants)**
   - Bước 1: chọn thuộc tính áp dụng (vd: Màu sắc + Kích thước).
   - Bước 2: chọn giá trị mỗi thuộc tính (vd: Đỏ, Xanh × S, M, L).
   - Bước 3: hệ thống tự generate matrix các tổ hợp → bảng để admin nhập:
     ```
     ┌────────┬─────┬───────────┬──────────┬─────────┐
     │ Màu    │ Size│   SKU     │   Giá    │  Tồn    │
     ├────────┼─────┼───────────┼──────────┼─────────┤
     │ Đỏ     │ S   │ TS-001-RS │ 199000   │ 50      │
     │ Đỏ     │ M   │ TS-001-RM │ 199000   │ 80      │
     │ Xanh   │ S   │ TS-001-BS │ 199000   │ 30      │
     │ Xanh   │ M   │ TS-001-BM │ 199000   │ 60      │
     └────────┴─────┴───────────┴──────────┴─────────┘
     ```
   - Mỗi variant có ảnh riêng (tuỳ chọn), weight_gram, sale_price.

4. **SEO** (tuỳ chọn): meta_title, meta_description.

**API:**
- `POST /v1/admin/products` → tạo product cha.
- `POST /v1/admin/products/{id}/variants` cho từng variant.
- `POST /v1/uploads/image` → trả URL → gán vào `thumbnail_url` / `product_images.image_url`.

### 7.9. OrdersListPage (`/orders`)

**Bảng:**
| Cột | Nguồn |
|---|---|
| Mã đơn | `orders.order_code` (link xem) |
| Khách hàng | `orders.recipient_name` + tooltip `recipient_phone` |
| Số mặt hàng | `COUNT(order_items)` |
| Tổng tiền | `total_amount` (format VND) |
| Phương thức TT | `payment_method` (icon) |
| TT thanh toán | `payment_status` (Tag) |
| Trạng thái | `status` (Tag) |
| Ngày đặt | `placed_at` |
| Action | Xem chi tiết |

**Filter quan trọng:**
- Status (multi-select).
- Payment method, payment status.
- Khoảng ngày `placed_at`.
- Khoảng tiền.
- Search theo `order_code` / SĐT khách.

**Bulk action:** xác nhận hàng loạt, in nhãn hàng loạt (tuỳ chọn).

**API:**
- `GET /v1/admin/orders?status=&from=&to=&q=&page=&size=&sort=createdAt,desc`

**Header KPI nhanh:** số đơn theo từng trạng thái (bấm để filter).

### 7.10. OrderDetailPage (`/orders/:code`)

**UI:**
```
┌──────────────────────────────────────────────────────────────┐
│ ← Quay lại  | Mã đơn: ORD-20260425-0001        [In] [Huỷ]    │
├──────────────────────────────────────────────────────────────┤
│ Stepper trạng thái:                                          │
│  pending ──▶ confirmed ──▶ processing ──▶ shipping ──▶ delivered│
│   ✓           ✓             ✓             ⏳             ○    │
│  [Chuyển sang: Đang giao ▾]   [Huỷ đơn]                      │
├──────────────────┬───────────────────────────────────────────┤
│ Sản phẩm (table) │  Tóm tắt:                                 │
│  ─ iPhone 15 Pro │   Subtotal:        28.990.000 ₫           │
│    SKU: ...      │   Phí ship:           +35.000 ₫           │
│    1 × 28.990.000│   Voucher:           -100.000 ₫           │
│    = 28.990.000  │   ───────────────────────────             │
│  ─ Áo thun ...   │   Tổng cộng:        28.925.000 ₫          │
├──────────────────┼───────────────────────────────────────────┤
│ Giao hàng:        │ Thanh toán:                               │
│  Nguyễn Văn A     │  Phương thức: VNPay                       │
│  0912345678       │  Trạng thái: Đã thanh toán ✓              │
│  123 Trần Thái... │  Mã GD: VNP_TXN_xxxx                      │
│  Hà Nội           │  Lúc: 15/04 10:30                         │
├──────────────────┴───────────────────────────────────────────┤
│ Lịch sử trạng thái (Timeline):                                │
│  • 15/04 10:00  Khách đặt đơn (customer)                      │
│  • 15/04 10:30  Xác nhận đơn (admin)                          │
│  • 15/04 13:00  Đang đóng gói (staff01)                       │
│  • ...                                                        │
└──────────────────────────────────────────────────────────────┘
```

**API:**
- `GET /v1/admin/orders/{code}` → chi tiết đầy đủ với items + history.
- `PATCH /v1/admin/orders/{code}/status` body `{ status, note }`.
- `POST /v1/admin/orders/{code}/cancel?note=...`.
- `GET /v1/payments/{orderCode}` → list giao dịch (admin có thể xem).

**Quy tắc UI cho transition:**
- Chỉ hiện các trạng thái kế tiếp hợp lệ trong dropdown (theo state machine ở backend).
- Yêu cầu nhập `note` cho mọi action quan trọng (cancel, returned).
- Disable nút "Huỷ đơn" nếu đơn đã `delivered/completed/cancelled`.

### 7.11. PaymentsListPage (`/payments`)

**Bảng:** id, order_code, payment_method, amount, transaction_code, status, paid_at.

**Filter:** status, method, date range, search transaction_code.

**Click row:** drawer chi tiết, hiển thị `gateway_response` (JSON pretty).

**Action (admin only):** đánh dấu refunded (mở modal nhập lý do), call `POST /v1/payments/{id}/refund`.

### 7.12. ShipmentsListPage (`/shipments`)

**Bảng:** order_code, tracking_number, carrier, status, shipped_at, delivered_at.

**Action (staff):** cập nhật `tracking_number`, đổi `status` (drawer/modal).

### 7.13. VouchersListPage (`/vouchers`) – chỉ admin

**Bảng:**
| Cột | Nguồn |
|---|---|
| Mã | `code` (font monospace) |
| Tên | `name` |
| Loại | `discount_type` (Tag: Percent / Fixed) |
| Giá trị | `discount_value` (format theo type) |
| Min order | `min_order_amount` |
| Đã dùng / Tổng | `used_count / usage_limit` |
| Hiệu lực | `start_date – end_date` |
| Status | Tag |

**Form drawer (tạo/sửa):**
- Code (uppercase, no space, max 50).
- Discount type (radio): Percentage / Fixed amount.
  - Nếu Percentage: hiện thêm "Giảm tối đa" (max_discount_amount).
- Discount value (% hoặc VND).
- Min order amount.
- Usage limit (tổng) + Usage limit per user.
- Start date / End date (DateRangePicker).
- Status.

**Validation Zod:**
- `endDate > startDate`.
- Nếu Percentage: `discount_value` 0-100.

**API:**
- `GET /v1/admin/vouchers`
- `POST/PUT/DELETE /v1/admin/vouchers[/{id}]`

### 7.14. ReviewsListPage (`/reviews`)

**Bảng:** product (thumbnail+name), user, rating (stars), title, status, created_at.

**Tab nhanh:** Pending / Approved / Rejected (badge số lượng).

**Action:**
- Click row → modal preview review đầy đủ.
- Bulk: Approve / Reject nhiều review cùng lúc.

**API:**
- `GET /v1/admin/reviews?status=pending` *(cần thêm endpoint admin list nếu chưa có)*
- `PATCH /v1/admin/reviews/{id}/status?status=approved|rejected`

### 7.15. ShippingMethodsPage (`/shipping-methods`) – chỉ admin

**UI:** Table CRUD.

**Trường:** code, name, description, base_fee, estimated_days_min/max, status.

### 7.16. RevenueReportPage (`/reports/revenue`)

**UI:**
- Filter: from, to, groupBy (day/week/month).
- Line chart doanh thu + số đơn theo ngày.
- Bảng chi tiết (export Excel/CSV).
- KPI tổng: tổng doanh thu, tổng đơn, AOV (Average Order Value), tỷ lệ huỷ.

**API:** `GET /v1/admin/reports/revenue?from=&to=&groupBy=`.

### 7.17. TopProductsPage (`/reports/top-products`)

**UI:** bar chart top 10 + bảng full list. Filter: từ ngày – đến ngày, theo doanh thu hoặc số lượng.

### 7.18. LowStockPage (`/reports/low-stock`)

**UI:** Bảng các variant có `stock_quantity < threshold` (default 20). Filter category, brand. Action: nhanh điều chỉnh tồn kho.

### 7.19. Settings

- **ProfilePage**: hiển thị + sửa thông tin user (gọi `/v1/me`).
- **ChangePasswordPage**: form 3 trường, gọi `/v1/auth/change-password`.
- **SystemSettingsPage** (admin): cấu hình hệ thống (tuỳ chọn mở rộng).

---

## 8. Permissions Matrix

| Resource / Action | admin | staff | customer (forbidden) |
|---|---|---|---|
| Xem dashboard | ✅ | ✅ | ❌ |
| Quản lý users (xem/khoá/đổi role) | ✅ | ❌ | ❌ |
| Quản lý categories/brands/attributes | ✅ | ❌ | ❌ |
| Quản lý products (CRUD) | ✅ | ✅ | ❌ |
| Xoá product | ✅ | ❌ | ❌ |
| Quản lý orders (xem, đổi trạng thái) | ✅ | ✅ | ❌ |
| Refund order | ✅ | ❌ | ❌ |
| Quản lý vouchers | ✅ | ❌ | ❌ |
| Duyệt review | ✅ | ✅ | ❌ |
| Quản lý shipping methods | ✅ | ❌ | ❌ |
| Xem báo cáo doanh thu | ✅ | ❌ | ❌ |
| Xem top products / low stock | ✅ | ✅ | ❌ |
| Cài đặt hệ thống | ✅ | ❌ | ❌ |

> Triển khai bằng `<PermissionGate roles={['admin']}>` ẩn UI và `RoleRoute` chặn route.

---

## 9. API Integration

### 9.1. axios client

```ts
// api/client.ts
import axios from "axios";
import { useAuthStore } from "@/stores/authStore";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080/api",
  timeout: 30000,
});

api.interceptors.request.use((cfg) => {
  const token = useAuthStore.getState().accessToken;
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401 && !err.config._retried) {
      err.config._retried = true;
      const ok = await useAuthStore.getState().refresh();
      if (ok) return api(err.config);
      useAuthStore.getState().logout();
    }
    return Promise.reject(err);
  }
);
```

### 9.2. Typing API

```ts
// types/api.ts
export interface ApiResponse<T> {
  success: boolean;
  code: number;
  message: string;
  data: T;
  errors?: ApiError[];
  timestamp: string;
}
export interface ApiError { field?: string; code: string; message: string; }
export interface PageResponse<T> {
  content: T[]; page: number; size: number;
  totalElements: number; totalPages: number;
  first: boolean; last: boolean;
}
```

### 9.3. React Query patterns

```ts
// hooks/queries/useOrders.ts
export const useOrders = (params: OrdersQuery) =>
  useQuery({
    queryKey: ["orders", params],
    queryFn: () => orderApi.adminList(params),
    keepPreviousData: true,
    staleTime: 30_000,
  });

export const useUpdateOrderStatus = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ code, status, note }: UpdateStatusInput) =>
      orderApi.updateStatus(code, status, note),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ["orders"] });
      qc.invalidateQueries({ queryKey: ["order", vars.code] });
      message.success("Cập nhật trạng thái thành công");
    },
  });
};
```

---

## 10. Validation rules (Zod)

```ts
// utils/validators.ts
export const productSchema = z.object({
  categoryId: z.number().positive(),
  brandId: z.number().positive().optional().nullable(),
  sku: z.string().min(1).max(100),
  name: z.string().min(1).max(200),
  slug: z.string().regex(/^[a-z0-9-]+$/).optional(),
  basePrice: z.number().positive(),
  salePrice: z.number().positive().optional().nullable()
    .refine((s) => s == null || true), // refine khi biết basePrice
  status: z.enum(["draft", "active", "inactive", "out_of_stock"]),
});

export const voucherSchema = z.object({
  code: z.string().min(1).max(50).regex(/^[A-Z0-9_-]+$/),
  discountType: z.enum(["percentage", "fixed_amount"]),
  discountValue: z.number().positive(),
  startDate: z.date(),
  endDate: z.date(),
}).refine((d) => d.endDate > d.startDate, {
  message: "Ngày kết thúc phải sau ngày bắt đầu",
  path: ["endDate"],
});
```

---

## 11. Error handling & Notification

| Tình huống | Hành động UI |
|---|---|
| 400 / 422 (validation) | hiện error dưới field tương ứng, scroll tới field đầu tiên |
| 401 unauthorized | tự refresh token. Fail → logout + redirect login |
| 403 forbidden | redirect `/forbidden` hoặc toast "Không có quyền" |
| 404 | toast cụ thể, hoặc empty state |
| 5xx / network | banner đỏ "Có lỗi xảy ra, thử lại" + retry button |
| Mutation success | toast xanh + invalidate query |

---

## 12. i18n & Theme

- Mặc định **vi-VN**, có thể switch sang **en-US**.
- AntD `ConfigProvider` với `viVN` locale.
- Theme:
  - Light (mặc định) / Dark (toggle ở header).
  - Lưu trong `uiStore` + localStorage.

---

## 13. Performance

- **Code splitting** theo route bằng `React.lazy + Suspense`.
- **Virtualize** bảng có > 500 dòng (dùng `rc-virtual-list` hoặc TanStack Virtual).
- Debounce search 300ms.
- Memo các cell renderer trong Table.
- `staleTime` mặc định 30s với React Query.
- Compress images trước khi upload (tuỳ chọn: dùng `browser-image-compression`).

---

## 14. Accessibility

- WCAG 2.1 AA: contrast ≥ 4.5:1.
- Mọi nút có aria-label.
- Form: liên kết `<label htmlFor>` với input.
- Keyboard: Tab order hợp lý, Esc đóng modal/drawer.
- Modal phải `trap focus`.

---

## 15. Build & Deployment

### 15.1. Scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview --port 5173",
    "lint": "eslint . --ext .ts,.tsx",
    "format": "prettier --write \"src/**/*.{ts,tsx,css,json}\""
  }
}
```

### 15.2. Biến môi trường (`.env`)

```
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=E-commerce Admin
VITE_DEFAULT_LOCALE=vi
```

### 15.3. Dockerfile (production – nginx serve static)

```dockerfile
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

`nginx.conf` cần config `try_files $uri /index.html;` cho SPA.

---

## 16. Lộ trình phát triển (8 sprint, ~2 tháng)

| Sprint | Phạm vi |
|---|---|
| 1 | Init Vite+TS+AntD, layout (Sidebar/Header), router guards, axios client, Login, authStore + refresh flow |
| 2 | DashboardPage (KPI + 1 chart), Categories, Brands |
| 3 | Products: list + form (general tab + images), upload integration |
| 4 | Products: variants tab + attributes management |
| 5 | Orders: list + detail + status flow + cancel |
| 6 | Vouchers, Shipments, Payments, Shipping methods |
| 7 | Reviews moderation, Reports (revenue, top-products, low-stock), Users management |
| 8 | i18n, dark mode, performance tuning, error pages, polish, deploy |

---

## 17. Wireframe ASCII tổng quan

### Dashboard
```
┌─────────┬──────────────────────────────────────────────────────────┐
│ LOGO    │ Dashboard                              🔔 admin@shop ▾    │
│         ├──────────────────────────────────────────────────────────┤
│ MENU    │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                      │
│ • Dash  │  │ KPI1 │ │ KPI2 │ │ KPI3 │ │ KPI4 │                      │
│ • Catlg │  └──────┘ └──────┘ └──────┘ └──────┘                      │
│ • Order │  ┌────────────────────────────────────────────────┐       │
│ • ...   │  │ Revenue chart (line)                            │       │
│         │  └────────────────────────────────────────────────┘       │
└─────────┴──────────────────────────────────────────────────────────┘
```

### Products list
```
┌─────────┬──────────────────────────────────────────────────────────┐
│         │ Sản phẩm                                  [+ Thêm sản phẩm]│
│         ├──────────────────────────────────────────────────────────┤
│ MENU    │  Search [_________]  Category [▾] Brand [▾] Status [▾]   │
│         │  ┌──────────────────────────────────────────────────┐    │
│         │  │ ☐ │Img│ Tên          │Cat│Giá  │Tồn│Status│Action │   │
│         │  ├──────────────────────────────────────────────────┤    │
│         │  │ ☐ │📱 │ iPhone 15 Pro │... │28M  │ 23│Active│✎ ✕  │   │
│         │  │ ☐ │👕 │ Áo thun Uniqlo│... │199K │230│Active│✎ ✕  │   │
│         │  └──────────────────────────────────────────────────┘    │
│         │                                  ‹ 1 2 3 ... 10 ›         │
└─────────┴──────────────────────────────────────────────────────────┘
```

---

## 18. Checklist khởi tạo project

```bash
# 1. Tạo project
npm create vite@latest ecommerce-admin -- --template react-ts
cd ecommerce-admin

# 2. Cài deps
npm i antd @ant-design/icons dayjs
npm i @tanstack/react-query axios zustand
npm i react-router-dom
npm i react-hook-form zod @hookform/resolvers
npm i recharts
npm i react-i18next i18next

# 3. Dev deps
npm i -D @types/node prettier eslint-config-prettier

# 4. Cấu hình aliases trong vite.config.ts + tsconfig (path '@/...')

# 5. Setup theme + ConfigProvider locale viVN

# 6. Tạo cấu trúc thư mục như mục 3
```

---

## 19. Tích hợp với backend đã có

> Xem `BackEnd/README.md` mục 7 để biết chi tiết URL từng endpoint. Một số mapping quan trọng:

| Trang Dashboard | Endpoint chính |
|---|---|
| Login | `POST /v1/auth/login` |
| Refresh | `POST /v1/auth/refresh` |
| Profile | `GET / PUT /v1/me` |
| Đổi mật khẩu | `POST /v1/auth/change-password` |
| Categories CRUD | `GET /v1/categories`, `POST/PUT/DELETE /v1/admin/categories[/{id}]` |
| Brands CRUD | `GET /v1/brands`, `POST/PUT/DELETE /v1/admin/brands[/{id}]` |
| Products list | `GET /v1/products?categoryId&brandId&q&minPrice&maxPrice&page&size&sort` |
| Product CRUD | `POST/PUT/DELETE /v1/admin/products[/{id}]` |
| Variants | `POST /v1/admin/products/{id}/variants`, `DELETE .../variants/{vid}` |
| Upload ảnh | `POST /v1/uploads/image` (multipart) |
| Orders list | `GET /v1/admin/orders?status&page&size&sort` |
| Order detail | `GET /v1/admin/orders/{code}` |
| Đổi trạng thái | `PATCH /v1/admin/orders/{code}/status` |
| Huỷ đơn | `POST /v1/admin/orders/{code}/cancel?note=...` |
| Vouchers | `GET / POST / PUT / DELETE /v1/admin/vouchers[/{id}]` |
| Shipping methods | `GET /v1/shipping/methods` |
| Reviews moderate | `PATCH /v1/admin/reviews/{id}/status?status=approved` |
| Reports | `GET /v1/admin/reports/overview`, `revenue`, `top-products`, `low-stock` |

> **Lưu ý:** một số endpoint admin (như `GET /v1/admin/users`, `GET /v1/admin/reviews`, `GET /v1/admin/reports/...`) đã có trong **REQUIREMENTS** nhưng **chưa implement đầy đủ trong backend MVP** – cần bổ sung khi triển khai trang tương ứng.

---

## 20. Tài khoản test

| Email | Role | Password (sau seed V2) |
|---|---|---|
| `admin@shop.vn` | admin | `Admin@123` |
| `staff01@shop.vn` | staff | `Admin@123` |

---

> Tài liệu này là **specification chi tiết** cho team Frontend Dashboard. Mọi page mới phải:
> 1. Có route trong `router.tsx` + role guard.
> 2. Có entry trong sidebar (nếu là menu chính).
> 3. Sử dụng `<DataTable>` / `<PageHeader>` / `<StatusTag>` đã định nghĩa.
> 4. Validate bằng Zod, fetch bằng React Query, mutate có invalidate.
> 5. Có quyền hợp lệ trong **Permissions Matrix** (mục 8).
