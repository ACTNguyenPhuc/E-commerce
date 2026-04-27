import type { CommonStatus } from "./catalog";

export interface ShippingMethod {
  id: number;
  code: string;
  name: string;
  description: string | null;
  baseFee: string;
  estimatedDaysMin: number | null;
  estimatedDaysMax: number | null;
  status: CommonStatus;
}
