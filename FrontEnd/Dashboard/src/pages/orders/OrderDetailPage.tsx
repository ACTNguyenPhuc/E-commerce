import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Card, Descriptions, Input, Modal, Select, Space, Table, Tag, Timeline, Typography } from "antd";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { orderApi } from "@/api/order.api";
import { PageHeader } from "@/components/common/PageHeader";
import { OrderStatusTag, PaymentStatusTag } from "@/components/common/StatusTag";
import { PriceDisplay } from "@/components/common/PriceDisplay";
import type { OrderItemRow, OrderStatus } from "@/types/order";
import { formatDateTime, formatVnd } from "@/utils/format";
import { allowedNextStatuses } from "@/utils/orderTransitions";
import { ArrowLeftOutlined } from "@ant-design/icons";

export function OrderDetailPage() {
  const { code = "" } = useParams<{ code: string }>();
  const { message } = App.useApp();
  const qc = useQueryClient();
  const [next, setNext] = useState<OrderStatus | undefined>(undefined);
  const [note, setNote] = useState("");
  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelNote, setCancelNote] = useState("");

  const { data: o, isLoading } = useQuery({
    queryKey: ["admin", "order", code],
    queryFn: () => orderApi.adminDetail(code),
    enabled: Boolean(code),
  });

  const updateMut = useMutation({
    mutationFn: () =>
      orderApi.updateStatus(code, { status: next!, note: note || undefined }),
    onSuccess: async () => {
      message.success("Đã cập nhật trạng thái");
      setNote("");
      setNext(undefined);
      await qc.invalidateQueries({ queryKey: ["admin", "order", code] });
      await qc.invalidateQueries({ queryKey: ["admin", "orders"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Không cập nhật được"),
  });

  const cancelMut = useMutation({
    mutationFn: (n?: string) => orderApi.cancel(code, n),
    onSuccess: async () => {
      message.success("Đã huỷ đơn");
      setCancelOpen(false);
      setCancelNote("");
      await qc.invalidateQueries({ queryKey: ["admin", "order", code] });
      await qc.invalidateQueries({ queryKey: ["admin", "orders"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Không huỷ được"),
  });

  if (isLoading || !o) {
    return <p>{isLoading ? "Đang tải…" : "Không tìm thấy đơn."}</p>;
  }

  const allowed = allowedNextStatuses(o.status);
  const canAdminCancel = o.status === "pending" || o.status === "confirmed";

  return (
    <>
      <PageHeader
        title={`Đơn ${o.orderCode}`}
        description="Cập nhật trạng thái theo workflow; lịch sử lấy từ order history."
        extra={
          <Link to="/orders">
            <Button icon={<ArrowLeftOutlined />}>Danh sách</Button>
          </Link>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Descriptions column={1} size="small" bordered>
          <Descriptions.Item label="Người nhận">{o.recipientName}</Descriptions.Item>
          <Descriptions.Item label="Điện thoại">{o.recipientPhone}</Descriptions.Item>
          <Descriptions.Item label="Địa chỉ">{o.shippingAddress}</Descriptions.Item>
          <Descriptions.Item label="Ghi chú">{o.note ?? "—"}</Descriptions.Item>
          <Descriptions.Item label="Tổng phụ">{formatVnd(o.subtotal)}</Descriptions.Item>
          <Descriptions.Item label="Phí ship">{formatVnd(o.shippingFee)}</Descriptions.Item>
          <Descriptions.Item label="Giảm giá">{formatVnd(o.discountAmount)}</Descriptions.Item>
          <Descriptions.Item label="Thành tiền">
            <Typography.Text strong>{formatVnd(o.totalAmount)}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="Thanh toán">
            {o.paymentMethod} · <PaymentStatusTag status={o.paymentStatus} />
          </Descriptions.Item>
          <Descriptions.Item label="Trạng thái đơn">
            <OrderStatusTag status={o.status} />
          </Descriptions.Item>
          <Descriptions.Item label="Đặt lúc">{formatDateTime(o.placedAt)}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="Chuyển trạng thái" style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <div>
            <span style={{ display: "inline-block", width: 160 }}>Trạng thái mới</span>
            <Select
              allowClear
              style={{ minWidth: 200 }}
              placeholder="Chọn bước hợp lệ kế tiếp"
              value={next}
              onChange={setNext}
              options={allowed.map((s) => ({ value: s, label: s }))}
            />
          </div>
          <div>
            <span style={{ display: "inline-block", width: 160 }}>Ghi chú (tuỳ chọn)</span>
            <Input.TextArea value={note} onChange={(e) => setNote(e.target.value)} style={{ maxWidth: 400 }} rows={2} />
          </div>
          <Button
            type="primary"
            disabled={!next}
            loading={updateMut.isPending}
            onClick={() => (next ? updateMut.mutate() : null)}
          >
            Áp dụng
          </Button>
        </Space>
        {canAdminCancel && (
          <div style={{ marginTop: 16 }}>
            <Button danger onClick={() => setCancelOpen(true)}>
              Huỷ đơn
            </Button>
            <Modal
              title="Huỷ đơn hàng"
              open={cancelOpen}
              onCancel={() => setCancelOpen(false)}
              onOk={() => cancelMut.mutate(cancelNote || undefined)}
              confirmLoading={cancelMut.isPending}
            >
              <Input.TextArea
                value={cancelNote}
                onChange={(e) => setCancelNote(e.target.value)}
                rows={2}
                placeholder="Lý do huỷ (tuỳ chọn)"
              />
            </Modal>
          </div>
        )}
      </Card>

      <Card title="Dòng hàng" style={{ marginTop: 16 }}>
        <Table<OrderItemRow>
        dataSource={o.items}
        rowKey="id"
        size="small"
        pagination={false}
        columns={[
          { title: "Sản phẩm", dataIndex: "productName" },
          { title: "Variant", dataIndex: "variantName", render: (t) => t ?? "—" },
          { title: "SKU", dataIndex: "sku" },
          { title: "SL", dataIndex: "quantity", width: 60 },
          {
            title: "Đơn giá",
            dataIndex: "unitPrice",
            render: (p: string) => <PriceDisplay value={p} />,
          },
          {
            title: "Cộng",
            dataIndex: "subtotal",
            render: (p: string) => <PriceDisplay value={p} />,
          },
        ]}
        />
      </Card>

      {o.history?.length > 0 && (
        <Card title="Lịch sử" style={{ marginTop: 16 }}>
          <Timeline
            items={o.history.map((h) => ({
              color: "blue",
              children: (
                <div>
                  <Tag>
                    {h.fromStatus ?? "—"} → {h.toStatus}
                  </Tag>
                  <div style={{ color: "#888", fontSize: 12 }}>{formatDateTime(h.createdAt)}</div>
                  {h.note && <div>{h.note}</div>}
                </div>
              ),
            }))}
          />
        </Card>
      )}
    </>
  );
}
