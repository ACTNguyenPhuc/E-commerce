import { useQuery } from "@tanstack/react-query";
import { Table, Tag } from "antd";
import { shippingApi } from "@/api/shipping.api";
import { PageHeader } from "@/components/common/PageHeader";
import type { ShippingMethod } from "@/types/shipping";
import { formatVnd } from "@/utils/format";

export function ShippingMethodsPage() {
  const { data: list = [], isLoading } = useQuery({
    queryKey: ["shipping", "methods"],
    queryFn: () => shippingApi.methods(),
  });
  return (
    <>
      <PageHeader
        title="Hình thức vận chuyển"
        description="Chỉ đọc; cấu hình phí & SLA nằm ở backend / seed. Shipment từng đơn xem ở chi tiết đơn hàng."
      />
      <Table<ShippingMethod>
        rowKey="id"
        loading={isLoading}
        dataSource={list}
        columns={[
          { title: "Mã", dataIndex: "code", width: 100 },
          { title: "Tên", dataIndex: "name" },
          { title: "Mô tả", dataIndex: "description", render: (t) => t ?? "—" },
          {
            title: "Phí cơ bản",
            dataIndex: "baseFee",
            width: 130,
            render: (b: string) => formatVnd(b),
          },
          {
            title: "Dự kiến (ngày)",
            key: "eta",
            width: 140,
            render: (_, r) =>
              r.estimatedDaysMin != null && r.estimatedDaysMax != null
                ? `${r.estimatedDaysMin}–${r.estimatedDaysMax}`
                : "—",
          },
          {
            title: "Trạng thái",
            dataIndex: "status",
            width: 100,
            render: (s: string) => <Tag color={s === "active" ? "green" : "default"}>{s}</Tag>,
          },
        ]}
        pagination={false}
      />
    </>
  );
}
