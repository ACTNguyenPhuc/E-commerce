import type { OrderStatus, PaymentStatus } from "@/types/order";
import type { TagProps } from "antd";

const orderMap: Record<OrderStatus, { label: string; color: TagProps["color"] }> = {
  pending: { label: "Chờ xác nhận", color: "gold" },
  confirmed: { label: "Đã xác nhận", color: "blue" },
  processing: { label: "Đang chuẩn bị", color: "cyan" },
  shipping: { label: "Đang giao", color: "geekblue" },
  delivered: { label: "Đã giao", color: "lime" },
  completed: { label: "Hoàn tất", color: "green" },
  cancelled: { label: "Đã huỷ", color: "red" },
  returned: { label: "Trả hàng", color: "volcano" },
};

export function orderStatusMeta(
  s: OrderStatus
): { label: string; color: TagProps["color"] } {
  return orderMap[s] || { label: s, color: "default" };
}

const payMap: Record<PaymentStatus, { label: string; color: TagProps["color"] }> = {
  pending: { label: "Chờ thanh toán", color: "gold" },
  paid: { label: "Đã thanh toán", color: "green" },
  failed: { label: "Thất bại", color: "red" },
  refunded: { label: "Đã hoàn tiền", color: "purple" },
};

export function paymentStatusMeta(
  s: PaymentStatus
): { label: string; color: TagProps["color"] } {
  return payMap[s] || { label: s, color: "default" };
}
