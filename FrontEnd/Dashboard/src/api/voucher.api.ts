import { api, extractData } from "@/api/client";
import type { ApiResponse } from "@/types/api";
import type { Voucher, VoucherRequest } from "@/types/voucher";

export const voucherApi = {
  list: () => api.get<ApiResponse<Voucher[]>>("/v1/admin/vouchers").then(extractData),
  create: (body: VoucherRequest) =>
    api.post<ApiResponse<Voucher>>("/v1/admin/vouchers", body).then(extractData),
  update: (id: number, body: VoucherRequest) =>
    api.put<ApiResponse<Voucher>>(`/v1/admin/vouchers/${id}`, body).then(extractData),
  remove: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/vouchers/${id}`).then(extractData),
};
