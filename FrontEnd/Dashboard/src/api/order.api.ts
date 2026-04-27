import { api, extractData } from "@/api/client";
import type { ApiResponse, PageResponse } from "@/types/api";
import type { OrderDetail, OrderRow, UpdateOrderStatus } from "@/types/order";

export const orderApi = {
  adminList: (params: { status?: string; page?: number; size?: number; sort?: string }) =>
    api
      .get<ApiResponse<PageResponse<OrderRow>>>("/v1/admin/orders", { params })
      .then(extractData),
  adminDetail: (code: string) =>
    api.get<ApiResponse<OrderDetail>>(`/v1/admin/orders/${code}`).then(extractData),
  updateStatus: (code: string, body: UpdateOrderStatus) =>
    api
      .patch<ApiResponse<OrderDetail>>(`/v1/admin/orders/${code}/status`, body)
      .then(extractData),
  cancel: (code: string, note?: string) =>
    api
      .post<ApiResponse<OrderDetail>>(
        `/v1/admin/orders/${code}/cancel`,
        {},
        { params: { ...(note ? { note } : {}) } }
      )
      .then(extractData),
};
