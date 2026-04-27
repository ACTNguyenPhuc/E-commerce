import type { ApiResponse } from "@/types/api";

export function extractData<T>(res: { data: ApiResponse<T> }): T {
  const body = res.data;
  if (!body?.success) {
    const errMsg = body?.errors?.[0]?.message || body?.message || "Lỗi API";
    throw Object.assign(new Error(errMsg), { code: body?.code, errors: body?.errors });
  }
  return body.data;
}
