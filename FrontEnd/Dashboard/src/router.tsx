import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppLayout } from "@/components/layout/AppLayout";
import { RequireAuth } from "@/components/auth/RequireAuth";
import { LoginPage } from "@/pages/LoginPage";
import { DashboardPage } from "@/pages/DashboardPage";
import { CategoriesPage } from "@/pages/catalog/CategoriesPage";
import { BrandsPage } from "@/pages/catalog/BrandsPage";
import { ProductListPage } from "@/pages/catalog/ProductListPage";
import { ProductFormPage } from "@/pages/catalog/ProductFormPage";
import { OrderListPage } from "@/pages/orders/OrderListPage";
import { OrderDetailPage } from "@/pages/orders/OrderDetailPage";
import { VouchersPage } from "@/pages/VouchersPage";
import { ShippingMethodsPage } from "@/pages/ShippingMethodsPage";
import { ProfilePage } from "@/pages/settings/ProfilePage";
import { ChangePasswordPage } from "@/pages/settings/ChangePasswordPage";
import { BannersPage } from "@/pages/settings/BannersPage";
import { PlaceholderPage } from "@/pages/PlaceholderPage";
import { ForbiddenPage } from "@/pages/ForbiddenPage";
import { NotFoundPage } from "@/pages/NotFoundPage";

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/forbidden", element: <ForbiddenPage /> },
  {
    path: "/",
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: "dashboard", element: <DashboardPage /> },
      { path: "catalog/categories", element: <CategoriesPage /> },
      { path: "catalog/brands", element: <BrandsPage /> },
      { path: "catalog/products", element: <ProductListPage /> },
      { path: "catalog/products/:slug", element: <ProductFormPage /> },
      { path: "orders", element: <OrderListPage /> },
      { path: "orders/:code", element: <OrderDetailPage /> },
      { path: "vouchers", element: <VouchersPage /> },
      { path: "shipping-methods", element: <ShippingMethodsPage /> },
      {
        path: "users",
        element: (
          <PlaceholderPage
            title="Người dùng"
            description="Màn hình quản lý tài khoản khách hàng sẽ bổ sung ở phiên bản sau. Hiện backend đã có model User; cần thêm API admin list/filter."
          />
        ),
      },
      {
        path: "reports",
        element: (
          <PlaceholderPage
            title="Báo cáo"
            description="Biểu đồ doanh thu, top sản phẩm… sẽ khi có endpoint thống kê (aggregates) từ backend."
          />
        ),
      },
      { path: "settings/profile", element: <ProfilePage /> },
      { path: "settings/change-password", element: <ChangePasswordPage /> },
      { path: "settings/banners", element: <BannersPage /> },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
