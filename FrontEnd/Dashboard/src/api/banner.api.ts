import { api, extractData } from "@/api/client";
import type { ApiResponse } from "@/types/api";
import type { Banner, BannerRequest } from "@/types/banner";
import { buildMultipartFormData } from "@/utils/multipart";

export const bannerApi = {
  adminList: () => api.get<ApiResponse<Banner[]>>("/v1/admin/banners").then(extractData),
  create: (body: BannerRequest, file?: File | null) =>
    api.post<ApiResponse<Banner>>("/v1/admin/banners", buildMultipartFormData(body, file)).then(extractData),
  update: (id: number, body: BannerRequest, file?: File | null) =>
    api.put<ApiResponse<Banner>>(`/v1/admin/banners/${id}`, buildMultipartFormData(body, file)).then(extractData),
  delete: (id: number) => api.delete<ApiResponse<null>>(`/v1/admin/banners/${id}`).then(extractData),
};

