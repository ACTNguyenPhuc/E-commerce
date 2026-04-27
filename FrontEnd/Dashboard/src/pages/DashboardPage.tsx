import { useQueries } from "@tanstack/react-query";
import { Card, Col, Row, Statistic, Typography } from "antd";
import { catalogApi } from "@/api/catalog.api";
import { orderApi } from "@/api/order.api";
import { PageHeader } from "@/components/common/PageHeader";
import { AppstoreOutlined, ClockCircleOutlined, ShoppingCartOutlined } from "@ant-design/icons";

export function DashboardPage() {
  const [qOrders, qPending, qProducts] = useQueries({
    queries: [
      {
        queryKey: ["kpi", "orders-total"],
        queryFn: () => orderApi.adminList({ page: 0, size: 1 }),
      },
      {
        queryKey: ["kpi", "orders-pending"],
        queryFn: () => orderApi.adminList({ page: 0, size: 1, status: "pending" }),
      },
      {
        queryKey: ["kpi", "products-total"],
        queryFn: () => catalogApi.adminProducts({ page: 0, size: 1 }),
      },
    ],
  });

  return (
    <>
      <PageHeader
        title="Tổng quan"
        description="Thống kê nhanh từ API (tổng số đơn, đơn chờ xác nhận, tổng sản phẩm trong catalog admin)."
      />
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Tổng đơn hàng"
              value={qOrders.data?.totalElements ?? "—"}
              prefix={<ShoppingCartOutlined />}
              loading={qOrders.isLoading}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Đơn chờ xác nhận"
              value={qPending.data?.totalElements ?? "—"}
              prefix={<ClockCircleOutlined />}
              loading={qPending.isLoading}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Sản phẩm (admin list)"
              value={qProducts.data?.totalElements ?? "—"}
              prefix={<AppstoreOutlined />}
              loading={qProducts.isLoading}
            />
          </Card>
        </Col>
      </Row>
      <Typography.Paragraph type="secondary" style={{ marginTop: 24 }}>
        Đăng nhập bằng tài khoản staff hoặc admin. Voucher tạo/sửa/xoá chỉ dành cho admin. Danh mục & thương hiệu:
        chỉ admin.
      </Typography.Paragraph>
    </>
  );
}
