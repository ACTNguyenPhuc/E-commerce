export type OrderStatus =
  | "pending"
  | "confirmed"
  | "processing"
  | "shipping"
  | "delivered"
  | "completed"
  | "cancelled"
  | "returned";

export type PaymentMethod = "cod" | "bank_transfer" | "credit_card" | "momo" | "vnpay" | "zalopay";
export type PaymentStatus = "pending" | "paid" | "failed" | "refunded";

export interface OrderRow {
  id: number;
  orderCode: string;
  userId: number;
  subtotal: string;
  shippingFee: string;
  discountAmount: string;
  totalAmount: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  status: OrderStatus;
  placedAt: string | null;
  createdAt: string;
}

export interface OrderItemRow {
  id: number;
  productId: number;
  variantId: number | null;
  productName: string;
  variantName: string | null;
  sku: string;
  unitPrice: string;
  quantity: number;
  subtotal: string;
}

export interface OrderHistory {
  fromStatus: string | null;
  toStatus: string;
  note: string | null;
  createdAt: string;
}

export interface OrderDetail {
  id: number;
  orderCode: string;
  recipientName: string;
  recipientPhone: string;
  shippingAddress: string;
  subtotal: string;
  shippingFee: string;
  discountAmount: string;
  totalAmount: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  status: OrderStatus;
  note: string | null;
  placedAt: string | null;
  confirmedAt: string | null;
  deliveredAt: string | null;
  cancelledAt: string | null;
  items: OrderItemRow[];
  history: OrderHistory[];
}

export interface UpdateOrderStatus {
  status: OrderStatus;
  note?: string;
}
