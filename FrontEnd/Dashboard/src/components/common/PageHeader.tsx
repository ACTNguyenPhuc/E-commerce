import { Typography, Space, theme } from "antd";

const { Title, Text } = Typography;

export function PageHeader({
  title,
  description,
  extra,
}: {
  title: string;
  description?: string;
  extra?: React.ReactNode;
}) {
  const { token } = theme.useToken();
  return (
    <div
      style={{
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
        alignItems: "flex-start",
        gap: 12,
        marginBottom: 24,
        borderBottom: `1px solid ${token.colorBorderSecondary}`,
        paddingBottom: 16,
      }}
    >
      <div>
        <Title level={3} style={{ margin: 0, color: token.colorText, fontWeight: 600 }}>
          {title}
        </Title>
        {description && (
          <Text type="secondary" style={{ display: "block", marginTop: 4 }}>
            {description}
          </Text>
        )}
      </div>
      {extra && <Space>{extra}</Space>}
    </div>
  );
}
