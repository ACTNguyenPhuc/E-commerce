import { useQuery } from "@tanstack/react-query";
import { Select, Space, Table } from "antd";
import { Link } from "react-router-dom";
import { useState } from "react";
import { orderApi } from "@/api/order.api";
import { PageHeader } from "@/components/common/PageHeader";
import { OrderStatusTag } from "@/components/common/StatusTag";
import { PaymentStatusTag } from "@/components/common/StatusTag";
import { formatDateTime, formatVnd } from "@/utils/format";
import type { OrderRow, OrderStatus } from "@/types/order";

const STATUS_ALL: (OrderStatus | "all")[] = [
  "all",
  "pending",
  "confirmed",
  "processing",
  "shipping",
  "delivered",
  "completed",
  "cancelled",
  "returned",
];

export function OrderListPage() {
  const [page, setPage] = useState(1);
  const [size] = useState(20);
  const [status, setStatus] = useState<OrderStatus | "all">("all");

  const { data, isLoading } = useQuery({
    queryKey: ["admin", "orders", { page, size, status }],
    queryFn: () =>
      orderApi.adminList({
        page: page - 1,
        size,
        status: status === "all" ? undefined : status,
        sort: "createdAt,desc",
      }),
  });

  return (
    <>
      <PageHeader
        title="Đơn hàng"
        description="Danh sách tất cả đơn (admin). Lọc theo trạng thái, xem chi tiết theo mã."
      />
      <Space style={{ marginBottom: 16 }}>
        <span>Trạng thái</span>
        <Select
          value={status}
          style={{ width: 200 }}
          onChange={(v) => {
            setStatus(v);
            setPage(1);
          }}
          options={STATUS_ALL.map((s) => ({ value: s, label: s }))}
        />
      </Space>
      <Table<OrderRow>
        loading={isLoading}
        rowKey="id"
        dataSource={data?.content ?? []}
        columns={[
          { title: "Mã", dataIndex: "orderCode", width: 140, render: (c) => <Link to={`/orders/${c}`}>{c}</Link> },
          {
            title: "Tổng",
            dataIndex: "totalAmount",
            width: 120,
            render: (a: string) => formatVnd(a),
          },
          { title: "PTTT", dataIndex: "paymentMethod", width: 100 },
          {
            title: "TT thanh toán",
            dataIndex: "paymentStatus",
            width: 130,
            render: (s) => <PaymentStatusTag status={s} />,
          },
          {
            title: "Trạng thái",
            dataIndex: "status",
            width: 120,
            render: (s: OrderStatus) => <OrderStatusTag status={s} />,
          },
          { title: "Thời gian", dataIndex: "createdAt", width: 180, render: (t) => formatDateTime(t) },
        ]}
        pagination={{
          current: page,
          pageSize: size,
          total: data?.totalElements,
          onChange: (p) => setPage(p),
        }}
      />
    </>
  );
}
