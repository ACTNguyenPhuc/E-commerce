import { create } from "zustand";
import { persist } from "zustand/middleware";

type UiState = {
  sidebarCollapsed: boolean;
  setSidebarCollapsed: (v: boolean) => void;
  toggleSidebar: () => void;
  themeMode: "light" | "dark";
  setThemeMode: (v: "light" | "dark") => void;
  toggleTheme: () => void;
};

export const useUiStore = create<UiState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      setSidebarCollapsed: (v) => set({ sidebarCollapsed: v }),
      toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),
      themeMode: "light",
      setThemeMode: (v) => set({ themeMode: v }),
      toggleTheme: () => set((s) => ({ themeMode: s.themeMode === "dark" ? "light" : "dark" })),
    }),
    { name: "ecom-admin-ui" }
  )
);
