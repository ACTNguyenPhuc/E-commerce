import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { catalogApi } from "@/api/catalog.api";
import { PageHeader } from "@/components/common/PageHeader";
import { PermissionGate } from "@/components/common/PermissionGate";
import type { Category, CategoryRequest } from "@/types/catalog";
import { flattenCategories } from "@/utils/catalogTree";
import { FileOrUrlImageInput } from "@/components/common/FileOrUrlImageInput";

function idToNameMap(nodes: Category[]): Map<number, string> {
  const m = new Map<number, string>();
  const walk = (list: Category[]) => {
    for (const c of list) {
      m.set(c.id, c.name);
      if (c.children?.length) walk(c.children);
    }
  };
  walk(nodes);
  return m;
}

function findCategoryById(nodes: Category[], id: number): Category | null {
  for (const n of nodes) {
    if (n.id === id) return n;
    if (n.children?.length) {
      const f = findCategoryById(n.children, id);
      if (f) return f;
    }
  }
  return null;
}

export function CategoriesPage() {
  const { message } = App.useApp();
  const qc = useQueryClient();
  const { data: tree = [], isLoading } = useQuery({
    queryKey: ["categories", "tree"],
    queryFn: () => catalogApi.categories(),
  });

  const parentMap = idToNameMap(tree);
  const flat = flattenCategories(tree);
  const rows = flat
    .map((r) => {
      const cat = findCategoryById(tree, r.id);
      if (!cat) return null;
      const pName = cat.parentId != null ? (parentMap.get(cat.parentId) ?? `#${cat.parentId}`) : "—";
      return {
        key: r.id,
        id: r.id,
        name: r.name,
        parentName: pName,
        slug: cat.slug,
        order: cat.displayOrder,
        status: cat.status,
      };
    })
    .filter((x): x is NonNullable<typeof x> => x != null);

  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm<CategoryRequest>();
  const [imageFile, setImageFile] = useState<File | null>(null);
  const useFileUploadW = Form.useWatch("useFileUpload", form) ?? false;
  const imageUrlW = Form.useWatch("imageUrl", form) ?? "";

  useEffect(() => {
    if (!open) {
      setEditingId(null);
      form.resetFields();
      setImageFile(null);
    }
  }, [open, form]);

  const openCreate = () => {
    setEditingId(null);
    form.setFieldsValue({
      name: "",
      displayOrder: 0,
    });
    setOpen(true);
  };

  const openEdit = (id: number) => {
    const c = findCategoryById(tree, id);
    if (!c) return;
    setEditingId(id);
    form.setFieldsValue({
      name: c.name,
      parentId: c.parentId ?? undefined,
      slug: c.slug,
      description: c.description ?? undefined,
      imageUrl: c.imageUrl ?? undefined,
      useFileUpload: false,
      displayOrder: c.displayOrder,
      status: c.status,
    });
    setOpen(true);
  };

  const saveMut = useMutation({
    mutationFn: async (payload: { body: CategoryRequest; file?: File | null }) => {
      if (editingId != null) return catalogApi.updateCategory(editingId, payload.body, payload.file);
      return catalogApi.createCategory(payload.body, payload.file);
    },
    onSuccess: async () => {
      message.success("Đã lưu");
      setOpen(false);
      await qc.invalidateQueries({ queryKey: ["categories"] });
    },
    onError: (e: unknown) => {
      message.error(e instanceof Error ? e.message : "Lỗi lưu danh mục");
    },
  });

  const delMut = useMutation({
    mutationFn: (id: number) => catalogApi.deleteCategory(id),
    onSuccess: async () => {
      message.success("Đã xoá");
      await qc.invalidateQueries({ queryKey: ["categories"] });
    },
    onError: () => message.error("Không xoá được (có thể còn ràng buộc)."),
  });

  const parentOptions = flat.map((r) => ({
    value: r.id,
    label: `${"— ".repeat(r.depth)}${r.name}`,
  }));

  return (
    <>
      <PageHeader
        title="Danh mục"
        extra={
          <PermissionGate roles={["admin"]}>
            <Button type="primary" onClick={openCreate}>
              Thêm danh mục
            </Button>
          </PermissionGate>
        }
      />
      <Table
        loading={isLoading}
        dataSource={rows}
        rowKey="id"
        columns={[
          { title: "Tên", dataIndex: "name", key: "name" },
          { title: "Slug", dataIndex: "slug", key: "slug" },
          { title: "Cha", dataIndex: "parentName", key: "parent" },
          { title: "Thứ tự", dataIndex: "order", key: "order", width: 90 },
          {
            title: "Trạng thái",
            dataIndex: "status",
            key: "status",
            width: 120,
            render: (s: string) => <Tag color={s === "active" ? "green" : "default"}>{s}</Tag>,
          },
          {
            title: "Thao tác",
            key: "act",
            width: 200,
            render: (_, r) => (
              <PermissionGate
                roles={["admin"]}
                fallback={
                  <Tag color="default" style={{ margin: 0 }}>
                    Chỉ xem
                  </Tag>
                }
              >
                <Space>
                  <Button type="link" onClick={() => openEdit(r.id)}>
                    Sửa
                  </Button>
                  <Popconfirm
                    title="Xoá danh mục?"
                    onConfirm={() => delMut.mutate(r.id)}
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
        title={editingId == null ? "Thêm danh mục" : "Sửa danh mục"}
        onCancel={() => setOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Form<CategoryRequest>
          form={form}
          layout="vertical"
          initialValues={{ useFileUpload: false, displayOrder: 0, status: "active" }}
          onFinish={(v) => {
            const body: CategoryRequest = { ...v, parentId: v.parentId ?? null };
            if (editingId != null && body.parentId === editingId) {
              message.error("Danh mục không thể là cha của chính nó");
              return;
            }
            const file = body.useFileUpload ? imageFile : null;
            saveMut.mutate({ body, file });
          }}
        >
          <Form.Item name="useFileUpload" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="imageUrl" hidden>
            <Input />
          </Form.Item>

          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="parentId" label="Danh mục cha">
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              options={parentOptions}
              placeholder="(Gốc)"
            />
          </Form.Item>
          <Form.Item name="slug" label="Slug (tuỳ chọn)">
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item label="Hình danh mục">
            <FileOrUrlImageInput
              value={{
                mode: useFileUploadW ? "file" : "url",
                url: imageUrlW,
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
          <Form.Item name="displayOrder" label="Thứ tự hiển thị">
            <InputNumber className="w-full" min={0} style={{ width: "100%" }} />
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
