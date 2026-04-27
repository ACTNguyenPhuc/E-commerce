import type { OrderStatus } from "@/types/order";

const NEXT: Record<OrderStatus, OrderStatus[]> = {
  pending: ["confirmed", "cancelled"],
  confirmed: ["processing", "cancelled"],
  processing: ["shipping", "cancelled"],
  shipping: ["delivered", "returned"],
  delivered: ["completed", "returned"],
  completed: [],
  cancelled: [],
  returned: [],
};

export function allowedNextStatuses(from: OrderStatus): OrderStatus[] {
  return NEXT[from] ?? [];
}
