import { App, Button, Card, Form, Input, theme, Typography } from "antd";
import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";
import { useUiStore } from "@/stores/uiStore";

export function LoginPage() {
  const { message } = App.useApp();
  const { token } = theme.useToken();
  const themeMode = useUiStore((s) => s.themeMode);
  const toggleTheme = useUiStore((s) => s.toggleTheme);
  const navigate = useNavigate();
  const loc = useLocation();
  const accessToken = useAuthStore((s) => s.accessToken);
  const login = useAuthStore((s) => s.login);

  useEffect(() => {
    if (accessToken) {
      void navigate("/dashboard", { replace: true });
    }
  }, [accessToken, navigate]);

  const pageBg =
    themeMode === "dark"
      ? "linear-gradient(165deg, #111c2d 0%, #1c293e 40%, #243652 100%)"
      : "linear-gradient(160deg, #eef2ff 0%, #f4f7fb 45%, #ffffff 100%)";

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: pageBg,
        padding: 16,
        position: "relative",
      }}
    >
      <Button
        type="text"
        style={{ position: "absolute", top: 16, right: 16 }}
        onClick={() => toggleTheme()}
      >
        {themeMode === "dark" ? "Giao diện sáng" : "Giao diện tối"}
      </Button>
      <Card
        style={{
          width: 420,
          boxShadow: token.boxShadowSecondary,
          border: `1px solid ${token.colorBorderSecondary}`,
          borderRadius: 12,
        }}
      >
        <Typography.Title
          level={3}
          style={{ textAlign: "center", marginTop: 0, color: token.colorText }}
        >
          Đăng nhập quản trị
        </Typography.Title>
        <Typography.Paragraph
          type="secondary"
          style={{ textAlign: "center", marginBottom: 24, color: token.colorTextSecondary }}
        >
          E-commerce Admin Dashboard
        </Typography.Paragraph>
        <Form
          layout="vertical"
          onFinish={async (v: { email: string; password: string }) => {
            try {
              await login(v.email, v.password);
              const from = (loc.state as { from?: string } | null)?.from ?? "/dashboard";
              void navigate(from, { replace: true });
            } catch (e) {
              message.error(e instanceof Error ? e.message : "Đăng nhập thất bại");
            }
          }}
        >
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Nhập email" },
              { type: "email", message: "Email không hợp lệ" },
            ]}
          >
            <Input autoComplete="email" placeholder="admin@shop.vn" size="large" />
          </Form.Item>
          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[{ required: true, message: "Nhập mật khẩu" }]}
          >
            <Input.Password autoComplete="current-password" size="large" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>
        <Typography.Text type="secondary" style={{ display: "block", textAlign: "center" }}>
          Bảng điều khiển nội bộ
        </Typography.Text>
      </Card>
    </div>
  );
}
