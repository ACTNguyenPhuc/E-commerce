import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User, AuthResponse } from "@/types/user";
import { publicApi } from "@/api/publicClient";
import { extractData } from "@/utils/apiHelpers";
import type { ApiResponse } from "@/types/api";
import type { ChangePasswordRequest } from "@/types/user";

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  setAuth: (data: AuthResponse) => void;
  setUser: (u: User) => void;
  clearAuth: () => void;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  changePassword: (body: ChangePasswordRequest) => Promise<void>;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setAuth: (data) => {
        set({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          user: data.user,
        });
      },
      setUser: (u) => set({ user: u }),
      clearAuth: () => set({ accessToken: null, refreshToken: null, user: null }),
      login: async (email, password) => {
        const r = await publicApi.post<ApiResponse<AuthResponse>>("/v1/auth/login", { email, password });
        const data = extractData(r);
        if (data.user.role === "customer") {
          throw new Error("Tài khoản không có quyền truy cập quản trị");
        }
        get().setAuth(data);
      },
      logout: () => {
        get().clearAuth();
        window.location.href = "/login";
      },
      changePassword: async (body) => {
        const a = (await import("axios")).default;
        const { api } = await import("@/api/client");
        try {
          const r = await api.post<ApiResponse<null>>("/v1/auth/change-password", body);
          extractData(r);
        } catch (e) {
          if (a.isAxiosError(e) && e.response?.data) {
            const b = e.response.data as ApiResponse<null>;
            const msg = b?.errors?.[0]?.message || b?.message || "Đổi mật khẩu thất bại";
            throw new Error(msg);
          }
          throw e;
        }
      },
    }),
    { name: "ecom-admin-auth" }
  )
);
