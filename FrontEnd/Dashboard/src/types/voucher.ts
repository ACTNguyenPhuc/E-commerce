export type DiscountType = "percentage" | "fixed_amount";
export type VoucherStatus = "active" | "inactive" | "expired";

export interface Voucher {
  id: number;
  code: string;
  name: string;
  description: string | null;
  discountType: DiscountType;
  discountValue: string;
  minOrderAmount: string;
  maxDiscountAmount: string | null;
  usageLimit: number | null;
  usageLimitPerUser: number | null;
  usedCount: number;
  startDate: string;
  endDate: string;
  status: VoucherStatus;
}

export interface VoucherRequest {
  code: string;
  name: string;
  description?: string;
  discountType: DiscountType;
  discountValue: string;
  minOrderAmount?: string;
  maxDiscountAmount?: string | null;
  usageLimit?: number | null;
  usageLimitPerUser?: number | null;
  startDate: string;
  endDate: string;
  status?: VoucherStatus;
}
