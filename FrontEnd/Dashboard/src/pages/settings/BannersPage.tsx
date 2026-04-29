import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from "antd";
import { useEffect, useMemo, useState } from "react";
import { bannerApi } from "@/api/banner.api";
import type { Banner, BannerRequest, CommonStatus } from "@/types/banner";
import { PageHeader } from "@/components/common/PageHeader";
import { FileOrUrlImageInput } from "@/components/common/FileOrUrlImageInput";
import { PlusOutlined } from "@ant-design/icons";

type FormVals = BannerRequest & { _imageFile?: File | null; _useFileUpload?: boolean };

const STATUS_OPTS: { value: CommonStatus; label: string }[] = [
  { value: "active", label: "Active" },
  { value: "inactive", label: "Inactive" },
];

export function BannersPage() {
  const { message } = App.useApp();
  const qc = useQueryClient();
  const { data: list = [], isLoading } = useQuery({ queryKey: ["admin", "banners"], queryFn: () => bannerApi.adminList() });

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Banner | null>(null);
  const [form] = Form.useForm<FormVals>();
  const [imageFile, setImageFile] = useState<File | null>(null);
  const useFileUploadW = Form.useWatch("useFileUpload", form) ?? false;
  const imageUrlW = Form.useWatch("imageUrl", form) ?? "";

  useEffect(() => {
    if (!open) {
      setEditing(null);
      setImageFile(null);
      form.resetFields();
    }
  }, [open, form]);

  const createMut = useMutation({
    mutationFn: (payload: { body: BannerRequest; file?: File | null }) => bannerApi.create(payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã tạo banner");
      setOpen(false);
      await qc.invalidateQueries({ queryKey: ["admin", "banners"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi tạo banner"),
  });

  const updateMut = useMutation({
    mutationFn: (payload: { id: number; body: BannerRequest; file?: File | null }) =>
      bannerApi.update(payload.id, payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã cập nhật banner");
      setOpen(false);
      await qc.invalidateQueries({ queryKey: ["admin", "banners"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật banner"),
  });

  const delMut = useMutation({
    mutationFn: (id: number) => bannerApi.delete(id),
    onSuccess: async () => {
      message.success("Đã xoá banner");
      await qc.invalidateQueries({ queryKey: ["admin", "banners"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá banner"),
  });

  const openCreate = () => {
    setEditing(null);
    setImageFile(null);
    form.setFieldsValue({ status: "active", displayOrder: 0, useFileUpload: false });
    setOpen(true);
  };

  const openEdit = (b: Banner) => {
    setEditing(b);
    setImageFile(null);
    form.setFieldsValue({
      title: b.title ?? undefined,
      subtitle: b.subtitle ?? undefined,
      imageUrl: b.imageUrl ?? undefined,
      useFileUpload: false,
      linkUrl: b.linkUrl ?? undefined,
      displayOrder: b.displayOrder,
      status: b.status,
    });
    setOpen(true);
  };

  const sorted = useMemo(() => [...list].sort((a, b) => a.displayOrder - b.displayOrder || a.id - b.id), [list]);

  return (
    <>
      <PageHeader
        title="Banners"
        description="Quản lý banner hiển thị trang chủ/marketing. Hỗ trợ URL hoặc upload file."
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} style={{ borderRadius: 999 }}>
            Add banner
          </Button>
        }
      />

      <Card style={{ borderRadius: 14 }}>
        <Table<Banner>
          loading={isLoading}
          rowKey="id"
          dataSource={sorted}
          pagination={false}
          columns={[
            { title: "#", dataIndex: "id", width: 70 },
            { title: "Title",  dataIndex: "title", render: (v: string | null) => v ?? "—" },
            { title: "Subtitle",  dataIndex: "subtitle", render: (v: string | null) => v ?? "—" },
            { title: "Order", dataIndex: "displayOrder", width: 90 },
            {
              title: "Status",
              dataIndex: "status",
              width: 110,
              render: (s: CommonStatus) => (
                <Tag color={s === "active" ? "green" : "default"} style={{ borderRadius: 999, padding: "2px 10px" }}>
                  {s}
                </Tag>
              ),
            },
            {
              title: "Action",
              key: "act",
              width: 200,
              render: (_, b) => (
                <Space>
                  <Button onClick={() => openEdit(b)}>Edit</Button>
                  <Popconfirm title="Xoá banner?" onConfirm={() => delMut.mutate(b.id)}>
                    <Button danger loading={delMut.isPending}>
                      Delete
                    </Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? "Edit banner" : "Add banner"}
        onCancel={() => setOpen(false)}
        footer={null}
        destroyOnClose
        width={820}
      >
        <Form<FormVals>
          form={form}
          layout="vertical"
          initialValues={{ status: "active", displayOrder: 0, useFileUpload: false }}
          onFinish={(v) => {
            const useFileUpload = Boolean(v.useFileUpload) && !!imageFile;
            const body: BannerRequest = {
              title: v.title,
              subtitle: v.subtitle,
              imageUrl: useFileUpload ? undefined : v.imageUrl,
              useFileUpload,
              linkUrl: v.linkUrl,
              displayOrder: v.displayOrder,
              status: v.status,
            };
            const file = useFileUpload ? imageFile : null;
            if (editing) updateMut.mutate({ id: editing.id, body, file });
            else createMut.mutate({ body, file });
          }}
        >
          <Form.Item name="useFileUpload" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="imageUrl" hidden>
            <Input />
          </Form.Item>

          <Space wrap style={{ width: "100%" }} size={16} align="start">
            <Form.Item name="title" label="Title" style={{ flex: "1 1 280px" }}>
              <Input placeholder="Summer sale" />
            </Form.Item>
            
          </Space>
          <Form.Item name="subtitle" label="Subtitle" style={{ display: "block" }}>
              <Input.TextArea
                placeholder="Up to 50% off (có thể dán JSON config cho Home hero)"
                autoSize={{ minRows: 4, maxRows: 10 }}
                showCount
                maxLength={2048}
              />
            </Form.Item>
          <Form.Item label="Image">
            <FileOrUrlImageInput
              value={{
                mode: useFileUploadW ? "file" : "url",
                url: useFileUploadW ? "" : imageUrlW,
                file: imageFile,
              }}
              onChange={(next) => {
                form.setFieldValue("useFileUpload", next.mode === "file");
                form.setFieldValue("imageUrl", next.url || undefined);
                setImageFile(next.file ?? null);
              }}
              urlPlaceholder="https://... hoặc /api/uploads/..."
            />
          </Form.Item>

          <Space wrap style={{ width: "100%" }} size={16} align="start">
            <Form.Item name="linkUrl" label="Link URL" style={{ flex: "1 1 420px" }}>
              <Input placeholder="https://..." />
            </Form.Item>
            <Form.Item name="displayOrder" label="Display order" style={{ flex: "1 1 180px" }}>
              <InputNumber min={0} style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="status" label="Status" style={{ flex: "1 1 180px" }}>
              <Select options={STATUS_OPTS} />
            </Form.Item>
          </Space>

          <Space style={{ width: "100%", justifyContent: "flex-end" }}>
            <Button onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit" loading={createMut.isPending || updateMut.isPending} style={{ borderRadius: 999 }}>
              Save
            </Button>
          </Space>
        </Form>
      </Modal>
    </>
  );
}

