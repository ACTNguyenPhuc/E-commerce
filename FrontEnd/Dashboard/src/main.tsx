import React, { useEffect } from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider, App as AntdApp } from "antd";
import viVN from "antd/locale/vi_VN";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import App from "./App";
import "./index.css";
import { useUiStore } from "@/stores/uiStore";
import { getDashboardTheme } from "@/theme/dashboardTheme";

dayjs.locale("vi");

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 30_000 },
  },
});

function RootProviders() {
  const mode = useUiStore((s) => s.themeMode);
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", mode);
  }, [mode]);
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={viVN} theme={getDashboardTheme(mode)}>
        <AntdApp>
          <App />
        </AntdApp>
      </ConfigProvider>
    </QueryClientProvider>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RootProviders />
  </React.StrictMode>
);
