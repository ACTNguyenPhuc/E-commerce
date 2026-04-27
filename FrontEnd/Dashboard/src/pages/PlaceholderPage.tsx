import { Empty } from "antd";
import { PageHeader } from "@/components/common/PageHeader";

export function PlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <>
      <PageHeader title={title} description={description} />
      <Empty description="Chưa triển khai" />
    </>
  );
}
