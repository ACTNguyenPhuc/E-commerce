import { useAuthStore } from "@/stores/authStore";
import type { Role } from "@/types/user";

export function usePermission() {
  const user = useAuthStore((s) => s.user);
  const isAdmin = user?.role === "admin";
  const isStaff = user?.role === "staff";
  const isAdminOrStaff = isAdmin || isStaff;
  return {
    user,
    isAdmin,
    isStaff,
    isAdminOrStaff,
    can: (roles: Role[]) => (user ? roles.includes(user.role) : false),
  };
}
