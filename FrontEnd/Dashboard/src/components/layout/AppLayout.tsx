import { Layout, theme } from "antd";
import { Outlet } from "react-router-dom";
import { HeaderBar } from "./HeaderBar";
import { SidebarMenu } from "./SidebarMenu";

const { Content } = Layout;

export function AppLayout() {
  const { token } = theme.useToken();
  return (
    <Layout style={{ minHeight: "100vh", background: token.colorBgLayout }}>
      <SidebarMenu />
      <Layout style={{ background: token.colorBgLayout }}>
        <HeaderBar />
        <Content
          style={{
            margin: 24,
            padding: 24,
            minHeight: 360,
            background: token.colorBgContainer,
            borderRadius: token.borderRadiusLG,
            boxShadow: token.boxShadowSecondary,
            border: `1px solid ${token.colorBorderSecondary}`,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
