import axios, { type AxiosError } from "axios";
import type { ApiResponse } from "@/types/api";
import { useAuthStore } from "@/stores/authStore";
import { publicApi } from "@/api/publicClient";
import { message } from "antd";

const baseURL = import.meta.env.VITE_API_URL || "/api";

export const api = axios.create({
  baseURL,
  timeout: 30_000,
  // Không set Content-Type mặc định để request multipart/form-data (FormData) tự hoạt động đúng.
});

let refreshPromise: Promise<boolean> | null = null;

api.interceptors.request.use((config) => {
  const t = useAuthStore.getState().accessToken;
  if (t) {
    config.headers.Authorization = `Bearer ${t}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => {
    const b = res.data as ApiResponse<unknown> | undefined;
    if (b && typeof b === "object" && "success" in b && b.success === false) {
      return Promise.reject({ response: { data: b, status: res.status, config: res.config } });
    }
    return res;
  },
  async (err: AxiosError) => {
    const original = err.config;
    if (!original) return Promise.reject(err);
    const sc = err.response?.status;
    if (sc === 401 && !("_retry" in original)) {
      (original as { _retry?: boolean })._retry = true;
      if (!refreshPromise) {
        refreshPromise = (async () => {
          const rt = useAuthStore.getState().refreshToken;
          if (!rt) return false;
          try {
            const r = await publicApi.post<ApiResponse<import("@/types/user").AuthResponse>>(
              "/v1/auth/refresh",
              { refreshToken: rt }
            );
            if (r.data.success && r.data.data) {
              useAuthStore.getState().setAuth(r.data.data);
              return true;
            }
          } catch {
            // ignore
          } finally {
            refreshPromise = null;
          }
          return false;
        })();
      }
      const ok = await refreshPromise;
      if (ok) {
        const t = useAuthStore.getState().accessToken;
        if (t) original.headers.Authorization = `Bearer ${t}`;
        return api.request(original);
      }
      useAuthStore.getState().clearAuth();
      message.error("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại");
      if (!window.location.pathname.startsWith("/login")) {
        window.location.href = "/login?redirect=" + encodeURIComponent(window.location.pathname);
      }
    }
    if (sc === 403) {
      if (!window.location.pathname.startsWith("/forbidden")) {
        window.location.href = "/forbidden";
      }
    }
    return Promise.reject(err);
  }
);

export { publicApi } from "@/api/publicClient";
export { extractData } from "@/utils/apiHelpers";
