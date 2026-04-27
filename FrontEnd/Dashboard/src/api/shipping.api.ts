import { api, extractData } from "@/api/client";
import type { ApiResponse } from "@/types/api";
import type { ShippingMethod } from "@/types/shipping";

export const shippingApi = {
  methods: () => api.get<ApiResponse<ShippingMethod[]>>("/v1/shipping/methods").then(extractData),
};
