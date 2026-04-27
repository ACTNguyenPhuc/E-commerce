import dayjs from "dayjs";

export function formatVnd(n: string | number | null | undefined): string {
  if (n == null || n === "") return "—";
  const v = typeof n === "string" ? parseFloat(n) : n;
  if (Number.isNaN(v)) return "—";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    v
  );
}

export function formatDateTime(s: string | null | undefined): string {
  if (!s) return "—";
  return dayjs(s).format("DD/MM/YYYY HH:mm");
}

export function formatDate(s: string | null | undefined): string {
  if (!s) return "—";
  return dayjs(s).format("DD/MM/YYYY");
}
