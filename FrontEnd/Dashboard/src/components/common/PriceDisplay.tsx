import { formatVnd } from "@/utils/format";
import { Typography } from "antd";

export function PriceDisplay({ value }: { value: string | number | null | undefined }) {
  return <Typography.Text strong>{formatVnd(value)}</Typography.Text>;
}
