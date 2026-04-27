import { useQuery } from "@tanstack/react-query";
import {
  Avatar,
  Button,
  Card,
  Input,
  Select,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
  Typography,
  theme,
} from "antd";
import { Link } from "react-router-dom";
import { useState } from "react";
import { catalogApi } from "@/api/catalog.api";
import { PageHeader } from "@/components/common/PageHeader";
import { PriceDisplay } from "@/components/common/PriceDisplay";
import type { Product, ProductStatus } from "@/types/catalog";
import { EyeOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";

const STATUS_OPTS: { value: ProductStatus | "all"; label: string }[] = [
  { value: "all", label: "Tất cả" },
  { value: "draft", label: "Nháp" },
  { value: "active", label: "Đang bán" },
  { value: "inactive", label: "Tắt" },
  { value: "out_of_stock", label: "Hết hàng" },
];

export function ProductListPage() {
  const { token } = theme.useToken();
  const [page, setPage] = useState(1);
  const [size] = useState(20);
  const [q, setQ] = useState<string | undefined>(undefined);
  const [qInput, setQInput] = useState("");
  const [status, setStatus] = useState<ProductStatus | "all">("all");

  const { data, isLoading } = useQuery({
    queryKey: ["admin", "products", { page, size, q, status }],
    queryFn: () =>
      catalogApi.adminProducts({
        page: page - 1,
        size,
        q: q || undefined,
        status: status === "all" ? undefined : status,
        sort: "id,desc",
      }),
  });

  const list = data?.content ?? [];

  return (
    <>
      <PageHeader
        title="Products"
        description="Quickly add a product and monitor your stats"
        extra={
          <Link to="/catalog/products/new">
            <Button type="primary" icon={<PlusOutlined />} style={{ borderRadius: 999 }}>
              Add a product
            </Button>
          </Link>
        }
      />

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: 12,
          marginBottom: 16,
        }}
      >
        <Card
          size="small"
          style={{ borderRadius: 14 }}
          bodyStyle={{ padding: 14 }}
        >
          <Statistic title="Product Sold (pcs)" value={0} valueStyle={{ fontWeight: 800 }} />
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            Last 7 days
          </Typography.Text>
        </Card>
        <Card size="small" style={{ borderRadius: 14 }} bodyStyle={{ padding: 14 }}>
          <Statistic title="On Packaging (pcs)" value={0} valueStyle={{ fontWeight: 800 }} />
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            Last 7 days
          </Typography.Text>
        </Card>
        <Card size="small" style={{ borderRadius: 14 }} bodyStyle={{ padding: 14 }}>
          <Statistic title="Average Order (pcs)" value={0} valueStyle={{ fontWeight: 800 }} />
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            Last 7 days
          </Typography.Text>
        </Card>
      </div>

      <Card
        style={{
          borderRadius: 14,
          background: token.colorBgContainer,
        }}
        bodyStyle={{ padding: 14 }}
      >
        <Space style={{ width: "100%", justifyContent: "space-between" }} wrap>
          <Input
            allowClear
            prefix={<SearchOutlined />}
            placeholder="Search product or category"
            value={qInput}
            onChange={(e) => setQInput(e.target.value)}
            onPressEnter={() => {
              setQ(qInput || undefined);
              setPage(1);
            }}
            style={{ width: 360, borderRadius: 999 }}
          />

          <Space wrap>
            <Select
              value={status}
              style={{ width: 190 }}
              options={STATUS_OPTS}
              onChange={(v) => {
                setStatus(v);
                setPage(1);
              }}
            />
          </Space>
        </Space>

        <Table<Product>
          style={{ marginTop: 12 }}
          loading={isLoading}
          rowKey="id"
          dataSource={list}
          rowSelection={{}}
          tableLayout="fixed"
          columns={[
            {
              title: "Product",
              key: "product",
              width: 280,
              render: (_, r) => (
                <Space>
                  <Avatar
                    shape="circle"
                    size={36}
                    src={r.thumbnailUrl ?? undefined}
                    style={{ background: token.colorFillSecondary }}
                  >
                    {(r.name?.[0] ?? "P").toUpperCase()}
                  </Avatar>
                  <div style={{ lineHeight: 1.1 }}>
                    <Typography.Text strong>{r.name}</Typography.Text>
                    <div style={{ fontSize: 12, opacity: 0.7 }}>{r.sku}</div>
                  </div>
                </Space>
              ),
            },
            {
              title: "Description",
              dataIndex: "shortDescription",
              width: 320,
              render: (v: string | null) => (
                <Typography.Text type="secondary" ellipsis={{ tooltip: v ?? "" }}>
                  {v || "—"}
                </Typography.Text>
              ),
            },
            {
              title: "Price Per Item",
              key: "price",
              width: 160,
              render: (_, r) => (
                <Space direction="vertical" size={0}>
                  <Typography.Text>
                    <PriceDisplay value={r.basePrice} />
                  </Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {r.salePrice ? <PriceDisplay value={r.salePrice} /> : "—"}
                  </Typography.Text>
                </Space>
              ),
            },
            {
              title: "Status",
              dataIndex: "status",
              width: 140,
              render: (s: ProductStatus) => {
                const color =
                  s === "active" ? "green" : s === "draft" ? "default" : s === "out_of_stock" ? "red" : "orange";
                return (
                  <Tag color={color} style={{ borderRadius: 999, padding: "2px 10px", fontWeight: 600 }}>
                    {s}
                  </Tag>
                );
              },
            },
            {
              title: "Action",
              key: "a",
              width: 140,
              render: (_, r) => (
                <Space>
                  <Tooltip title="See details">
                    <Link to={`/catalog/products/${r.slug}`}>
                      <Button
                        type="primary"
                        icon={<EyeOutlined />}
                        style={{ borderRadius: 999 }}
                      />
                    </Link>
                  </Tooltip>
                </Space>
              ),
            },
          ]}
          pagination={{
            current: page,
            pageSize: size,
            total: data?.totalElements,
            onChange: (p) => setPage(p),
            showSizeChanger: false,
          }}
        />
      </Card>
    </>
  );
}
