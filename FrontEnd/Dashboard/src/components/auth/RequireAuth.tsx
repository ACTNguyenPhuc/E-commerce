import { Spin } from "antd";
import { useSyncExternalStore, type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";

function useRehydrated() {
  return useSyncExternalStore(
    (onStoreChange) => {
      if (useAuthStore.persist.hasHydrated()) {
        onStoreChange();
        return () => undefined;
      }
      return useAuthStore.persist.onFinishHydration(() => onStoreChange());
    },
    () => useAuthStore.persist.hasHydrated(),
    () => true
  );
}

export function RequireAuth({ children }: { children: ReactNode }) {
  const loc = useLocation();
  const { accessToken, user } = useAuthStore();
  const ok = useRehydrated();

  if (!ok) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!accessToken) {
    return <Navigate to="/login" state={{ from: loc.pathname }} replace />;
  }
  if (user?.role === "customer") {
    return <Navigate to="/forbidden" replace />;
  }
  return <>{children}</>;
}
