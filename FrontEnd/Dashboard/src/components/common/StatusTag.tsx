import { Tag } from "antd";
import type { OrderStatus, PaymentStatus } from "@/types/order";
import { orderStatusMeta, paymentStatusMeta } from "@/utils/status";

export function OrderStatusTag({ status }: { status: OrderStatus }) {
  const m = orderStatusMeta(status);
  return <Tag color={m.color}>{m.label}</Tag>;
}

export function PaymentStatusTag({ status }: { status: PaymentStatus }) {
  const m = paymentStatusMeta(status);
  return <Tag color={m.color}>{m.label}</Tag>;
}
