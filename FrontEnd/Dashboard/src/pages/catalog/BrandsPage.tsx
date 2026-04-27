import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { catalogApi } from "@/api/catalog.api";
import { PageHeader } from "@/components/common/PageHeader";
import { PermissionGate } from "@/components/common/PermissionGate";
import type { Brand, BrandRequest } from "@/types/catalog";
import { FileOrUrlImageInput } from "@/components/common/FileOrUrlImageInput";

export function BrandsPage() {
  const { message } = App.useApp();
  const qc = useQueryClient();
  const { data: list = [], isLoading } = useQuery({ queryKey: ["brands"], queryFn: () => catalogApi.brands() });
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Brand | null>(null);
  const [form] = Form.useForm<BrandRequest>();
  const [logoFile, setLogoFile] = useState<File | null>(null);
  const useFileUploadW = Form.useWatch("useFileUpload", form) ?? false;
  const logoUrlW = Form.useWatch("logoUrl", form) ?? "";

  useEffect(() => {
    if (!open) {
      setEditing(null);
      form.resetFields();
      setLogoFile(null);
    }
  }, [open, form]);

  const openCreate = () => {
    setEditing(null);
    setOpen(true);
  };

  const openEdit = (b: Brand) => {
    setEditing(b);
    form.setFieldsValue({
      name: b.name,
      slug: b.slug,
      description: b.description ?? undefined,
      logoUrl: b.logoUrl ?? undefined,
      useFileUpload: false,
      status: b.status,
    });
    setOpen(true);
  };

  const saveMut = useMutation({
    mutationFn: (payload: { body: BrandRequest; file?: File | null }) =>
      editing
        ? catalogApi.updateBrand(editing.id, payload.body, payload.file)
        : catalogApi.createBrand(payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã lưu");
      setOpen(false);
      await qc.invalidateQueries({ queryKey: ["brands"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi lưu"),
  });

  const delMut = useMutation({
    mutationFn: (id: number) => catalogApi.deleteBrand(id),
    onSuccess: async () => {
      message.success("Đã xoá");
      await qc.invalidateQueries({ queryKey: ["brands"] });
    },
    onError: () => message.error("Không xoá được thương hiệu."),
  });

  return (
    <>
      <PageHeader
        title="Thương hiệu"
        description="Quản lý thương hiệu. CRUD: chỉ admin."
        extra={
          <PermissionGate roles={["admin"]}>
            <Button type="primary" onClick={openCreate}>
              Thêm thương hiệu
            </Button>
          </PermissionGate>
        }
      />
      <Table<Brand>
        loading={isLoading}
        rowKey="id"
        dataSource={list}
        columns={[
          { title: "Tên", dataIndex: "name" },
          { title: "Slug", dataIndex: "slug" },
          {
            title: "Trạng thái",
            dataIndex: "status",
            render: (s: string) => <Tag color={s === "active" ? "green" : "default"}>{s}</Tag>,
            width: 120,
          },
          {
            title: "Thao tác",
            key: "act",
            width: 200,
            render: (_, b) => (
              <PermissionGate
                roles={["admin"]}
                fallback={
                  <Tag color="default" style={{ margin: 0 }}>
                    Chỉ xem
                  </Tag>
                }
              >
                <Space>
                  <Button type="link" onClick={() => openEdit(b)}>
                    Sửa
                  </Button>
                  <Popconfirm
                    title="Xoá thương hiệu?"
                    onConfirm={() => delMut.mutate(b.id)}
                    okText="Xoá"
                    cancelText="Huỷ"
                  >
                    <Button type="link" danger loading={delMut.isPending}>
                      Xoá
                    </Button>
                  </Popconfirm>
                </Space>
              </PermissionGate>
            ),
          },
        ]}
        pagination={false}
      />
      <Modal
        open={open}
        title={editing ? "Sửa thương hiệu" : "Thêm thương hiệu"}
        onCancel={() => setOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Form<BrandRequest>
          form={form}
          layout="vertical"
          initialValues={{ useFileUpload: false, status: "active" }}
          onFinish={(v) => {
            const file = v.useFileUpload ? logoFile : null;
            saveMut.mutate({ body: v, file });
          }}
        >
          <Form.Item name="useFileUpload" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="logoUrl" hidden>
            <Input />
          </Form.Item>

          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="slug" label="Slug (tuỳ chọn)">
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item label="Logo">
            <FileOrUrlImageInput
              value={{
                mode: useFileUploadW ? "file" : "url",
                url: logoUrlW,
                file: logoFile,
              }}
              onChange={(next) => {
                form.setFieldValue("useFileUpload", next.mode === "file");
                form.setFieldValue("logoUrl", next.url || undefined);
                setLogoFile(next.file ?? null);
              }}
              urlPlaceholder="https://... hoặc /api/uploads/..."
            />
          </Form.Item>
          <Form.Item name="status" label="Trạng thái">
            <Select
              options={[
                { value: "active", label: "Hoạt động" },
                { value: "inactive", label: "Tắt" },
              ]}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={saveMut.isPending} block>
            Lưu
          </Button>
        </Form>
      </Modal>
    </>
  );
}
