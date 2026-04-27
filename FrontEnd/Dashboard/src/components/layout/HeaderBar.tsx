import { Layout, Dropdown, Space, Avatar, theme, Button, Typography, Menu, Badge, Tooltip } from "antd";
import {
  UserOutlined,
  LogoutOutlined,
  LockOutlined,
  MenuOutlined,
  MenuFoldOutlined,
  BulbOutlined,
  BellOutlined,
  AppstoreOutlined,
  ShoppingCartOutlined,
  TeamOutlined,
  BarChartOutlined,
} from "@ant-design/icons";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";
import { useUiStore } from "@/stores/uiStore";

const { Header } = Layout;

export function HeaderBar() {
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar, themeMode, toggleTheme } = useUiStore();
  const isDark = themeMode === "dark";

  const selectedKey = (() => {
    const p = location.pathname;
    if (p.startsWith("/catalog/products")) return "products";
    if (p.startsWith("/orders")) return "transactions";
    if (p.startsWith("/users")) return "resellers";
    if (p.startsWith("/reports")) return "reports";
    if (p.startsWith("/dashboard")) return "overview";
    return "";
  })();

  const navItems = [
    { key: "overview", label: "Overview", icon: <AppstoreOutlined />, onClick: () => navigate("/dashboard") },
    { key: "products", label: "Products", icon: <ShoppingCartOutlined />, onClick: () => navigate("/catalog/products") },
    { key: "transactions", label: "Transactions", icon: <ShoppingCartOutlined />, onClick: () => navigate("/orders") },
    { key: "resellers", label: "Resellers", icon: <TeamOutlined />, onClick: () => navigate("/users") },
    { key: "reports", label: "Reports", icon: <BarChartOutlined />, onClick: () => navigate("/reports") },
  ];

  return (
    <Header
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        height: 64,
        padding: "0 20px",
        background: isDark
          ? "linear-gradient(180deg, rgba(28,41,62,1) 0%, rgba(24,36,54,1) 100%)"
          : token.colorBgContainer,
        borderBottom: `1px solid ${token.colorBorderSecondary}`,
      }}
    >
      <Space size={12} style={{ minWidth: 220 }}>
        <Button
          type="text"
          icon={sidebarCollapsed ? <MenuOutlined /> : <MenuFoldOutlined />}
          onClick={toggleSidebar}
        />
       
      </Space>

      <div style={{ flex: 1, display: "flex", justifyContent: "center" }}>
        <Menu
          mode="horizontal"
          selectedKeys={selectedKey ? [selectedKey] : []}
          items={navItems}
          className="topNavMenu"
          style={{
            minWidth: 520,
            borderBottom: "none",
            background: "transparent",
            fontWeight: 600,
          }}
        />
      </div>

      <Space size={16}>
        <Button
          type="text"
          icon={<BulbOutlined />}
          onClick={toggleTheme}
          title={themeMode === "dark" ? "Chuyển Light mode" : "Chuyển Dark mode"}
          style={{
            color: isDark ? token.colorText : token.colorTextSecondary,
            fontWeight: 600,
          }}
        >
          {themeMode === "dark" ? "Dark" : "Light"}
        </Button>

        <Tooltip title="Notifications">
          <Button
            type="text"
            style={{ color: isDark ? token.colorText : token.colorTextSecondary }}
            icon={
              <Badge dot offset={[-2, 2]}>
                <BellOutlined />
              </Badge>
            }
          />
        </Tooltip>
        <Dropdown
          menu={{
            items: [
              { key: "profile", label: "Hồ sơ", icon: <UserOutlined />, onClick: () => navigate("/settings/profile") },
              { key: "pass", label: "Đổi mật khẩu", icon: <LockOutlined />, onClick: () => navigate("/settings/change-password") },
              { type: "divider" },
              { key: "out", label: "Đăng xuất", icon: <LogoutOutlined />, danger: true, onClick: () => logout() },
            ],
          }}
        >
          <Space
            style={{
              cursor: "pointer",
              padding: "0px 10px",
              borderRadius: 12
            }}
            size={10}
          >
            <Avatar
              size="small"
              style={{ backgroundColor: token.colorPrimary }}
              src={user?.avatarUrl ?? undefined}
              icon={<UserOutlined />}
            />
            <div style={{ lineHeight: 1.2 }}>
              <Typography.Text style={{ color: token.colorText, fontWeight: 600 }}>
                Hi, {user?.fullName ?? "—"}
              </Typography.Text>
              <Typography.Text
                type="secondary"
                style={{ display: "block", fontSize: 12, color: token.colorTextSecondary }}
              >
                {user?.email ?? "—"} · {user?.role ?? "—"}
              </Typography.Text>
            </div>
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
}
