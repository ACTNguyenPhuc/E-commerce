import { Layout, Menu, theme } from "antd";
import {
  DashboardOutlined,
  AppstoreOutlined,
  ShoppingCartOutlined,
  TagOutlined,
  CarOutlined,
  UserOutlined,
  BarChartOutlined,
  SettingOutlined,
  ShoppingOutlined,
  ShopOutlined,
  PictureOutlined
} from "@ant-design/icons";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";
import { useUiStore } from "@/stores/uiStore";
import { useEffect, useMemo, useState } from "react";

const { Sider } = Layout;

type Item = { key: string; icon: React.ReactNode; label: string; adminOnly?: boolean; children?: Item[] };

const items: Item[] = [
  { key: "/dashboard", icon: <DashboardOutlined />, label: "Tổng quan" },
  { key: "/catalog/products", icon: <ShoppingOutlined />, label: "Sản phẩm" },
  { key: "/catalog/categories", icon: <AppstoreOutlined />, label: "Danh mục", adminOnly: true },
  { key: "/catalog/brands", icon: <ShopOutlined />, label: "Thương hiệu", adminOnly: true },
  { key: "/orders", icon: <ShoppingCartOutlined />, label: "Đơn hàng" },
  { key: "/vouchers", icon: <TagOutlined />, label: "Voucher" },
  { key: "/shipping-methods", icon: <CarOutlined />, label: "Vận chuyển" },
  { key: "/users", icon: <UserOutlined />, label: "Người dùng", adminOnly: true },
  { key: "/reports", icon: <BarChartOutlined />, label: "Báo cáo", adminOnly: true },
  {
    key: "/settings",
    icon: <SettingOutlined />,
    label: "Cài đặt",
    children: [
      { key: "/settings/profile", icon: <UserOutlined />, label: "Hồ sơ" },
      { key: "/settings/change-password", icon: <SettingOutlined />, label: "Đổi mật khẩu" },
      { key: "/settings/banners", icon: <PictureOutlined />, label: "Banners", adminOnly: true },
    ],
  },
];

export function SidebarMenu() {
  const { token } = theme.useToken();
  const loc = useLocation();
  const nav = useNavigate();
  const role = useAuthStore((s) => s.user?.role);
  const { sidebarCollapsed, setSidebarCollapsed } = useUiStore();

  const filtered: Item[] = items
    .filter((i) => !i.adminOnly || role === "admin")
    .map((i) => ({
      ...i,
      children: i.children?.filter((c) => !c.adminOnly || role === "admin"),
    }));

  const flatKeys: string[] = filtered.flatMap((i) => [i.key, ...(i.children?.map((c) => c.key) ?? [])]);

  const pathForSelect = (() => {
    const p = loc.pathname;
    // Pick the longest matching key (works for nested /settings/* too)
    const hit = flatKeys.filter((k) => p.startsWith(k)).sort((a, b) => b.length - a.length);
    return hit[0] ?? "/dashboard";
  })();

  useEffect(() => {
    const w = () => {
      if (window.innerWidth < 992) setSidebarCollapsed(true);
    };
    window.addEventListener("resize", w);
    w();
    return () => window.removeEventListener("resize", w);
  }, [setSidebarCollapsed]);

  const initialOpenKeys = useMemo(() => (loc.pathname.startsWith("/settings") ? ["/settings"] : []), [loc.pathname]);
  const [openKeys, setOpenKeys] = useState<string[]>(initialOpenKeys);

  // Keep Settings opened when navigating inside /settings/*
  useEffect(() => {
    if (loc.pathname.startsWith("/settings")) {
      setOpenKeys((prev) => (prev.includes("/settings") ? prev : [...prev, "/settings"]));
    }
  }, [loc.pathname]);

  return (
    <Sider
      collapsible
      collapsed={sidebarCollapsed}
      onCollapse={setSidebarCollapsed}
      width={260}
      style={{
        background: token.colorBgContainer,
        borderRight: `1px solid ${token.colorBorderSecondary}`,
        boxShadow: "2px 0 12px rgba(42, 53, 71, 0.04)",
      }}
    >
      <div
        style={{
          height: 64,
          display: "flex",
          alignItems: "center",
          justifyContent: sidebarCollapsed ? "center" : "flex-start",
          padding: sidebarCollapsed ? 0 : "0 20px",
          fontWeight: 700,
          fontSize: 17,
          letterSpacing: -0.2,
        }}
      >
        {sidebarCollapsed ? (
          <span
            style={{
              background: "linear-gradient(90deg, #a855f7, #5d87ff)",
              WebkitBackgroundClip: "text",
              backgroundClip: "text"
              
            }}
          >
            E
          </span>
        ) : (
          <span
            style={{
              background: "linear-gradient(90deg, #a855f7, #5d87ff)",
              WebkitBackgroundClip: "text",
              backgroundClip: "text"
            }}
          >
            E-commerce
          </span>
        )}
      </div>
      <Menu
        mode="inline"
        selectedKeys={[pathForSelect]}
        openKeys={sidebarCollapsed ? undefined : openKeys}
        onOpenChange={(keys) => setOpenKeys(keys as string[])}
        style={{ border: "none", padding: "0 8px 16px" }}
        onClick={({ key }) => {
          // Don't navigate on parent submenu click; allow expand/collapse.
          if (key === "/settings") return;
          void nav(key);
        }}
        items={filtered.map((i) => ({
          key: i.key,
          icon: i.icon,
          label: i.label,
          children: i.children?.map((c) => ({ key: c.key, icon: c.icon, label: c.label })),
        }))}
      />
    </Sider>
  );
}
