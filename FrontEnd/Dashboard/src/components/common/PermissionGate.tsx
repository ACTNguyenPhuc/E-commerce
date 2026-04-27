import type { ReactNode } from "react";
import { useAuthStore } from "@/stores/authStore";
import type { Role } from "@/types/user";

export function PermissionGate({
  roles,
  children,
  fallback = null,
}: {
  roles: Role[];
  children: ReactNode;
  fallback?: ReactNode;
}) {
  const role = useAuthStore((s) => s.user?.role);
  if (!role || !roles.includes(role)) return <>{fallback}</>;
  return <>{children}</>;
}
