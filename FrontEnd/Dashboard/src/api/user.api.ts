import { api, extractData } from "@/api/client";
import type { ApiResponse } from "@/types/api";
import type { MeUpdate, User } from "@/types/user";
import { buildMultipartFormData } from "@/utils/multipart";

export const userApi = {
  me: () => api.get<ApiResponse<User>>("/v1/me").then(extractData),
  updateMe: (body: MeUpdate, file?: File | null) =>
    api
      .put<ApiResponse<User>>("/v1/me", buildMultipartFormData(body, file))
      .then(extractData),
};
