import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  App,
  Button,
  Card,
  Descriptions,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Table,
  Tabs,
  Tag,
  Typography,
  theme,
} from "antd";
import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { catalogApi } from "@/api/catalog.api";
import { api } from "@/api/client";
import { PageHeader } from "@/components/common/PageHeader";
import { PermissionGate } from "@/components/common/PermissionGate";
import { PriceDisplay } from "@/components/common/PriceDisplay";
import type {
  AttributeValueResponse,
  ProductAttribute,
  ProductAttributeRequest,
  ProductAttributeValueRequest,
  ProductDetail,
  ProductRequest,
  ProductStatus,
  VariantRequest,
} from "@/types/catalog";
import { categoryOptions } from "@/utils/catalogTree";
import {
  AppstoreOutlined,
  ArrowLeftOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  SaveOutlined,
  SettingOutlined,
  TagsOutlined,
} from "@ant-design/icons";
import { FileOrUrlImageInput } from "@/components/common/FileOrUrlImageInput";
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";
// Quill plugins (Quill v2 still works with these via legacy peer deps)
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BlotFormatter from "quill-blot-formatter";

export function ProductFormPage() {
  const { slug = "" } = useParams<{ slug: string }>();
  const { message } = App.useApp();
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const isNew = slug === "new";

  const [form] = Form.useForm<ProductRequest & { isFeaturedCheck?: boolean }>();
  const [varOpen, setVarOpen] = useState(false);
  const [variantEditingId, setVariantEditingId] = useState<number | null>(null);
  const [varForm] = Form.useForm<VariantRequest>();
  const [thumbFile, setThumbFile] = useState<File | null>(null);
  const [variantFile, setVariantFile] = useState<File | null>(null);
  const [variantHasImage, setVariantHasImage] = useState(true);
  const [imagesOpen, setImagesOpen] = useState(false);
  const [newImages, setNewImages] = useState<File[]>([]);
  const [newImageUrl, setNewImageUrl] = useState("");
  const [newImagePrimary, setNewImagePrimary] = useState(false);
  const [attrModalOpen, setAttrModalOpen] = useState(false);
  const [attrEditing, setAttrEditing] = useState<ProductAttribute | null>(null);
  const [attrValueModalOpen, setAttrValueModalOpen] = useState(false);
  const [selectedAttrId, setSelectedAttrId] = useState<number | null>(null);
  const [attrForm] = Form.useForm<ProductAttributeRequest>();
  const [attrValueForm] = Form.useForm<Omit<ProductAttributeValueRequest, "attributeId">>();
  const useThumbFileUploadW = Form.useWatch("useFileUpload", form) ?? false;
  const thumbnailUrlW = Form.useWatch("thumbnailUrl", form) ?? "";
  const useVarFileUploadW = Form.useWatch("useFileUpload", varForm) ?? false;
  const varImageUrlW = Form.useWatch("imageUrl", varForm) ?? "";
  const richDescW = Form.useWatch("description", form) ?? "";
  const quillRef = useRef<ReactQuill | null>(null);

  // Register Quill modules once
  useEffect(() => {
    const Quill = (ReactQuill as unknown as { Quill?: any }).Quill;
    if (!Quill) return;
    try {
      // Avoid double-register in dev/hmr
      if (!Quill.imports?.["modules/blotFormatter"]) {
        Quill.register("modules/blotFormatter", BlotFormatter);
      }
    } catch {
      // ignore
    }
  }, []);

  // Support paste/drag-drop images: upload to server and insert URL (no base64)
  useEffect(() => {
    const editor = quillRef.current?.getEditor?.();
    const root: HTMLElement | null = editor?.root ?? null;
    if (!editor || !root) return;

    const uploadAndInsert = async (files: FileList | File[]) => {
      const arr = Array.from(files);
      for (const f of arr) {
        if (!f.type?.startsWith("image/")) continue;
        try {
          const fd = new FormData();
          fd.append("file", f);
          fd.append("folder", "product-descriptions");
          const res = await api.post("/v1/uploads/image", fd);
          const url = (res.data?.data?.url ?? res.data?.url) as string;
          if (!url) throw new Error("Upload failed");

          const range = editor.getSelection(true) ?? { index: editor.getLength(), length: 0 };
          editor.insertEmbed(range.index, "image", url, "user");
          editor.setSelection(range.index + 1, 0);
        } catch (e) {
          message.error("Upload ảnh thất bại");
          // eslint-disable-next-line no-console
          console.error(e);
        }
      }
    };

    const onPaste = (ev: ClipboardEvent) => {
      const items = ev.clipboardData?.items;
      if (!items) return;
      const files: File[] = [];
      for (const it of Array.from(items)) {
        if (it.kind === "file") {
          const f = it.getAsFile();
          if (f && f.type.startsWith("image/")) files.push(f);
        }
      }
      if (files.length) {
        ev.preventDefault();
        void uploadAndInsert(files);
      }
    };

    const onDrop = (ev: DragEvent) => {
      const dt = ev.dataTransfer;
      const files = dt?.files;
      if (files && files.length) {
        const hasImage = Array.from(files).some((f) => f.type.startsWith("image/"));
        if (hasImage) {
          ev.preventDefault();
          void uploadAndInsert(files);
        }
      }
    };

    root.addEventListener("paste", onPaste as unknown as EventListener);
    root.addEventListener("drop", onDrop as unknown as EventListener);
    root.addEventListener("dragover", (e) => e.preventDefault());

    return () => {
      root.removeEventListener("paste", onPaste as unknown as EventListener);
      root.removeEventListener("drop", onDrop as unknown as EventListener);
    };
  }, [message]);

  useEffect(() => {
    if (!varOpen) {
      varForm.resetFields();
      setVariantFile(null);
      setVariantHasImage(true);
      setVariantEditingId(null);
    } else {
      varForm.setFieldsValue({ useFileUpload: false });
    }
  }, [varOpen, varForm]);

  const { data: product, isLoading: loadingP } = useQuery({
    queryKey: ["product", "admin", slug],
    queryFn: () => catalogApi.productBySlug(slug),
    enabled: !isNew && Boolean(slug),
  });

  const { data: tree = [] } = useQuery({
    queryKey: ["categories", "tree"],
    queryFn: () => catalogApi.categories(),
  });
  const { data: brands = [] } = useQuery({ queryKey: ["brands"], queryFn: () => catalogApi.brands() });
  const { data: attrValues = [] } = useQuery({
    queryKey: ["admin", "attribute-values"],
    queryFn: () => catalogApi.attributeValues(),
  });
  const { data: attrs = [] } = useQuery({
    queryKey: ["admin", "attributes"],
    queryFn: () => catalogApi.attributes(),
  });
  const { data: attrValuesByAttr = [] } = useQuery({
    queryKey: ["admin", "attributes", selectedAttrId, "values"],
    queryFn: () => catalogApi.attributeValuesByAttribute(selectedAttrId!),
    enabled: selectedAttrId != null,
  });

  useEffect(() => {
    if (isNew) {
      form.setFieldsValue({
        status: "draft",
        isFeaturedCheck: false,
        basePrice: "0",
        useFileUpload: false,
      });
    } else if (product) {
      form.setFieldsValue({
        categoryId: product.categoryId,
        brandId: product.brandId ?? undefined,
        sku: product.sku,
        name: product.name,
        slug: product.slug,
        shortDescription: product.shortDescription ?? undefined,
        description: product.description ?? undefined,
        basePrice: product.basePrice,
        salePrice: product.salePrice ?? undefined,
        thumbnailUrl: product.thumbnailUrl ?? undefined,
        status: product.status,
        isFeaturedCheck: product.isFeatured,
        useFileUpload: false,
      });
    }
  }, [isNew, product, form]);

  const createMut = useMutation({
    mutationFn: (payload: { body: ProductRequest; file?: File | null }) =>
      catalogApi.createProduct(payload.body, payload.file),
    onSuccess: async (p: ProductDetail) => {
      message.success("Đã tạo sản phẩm");
      await qc.invalidateQueries({ queryKey: ["admin", "products"] });
      void navigate(`/catalog/products/${p.slug}`, { replace: true });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi tạo"),
  });

  const updateMut = useMutation({
    mutationFn: (payload: { body: ProductRequest; file?: File | null }) =>
      catalogApi.updateProduct(product!.id, payload.body, payload.file),
    onSuccess: async (p: ProductDetail) => {
      message.success("Đã cập nhật");
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
      await qc.invalidateQueries({ queryKey: ["admin", "products"] });
      if (p.slug !== slug) {
        void navigate(`/catalog/products/${p.slug}`, { replace: true });
      } else {
        void qc.refetchQueries({ queryKey: ["product", "admin", slug] });
      }
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật"),
  });

  const delMut = useMutation({
    mutationFn: () => catalogApi.deleteProduct(product!.id),
    onSuccess: async () => {
      message.success("Đã xoá");
      await qc.invalidateQueries({ queryKey: ["admin", "products"] });
      void navigate("/catalog/products", { replace: true });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Không xoá được"),
  });

  const addVarMut = useMutation({
    mutationFn: (payload: { body: VariantRequest; file?: File | null }) =>
      catalogApi.addVariant(product!.id, payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã thêm biến thể");
      setVarOpen(false);
      varForm.resetFields();
      setVariantFile(null);
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi thêm biến thể"),
  });

  const updateVarMut = useMutation({
    mutationFn: (payload: { variantId: number; body: VariantRequest; file?: File | null }) =>
      catalogApi.updateVariant(product!.id, payload.variantId, payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã cập nhật biến thể");
      setVarOpen(false);
      varForm.resetFields();
      setVariantFile(null);
      setVariantEditingId(null);
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật biến thể"),
  });

  const delVarMut = useMutation({
    mutationFn: (vid: number) => catalogApi.deleteVariant(product!.id, vid),
    onSuccess: async () => {
      message.success("Đã xoá biến thể");
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá"),
  });

  const addImgMut = useMutation({
    mutationFn: (payload: { productId: number; body: { imageUrl?: string; useFileUpload?: boolean; isPrimary?: boolean }; file?: File | null }) =>
      catalogApi.addProductImage(payload.productId, payload.body, payload.file),
    onSuccess: async () => {
      message.success("Đã thêm ảnh");
      setNewImages([]);
      setNewImageUrl("");
      setNewImagePrimary(false);
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi thêm ảnh"),
  });

  const delImgMut = useMutation({
    mutationFn: (payload: { productId: number; imageId: number }) =>
      catalogApi.deleteProductImage(payload.productId, payload.imageId),
    onSuccess: async () => {
      message.success("Đã xoá ảnh");
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá ảnh"),
  });

  const primaryImgMut = useMutation({
    mutationFn: (payload: { productId: number; imageId: number }) =>
      catalogApi.setPrimaryProductImage(payload.productId, payload.imageId),
    onSuccess: async () => {
      message.success("Đã đặt ảnh chính");
      await qc.invalidateQueries({ queryKey: ["product", "admin", slug] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật ảnh chính"),
  });

  const createAttrMut = useMutation({
    mutationFn: (payload: { body: ProductAttributeRequest }) => catalogApi.createAttribute(payload.body),
    onSuccess: async () => {
      setAttrModalOpen(false);
      setAttrEditing(null);
      attrForm.resetFields();
      message.success("Đã thêm thuộc tính");
      await qc.invalidateQueries({ queryKey: ["admin", "attributes"] });
      await qc.invalidateQueries({ queryKey: ["admin", "attribute-values"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi thêm thuộc tính"),
  });

  const updateAttrMut = useMutation({
    mutationFn: (payload: { id: number; body: ProductAttributeRequest }) =>
      catalogApi.updateAttribute(payload.id, payload.body),
    onSuccess: async () => {
      setAttrModalOpen(false);
      setAttrEditing(null);
      attrForm.resetFields();
      message.success("Đã cập nhật thuộc tính");
      await qc.invalidateQueries({ queryKey: ["admin", "attributes"] });
      await qc.invalidateQueries({ queryKey: ["admin", "attribute-values"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật thuộc tính"),
  });

  const delAttrMut = useMutation({
    mutationFn: (id: number) => catalogApi.deleteAttribute(id),
    onSuccess: async () => {
      message.success("Đã xoá thuộc tính");
      if (selectedAttrId != null) setSelectedAttrId(null);
      await qc.invalidateQueries({ queryKey: ["admin", "attributes"] });
      await qc.invalidateQueries({ queryKey: ["admin", "attribute-values"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá thuộc tính"),
  });

  const createAttrValueMut = useMutation({
    mutationFn: (payload: { attributeId: number; body: Omit<ProductAttributeValueRequest, "attributeId"> }) =>
      catalogApi.createAttributeValue({ attributeId: payload.attributeId, ...payload.body }),
    onSuccess: async () => {
      message.success("Đã thêm giá trị");
      setAttrValueModalOpen(false);
      attrValueForm.resetFields();
      await qc.invalidateQueries({ queryKey: ["admin", "attribute-values"] });
      if (selectedAttrId != null) {
        await qc.invalidateQueries({ queryKey: ["admin", "attributes", selectedAttrId, "values"] });
      }
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi thêm giá trị"),
  });

  const delAttrValueMut = useMutation({
    mutationFn: (id: number) => catalogApi.deleteAttributeValue(id),
    onSuccess: async () => {
      message.success("Đã xoá giá trị");
      await qc.invalidateQueries({ queryKey: ["admin", "attribute-values"] });
      if (selectedAttrId != null) {
        await qc.invalidateQueries({ queryKey: ["admin", "attributes", selectedAttrId, "values"] });
      }
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá giá trị"),
  });

  const onSave = (v: ProductRequest & { isFeaturedCheck?: boolean }) => {
    const body: ProductRequest = {
      categoryId: v.categoryId,
      brandId: v.brandId ?? null,
      sku: v.sku,
      name: v.name,
      slug: v.slug,
      shortDescription: v.shortDescription,
      description: v.description,
      basePrice: typeof v.basePrice === "number" ? String(v.basePrice) : v.basePrice,
      salePrice: v.salePrice == null || v.salePrice === "" ? null : String(v.salePrice),
      thumbnailUrl: v.thumbnailUrl,
      useFileUpload: v.useFileUpload ?? false,
      status: v.status,
      isFeatured: v.isFeaturedCheck ?? false,
    };
    const file = body.useFileUpload ? thumbFile : null;
    if (isNew) createMut.mutate({ body, file });
    else updateMut.mutate({ body, file });
  };

  const quillModules = useMemo(() => ({
    toolbar: {
      container: [
        [{ header: [1, 2, 3, false] }],
        ["bold", "italic", "underline"],
        [{ list: "ordered" }, { list: "bullet" }],
        [{ align: [] }],
        [{ indent: "-1" }, { indent: "+1" }],
        ["link", "image"],
        ["clean"],
      ],
      handlers: {
        image: () => {
          const input = document.createElement("input");
          input.setAttribute("type", "file");
          input.setAttribute("accept", "image/*");
          input.click();
          input.onchange = async () => {
            const file = input.files?.[0];
            if (!file) return;
            const fd = new FormData();
            fd.append("file", file);
            fd.append("folder", "product-descriptions");
            const res = await api.post("/v1/uploads/image", fd);
            const url = (res.data?.data?.url ?? res.data?.url) as string;
            const editor = quillRef.current?.getEditor?.();
            if (!editor || !url) return;
            const range = editor.getSelection(true) ?? { index: editor.getLength(), length: 0 };
            editor.insertEmbed(range.index, "image", url, "user");
            editor.setSelection(range.index + 1, 0);
          };
        },
      },
    },
    blotFormatter: {},
  }), [quillRef]);

  const p = product;
  return (
    <>
      <PageHeader
        title={isNew ? "Add Product" : `Product: ${p?.name ?? slug}`}
        extra={
          <Link to="/catalog/products">
            <Button icon={<ArrowLeftOutlined />} style={{ borderRadius: 999 }}>
              Back to list
            </Button>
          </Link>
        }
      />
      {loadingP && !isNew ? (
        <p>Đang tải…</p>
      ) : (
        <div style={{ maxWidth: 980 }}>
          <Tabs
            defaultActiveKey="detail"
            items={[
              {
                key: "detail",
                label: (
                  <Space>
                    <SettingOutlined />
                    Product detail
                  </Space>
                ),
                children: (
                  <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 12 }}>
                    <Card style={{ borderRadius: 14 }} bodyStyle={{ padding: 16 }}>
                      <Form form={form} layout="vertical" onFinish={onSave} style={{ margin: 0 }}>
                        <Form.Item name="useFileUpload" hidden>
                        <Input />
                      </Form.Item>
                        <Form.Item name="thumbnailUrl" hidden>
                          <Input />
                        </Form.Item>

                      <Space direction="vertical" size={16} style={{ width: "100%" }}>
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          General
                        </Typography.Title>
                        <Space wrap style={{ width: "100%" }} size={16} align="start">
                          <Form.Item name="sku" label="SKU" rules={[{ required: true }]} style={{ flex: "1 1 240px" }}>
                            <Input disabled={!isNew} />
                          </Form.Item>
                          <Form.Item
                            name="name"
                            label="Product name"
                            rules={[{ required: true }]}
                            style={{ flex: "2 1 320px" }}
                          >
                            <Input />
                          </Form.Item>
                          <Form.Item name="slug" label="Slug (optional)" style={{ flex: "1 1 240px" }}>
                            <Input />
                          </Form.Item>
                        </Space>

                        <Divider style={{ margin: "4px 0" }} />
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          Classification
                        </Typography.Title>
                        <Space wrap style={{ width: "100%" }} size={16} align="start">
                          <Form.Item
                            name="categoryId"
                            label="Category"
                            rules={[{ required: true }]}
                            style={{ flex: "1 1 320px" }}
                          >
                            <Select
                              showSearch
                              options={categoryOptions(tree)}
                              optionFilterProp="label"
                              style={{ width: "100%" }}
                            />
                          </Form.Item>
                          <Form.Item name="brandId" label="Brand" style={{ flex: "1 1 320px" }}>
                            <Select
                              allowClear
                              showSearch
                              options={brands.map((b) => ({ value: b.id, label: b.name }))}
                              optionFilterProp="label"
                              style={{ width: "100%" }}
                            />
                          </Form.Item>
                          <Form.Item
                            name="status"
                            label="Status"
                            rules={[{ required: true }]}
                            style={{ flex: "1 1 220px" }}
                          >
                            <Select
                              options={(
                                ["draft", "active", "inactive", "out_of_stock"] as ProductStatus[]
                              ).map((s) => ({ value: s, label: s }))}
                            />
                          </Form.Item>
                        </Space>

                        <Divider style={{ margin: "4px 0" }} />
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          Pricing
                        </Typography.Title>
                        <Space wrap style={{ width: "100%" }} size={16} align="start">
                          <Form.Item
                            name="basePrice"
                            label="Price"
                            rules={[{ required: true }]}
                            style={{ flex: "1 1 220px" }}
                          >
                            <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
                          </Form.Item>
                          <Form.Item name="salePrice" label="Sale price" style={{ flex: "1 1 220px" }}>
                            <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
                          </Form.Item>
                          <Form.Item
                            name="isFeaturedCheck"
                            label="Featured"
                            valuePropName="checked"
                            style={{ flex: "1 1 180px" }}
                          >
                            <Switch />
                          </Form.Item>
                        </Space>

                        <Divider style={{ margin: "4px 0" }} />
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          Description
                        </Typography.Title>
                        <Form.Item name="shortDescription" label="Short description">
                          <Input.TextArea rows={2} />
                        </Form.Item>
                        <Form.Item name="description" label="Description (rich text)">
                          <ReactQuill
                            ref={quillRef}
                            theme="snow"
                            className="productRichEditor"
                            value={richDescW}
                            onChange={(html) => form.setFieldValue("description", html)}
                            modules={quillModules as unknown as Record<string, unknown>}
                          />
                        </Form.Item>

                       

                        <Divider style={{ margin: "4px 0" }} />
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          Media
                        </Typography.Title>
                        <Form.Item label="Thumbnail">
                          <FileOrUrlImageInput
                            value={{
                              mode: useThumbFileUploadW ? "file" : "url",
                              url: useThumbFileUploadW ? "" : thumbnailUrlW,
                              file: thumbFile,
                            }}
                            onChange={(next) => {
                              form.setFieldValue("useFileUpload", next.mode === "file");
                              form.setFieldValue("thumbnailUrl", next.url || undefined);
                              setThumbFile(next.file ?? null);
                            }}
                            urlPlaceholder="https://... hoặc /api/uploads/..."
                          />
                        </Form.Item>

                        <Space>
                          <Button
                            type="primary"
                            icon={<SaveOutlined />}
                            htmlType="submit"
                            loading={createMut.isPending || updateMut.isPending}
                            style={{ borderRadius: 999 }}
                          >
                            {isNew ? "Create product" : "Save changes"}
                          </Button>
                          {!isNew && p && (
                            <PermissionGate roles={["admin"]}>
                              <Popconfirm title="Xoá sản phẩm vĩnh viễn?" onConfirm={() => delMut.mutate()}>
                                <Button danger loading={delMut.isPending}>
                                  Delete
                                </Button>
                              </Popconfirm>
                            </PermissionGate>
                          )}
                        </Space>
                      </Space>
                    </Form>
                    </Card>

                    <Card
                      style={{ borderRadius: 14, height: "fit-content" }}
                      bodyStyle={{ padding: 16 }}
                      title={
                        <Space>
                          <AppstoreOutlined />
                          Preview
                        </Space>
                      }
                    >
                      <div
                        style={{
                          width: "100%",
                          aspectRatio: "1 / 1",
                          borderRadius: 12,
                          border: `1px dashed ${token.colorBorderSecondary}`,
                          overflow: "hidden",
                          background: token.colorFillQuaternary,
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        {thumbnailUrlW ? (
                          <img
                            src={thumbnailUrlW}
                            alt="thumbnail preview"
                            style={{ width: "100%", height: "100%", objectFit: "cover" }}
                            onError={(e) => {
                              (e.currentTarget as HTMLImageElement).src = "";
                            }}
                          />
                        ) : (
                          <Typography.Text type="secondary" style={{ textAlign: "center", padding: 12 }}>
                            Add a thumbnail (URL or upload) to preview here.
                          </Typography.Text>
                        )}
                      </div>

                      <Divider style={{ margin: "14px 0" }} />

                      <Space direction="vertical" style={{ width: "100%" }} size={6}>
                        <Typography.Text strong>{form.getFieldValue("name") || "—"}</Typography.Text>
                        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                          SKU: {form.getFieldValue("sku") || "—"}
                        </Typography.Text>
                        <Typography.Text>
                          <PriceDisplay value={form.getFieldValue("basePrice") || "0"} />
                          {form.getFieldValue("salePrice") ? (
                            <Typography.Text type="secondary" style={{ marginLeft: 8, fontSize: 12 }}>
                              <PriceDisplay value={String(form.getFieldValue("salePrice"))} />
                            </Typography.Text>
                          ) : null}
                        </Typography.Text>
                      </Space>
                    </Card>
                  </div>
                ),
              },
              {
                key: "variants",
                label: (
                  <Space>
                    <TagsOutlined />
                    Variants
                  </Space>
                ),
                children: (
                  <Card style={{ borderRadius: 14 }} bodyStyle={{ padding: 16 }}>
                    {isNew ? (
                      <Empty
                        description="Hãy tạo sản phẩm trước, sau đó bạn có thể thêm biến thể."
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                      />
                    ) : (
                      <>
                        <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 12 }} wrap>
                          <div>
                            <Typography.Title level={5} style={{ margin: 0 }}>
                              Variants
                            </Typography.Title>
                            <Typography.Text type="secondary">
                              Add variants (SKU, price, stock, optional image) for this product.
                            </Typography.Text>
                          </div>
                          <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={() => setVarOpen(true)}
                            style={{ borderRadius: 999 }}
                          >
                            Add variant
                          </Button>
                        </Space>

                        <Table
                          dataSource={p?.variants ?? []}
                          rowKey="id"
                          size="middle"
                          pagination={false}
                          columns={[
                            { title: "SKU", dataIndex: "sku" },
                            { title: "Price", render: (_, v) => <PriceDisplay value={v.price} />, width: 140 },
                            {
                              title: "Sale",
                              render: (_, v) => (v.salePrice ? <PriceDisplay value={v.salePrice} /> : "—"),
                              width: 140,
                            },
                            { title: "Stock", dataIndex: "stockQuantity", width: 110 },
                            {
                              title: "Status",
                              dataIndex: "status",
                              width: 120,
                              render: (s: string) => <Tag>{s}</Tag>,
                            },
                            {
                              title: "",
                              key: "x",
                              width: 140,
                              render: (_, v) => (
                                <Space>
                                  <Button
                                    size="small"
                                    icon={<EditOutlined />}
                                    onClick={() => {
                                      setVariantEditingId(v.id);
                                      setVariantHasImage(Boolean(v.imageUrl));
                                      setVariantFile(null);
                                      varForm.setFieldsValue({
                                        sku: v.sku,
                                        price: v.price,
                                        salePrice: v.salePrice ?? undefined,
                                        stockQuantity: v.stockQuantity,
                                        imageUrl: v.imageUrl ?? undefined,
                                        useFileUpload: false,
                                        status: v.status,
                                        attributeValueIds: v.attributes?.map((a: { id: number }) => a.id) ?? [],
                                      });
                                      setVarOpen(true);
                                    }}
                                  />
                                  <Popconfirm title="Xoá biến thể?" onConfirm={() => delVarMut.mutate(v.id)}>
                                    <Button size="small" danger loading={delVarMut.isPending} icon={<DeleteOutlined />} />
                                  </Popconfirm>
                                </Space>
                              ),
                            },
                          ]}
                        />
                      </>
                    )}
                  </Card>
                ),
              },
              {
                key: "images",
                label: (
                  <Space>
                    <AppstoreOutlined />
                    Images
                  </Space>
                ),
                children: (
                  <Card style={{ borderRadius: 14 }} bodyStyle={{ padding: 16 }}>
                    {isNew || !p ? (
                      <Empty
                        description="Hãy tạo sản phẩm trước, sau đó bạn có thể thêm nhiều ảnh."
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                      />
                    ) : (
                      <>
                        <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 12 }} wrap>
                          <div>
                            <Typography.Title level={5} style={{ margin: 0 }}>
                              Product images
                            </Typography.Title>
                            <Typography.Text type="secondary">
                              Một sản phẩm có thể có nhiều ảnh. Bạn có thể đặt ảnh chính (primary).
                            </Typography.Text>
                          </div>
                          <Button type="primary" icon={<PlusOutlined />} onClick={() => setImagesOpen(true)} style={{ borderRadius: 999 }}>
                            Add images
                          </Button>
                        </Space>

                        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))", gap: 12 }}>
                          {(p.images ?? []).map((img) => (
                            <Card
                              key={img.id}
                              size="small"
                              style={{ borderRadius: 12 }}
                              bodyStyle={{ padding: 10 }}
                              cover={
                                <div style={{ height: 140, overflow: "hidden", borderTopLeftRadius: 12, borderTopRightRadius: 12 }}>
                                  <img
                                    src={img.imageUrl}
                                    alt={img.altText ?? ""}
                                    style={{ width: "100%", height: "100%", objectFit: "cover", display: "block" }}
                                    onError={(e) => {
                                      (e.currentTarget as HTMLImageElement).src = "";
                                    }}
                                  />
                                </div>
                              }
                            >
                              <Space direction="vertical" style={{ width: "100%" }} size={6}>
                                <Space style={{ width: "100%", justifyContent: "space-between" }}>
                                  {img.isPrimary ? (
                                    <Tag color="green" style={{ borderRadius: 999 }}>
                                      Primary
                                    </Tag>
                                  ) : (
                                    <Tag style={{ borderRadius: 999 }}>Image</Tag>
                                  )}
                                  <Button
                                    type="link"
                                    danger
                                    size="small"
                                    loading={delImgMut.isPending}
                                    onClick={() => delImgMut.mutate({ productId: p.id, imageId: img.id })}
                                  >
                                    Delete
                                  </Button>
                                </Space>

                                <Space style={{ width: "100%", justifyContent: "space-between" }}>
                                  <Button
                                    size="small"
                                    disabled={img.isPrimary || primaryImgMut.isPending}
                                    onClick={() => primaryImgMut.mutate({ productId: p.id, imageId: img.id })}
                                  >
                                    Set primary
                                  </Button>
                                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                                    #{img.displayOrder}
                                  </Typography.Text>
                                </Space>
                              </Space>
                            </Card>
                          ))}
                        </div>

                        <Modal
                          open={imagesOpen}
                          title="Add images"
                          onCancel={() => setImagesOpen(false)}
                          footer={null}
                          destroyOnClose
                          width={760}
                        >
                          <Space direction="vertical" style={{ width: "100%" }} size={12}>
                            <Card size="small" style={{ borderRadius: 12 }}>
                              <Typography.Text strong>Upload multiple files</Typography.Text>
                              <div style={{ marginTop: 10 }}>
                                <input
                                  type="file"
                                  multiple
                                  accept="image/*"
                                  onChange={(e) => setNewImages(Array.from(e.target.files ?? []))}
                                />
                              </div>
                              <div style={{ marginTop: 8, fontSize: 12, opacity: 0.75 }}>
                                Đã chọn: {newImages.length} file
                              </div>
                              <Space style={{ marginTop: 10 }}>
                                <Switch checked={newImagePrimary} onChange={setNewImagePrimary} />
                                <Typography.Text>Set first uploaded image as primary</Typography.Text>
                              </Space>
                              <div style={{ marginTop: 12 }}>
                                <Button
                                  type="primary"
                                  loading={addImgMut.isPending}
                                  disabled={newImages.length === 0}
                                  onClick={async () => {
                                    for (let i = 0; i < newImages.length; i++) {
                                      const f = newImages[i];
                                      // eslint-disable-next-line no-await-in-loop
                                      await addImgMut.mutateAsync({
                                        productId: p.id,
                                        body: { useFileUpload: true, isPrimary: newImagePrimary && i === 0 },
                                        file: f,
                                      });
                                    }
                                    setImagesOpen(false);
                                  }}
                                  style={{ borderRadius: 999 }}
                                >
                                  Upload
                                </Button>
                              </div>
                            </Card>

                            <Card size="small" style={{ borderRadius: 12 }}>
                              <Typography.Text strong>Add by URL</Typography.Text>
                              <Space style={{ width: "100%", marginTop: 10 }} wrap>
                                <Input
                                  value={newImageUrl}
                                  onChange={(e) => setNewImageUrl(e.target.value)}
                                  placeholder="https://..."
                                  style={{ flex: "1 1 420px" }}
                                />
                                <Button
                                  type="primary"
                                  loading={addImgMut.isPending}
                                  disabled={!newImageUrl.trim()}
                                  onClick={async () => {
                                    await addImgMut.mutateAsync({
                                      productId: p.id,
                                      body: { imageUrl: newImageUrl.trim(), useFileUpload: false, isPrimary: false },
                                      file: null,
                                    });
                                    setImagesOpen(false);
                                  }}
                                  style={{ borderRadius: 999 }}
                                >
                                  Add
                                </Button>
                              </Space>
                            </Card>
                          </Space>
                        </Modal>
                      </>
                    )}
                  </Card>
                ),
              },
              {
                key: "attributes",
                label: (
                  <Space>
                    <TagsOutlined />
                    Attributes
                  </Space>
                ),
                children: (
                  <Card style={{ borderRadius: 14 }} bodyStyle={{ padding: 16 }}>
                    <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 12 }} wrap>
                      <div>
                        <Typography.Title level={5} style={{ margin: 0 }}>
                          Product attributes
                        </Typography.Title>
                        <Typography.Text type="secondary">
                          Tạo thuộc tính (Color, Size…) và thêm các giá trị cho từng thuộc tính.
                        </Typography.Text>
                      </div>
                      <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        style={{ borderRadius: 999 }}
                        onClick={() => {
                          setAttrEditing(null);
                          attrForm.resetFields();
                          setAttrModalOpen(true);
                        }}
                      >
                        Add attribute
                      </Button>
                    </Space>

                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1.2fr", gap: 12 }}>
                      <Card size="small" style={{ borderRadius: 12 }} title="Attributes">
                        <Table<ProductAttribute>
                          size="small"
                          rowKey="id"
                          dataSource={attrs}
                          pagination={false}
                          rowSelection={undefined}
                          rowClassName={(r) => (r.id === selectedAttrId ? "ant-table-row-selected" : "")}
                          onRow={(r) => ({
                            onClick: () => setSelectedAttrId(r.id),
                          })}
                          columns={[
                            {
                              title: "Name",
                              dataIndex: "name",
                              render: (v: string, r) => (
                                <Space>
                                  <Tag style={{ borderRadius: 999 }}>{r.slug}</Tag>
                                  <Typography.Text strong>{v}</Typography.Text>
                                </Space>
                              ),
                            },
                            {
                              title: "",
                              key: "act",
                              width: 90,
                              render: (_, r) => (
                                <Space>
                                  <Button
                                    size="small"
                                    icon={<EditOutlined />}
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setAttrEditing(r);
                                      attrForm.setFieldsValue({ name: r.name, slug: r.slug });
                                      setAttrModalOpen(true);
                                    }}
                                  />
                                  <Popconfirm
                                    title="Xoá thuộc tính?"
                                    onConfirm={(e) => {
                                      e?.stopPropagation();
                                      delAttrMut.mutate(r.id);
                                    }}
                                  >
                                    <Button
                                      size="small"
                                      danger
                                      icon={<DeleteOutlined />}
                                      loading={delAttrMut.isPending}
                                      onClick={(e) => e.stopPropagation()}
                                    />
                                  </Popconfirm>
                                </Space>
                              ),
                            },
                          ]}
                        />
                      </Card>

                      <Card
                        size="small"
                        style={{ borderRadius: 12 }}
                        title={selectedAttrId ? "Values" : "Values (select an attribute)"}
                        extra={
                          <Button
                            type="primary"
                            size="small"
                            icon={<PlusOutlined />}
                            style={{ borderRadius: 999 }}
                            disabled={!selectedAttrId}
                            onClick={() => setAttrValueModalOpen(true)}
                          >
                            Add value
                          </Button>
                        }
                      >
                        {!selectedAttrId ? (
                          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chọn 1 thuộc tính để xem giá trị." />
                        ) : (
                          <Table<AttributeValueResponse>
                            size="small"
                            rowKey="id"
                            dataSource={attrValuesByAttr}
                            pagination={false}
                            columns={[
                              {
                                title: "Value",
                                dataIndex: "value",
                                render: (v: string, r) => (
                                  <Space>
                                    {r.colorCode ? (
                                      <span
                                        style={{
                                          width: 10,
                                          height: 10,
                                          borderRadius: 999,
                                          background: r.colorCode,
                                          display: "inline-block",
                                          border: "1px solid rgba(0,0,0,0.12)",
                                        }}
                                      />
                                    ) : null}
                                    <Typography.Text strong>{v}</Typography.Text>
                                    {r.colorCode ? (
                                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                                        {r.colorCode}
                                      </Typography.Text>
                                    ) : null}
                                  </Space>
                                ),
                              },
                              {
                                title: "",
                                key: "del",
                                width: 90,
                                render: (_, r) => (
                                  <Popconfirm title="Xoá giá trị?" onConfirm={() => delAttrValueMut.mutate(r.id)}>
                                    <Button
                                      size="small"
                                      danger
                                      icon={<DeleteOutlined />}
                                      loading={delAttrValueMut.isPending}
                                    />
                                  </Popconfirm>
                                ),
                              },
                            ]}
                          />
                        )}
                      </Card>
                    </div>

                    <Modal
                      open={attrModalOpen}
                      title={attrEditing ? "Edit attribute" : "Add attribute"}
                      onCancel={() => {
                        setAttrModalOpen(false);
                        setAttrEditing(null);
                      }}
                      footer={null}
                      destroyOnClose
                    >
                      <Form<ProductAttributeRequest>
                        form={attrForm}
                        layout="vertical"
                        onFinish={(v) => {
                          if (attrEditing) updateAttrMut.mutate({ id: attrEditing.id, body: v });
                          else createAttrMut.mutate({ body: v });
                        }}
                      >
                        <Form.Item name="name" label="Name" rules={[{ required: true }]}>
                          <Input placeholder="Color" />
                        </Form.Item>
                        <Form.Item name="slug" label="Slug" rules={[{ required: true }]}>
                          <Input placeholder="color" />
                        </Form.Item>
                        <Space style={{ width: "100%", justifyContent: "flex-end" }}>
                          <Button onClick={() => setAttrModalOpen(false)}>Cancel</Button>
                          <Button
                            type="primary"
                            htmlType="submit"
                            loading={createAttrMut.isPending || updateAttrMut.isPending}
                            style={{ borderRadius: 999 }}
                          >
                            Save
                          </Button>
                        </Space>
                      </Form>
                    </Modal>

                    <Modal
                      open={attrValueModalOpen}
                      title="Add value"
                      onCancel={() => setAttrValueModalOpen(false)}
                      footer={null}
                      destroyOnClose
                    >
                      <Form<Omit<ProductAttributeValueRequest, "attributeId">>
                        form={attrValueForm}
                        layout="vertical"
                        onFinish={(v) => {
                          if (!selectedAttrId) return;
                          createAttrValueMut.mutate({ attributeId: selectedAttrId, body: v });
                        }}
                      >
                        <Form.Item name="value" label="Value" rules={[{ required: true }]}>
                          <Input placeholder="Red / M / 256GB" />
                        </Form.Item>
                        <Form.Item name="colorCode" label="Color code (optional)">
                          <Input placeholder="#FF0000" />
                        </Form.Item>
                        <Form.Item name="displayOrder" label="Display order (optional)">
                          <InputNumber min={0} style={{ width: "100%" }} />
                        </Form.Item>
                        <Space style={{ width: "100%", justifyContent: "flex-end" }}>
                          <Button onClick={() => setAttrValueModalOpen(false)}>Cancel</Button>
                          <Button type="primary" htmlType="submit" loading={createAttrValueMut.isPending} style={{ borderRadius: 999 }}>
                            Add
                          </Button>
                        </Space>
                      </Form>
                    </Modal>
                  </Card>
                ),
              },
            ]}
          />
        </div>
      )}

      <Modal
        open={varOpen}
        title={variantEditingId ? "Edit Variant" : "Add Product Variants"}
        onCancel={() => setVarOpen(false)}
        footer={null}
        destroyOnClose
        width={920}
      >
        <Space direction="vertical" size={16} style={{ width: "100%" }}>
          <Card size="small" style={{ borderRadius: 12 }}>
            <Typography.Text type="secondary">Product detail</Typography.Text>
            <Typography.Title level={5} style={{ margin: "6px 0 10px" }}>
              {p?.name ?? "—"}
            </Typography.Title>
            <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3, lg: 3 }}>
              <Descriptions.Item label="Category">
                {tree.find((c) => c.id === p?.categoryId)?.name ?? p?.categoryId ?? "—"}
              </Descriptions.Item>
              <Descriptions.Item label="Brand">
                {brands.find((b) => b.id === p?.brandId)?.name ?? "—"}
              </Descriptions.Item>
              <Descriptions.Item label="Status">{p?.status ?? "—"}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card size="small" style={{ borderRadius: 12 }} title="Variant 1" extra={null}>
            <Form<VariantRequest>
              form={varForm}
              layout="vertical"
              onFinish={(b) => {
                const body: VariantRequest = {
                  ...b,
                  price: String(b.price),
                  salePrice: b.salePrice == null || b.salePrice === "" ? null : String(b.salePrice),
                  useFileUpload: b.useFileUpload ?? false,
                  imageUrl: variantHasImage ? b.imageUrl : undefined,
                };
                const file = body.useFileUpload && variantHasImage ? variantFile : null;
                if (variantEditingId) updateVarMut.mutate({ variantId: variantEditingId, body, file });
                else addVarMut.mutate({ body, file });
              }}
            >
              <Form.Item name="useFileUpload" hidden>
                <Input />
              </Form.Item>
              <Form.Item name="imageUrl" hidden>
                <Input />
              </Form.Item>

              <Space wrap style={{ width: "100%" }} size={16} align="start">
                <Form.Item name="sku" label="SKU" rules={[{ required: true }]} style={{ flex: "1 1 240px" }}>
                  <Input />
                </Form.Item>
                <Form.Item name="price" label="Price" rules={[{ required: true }]} style={{ flex: "1 1 220px" }}>
                  <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
                </Form.Item>
                <Form.Item name="salePrice" label="Sale price" style={{ flex: "1 1 220px" }}>
                  <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
                </Form.Item>
                <Form.Item
                  name="stockQuantity"
                  label="Stock"
                  rules={[{ required: true }]}
                  style={{ flex: "1 1 180px" }}
                >
                  <InputNumber className="w-full" min={0} style={{ width: "100%" }} />
                </Form.Item>
              </Space>

              <Form.Item name="attributeValueIds" label="Attributes (values)">
                <Select
                  mode="multiple"
                  allowClear
                  optionFilterProp="label"
                  showSearch
                  placeholder="Chọn các giá trị thuộc tính (VD: Color: Red, Size: M)"
                  options={attrValues.map((v: AttributeValueResponse) => ({
                    value: v.id,
                    label: `${v.attributeName}: ${v.value}`,
                  }))}
                />
              </Form.Item>

              <Space style={{ marginBottom: 10 }} align="center">
                <Switch
                  checked={variantHasImage}
                  onChange={(v) => {
                    setVariantHasImage(v);
                    if (!v) {
                      varForm.setFieldValue("useFileUpload", false);
                      varForm.setFieldValue("imageUrl", undefined);
                      setVariantFile(null);
                    }
                  }}
                />
                <Typography.Text>Use image for this variant</Typography.Text>
              </Space>

              {variantHasImage && (
                <Form.Item label="Variant image">
                  <FileOrUrlImageInput
                    value={{
                      mode: useVarFileUploadW ? "file" : "url",
                      url: useVarFileUploadW ? "" : varImageUrlW,
                      file: variantFile,
                    }}
                    onChange={(next) => {
                      varForm.setFieldValue("useFileUpload", next.mode === "file");
                      varForm.setFieldValue("imageUrl", next.url || undefined);
                      setVariantFile(next.file ?? null);
                    }}
                    urlPlaceholder="https://... hoặc /api/uploads/..."
                  />
                </Form.Item>
              )}

              <Form.Item name="weightGram" label="Weight (g)">
                <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="0.01" />
              </Form.Item>

              <Space style={{ width: "100%", justifyContent: "flex-end" }}>
                <Button onClick={() => setVarOpen(false)}>Cancel</Button>
                <Button type="primary" htmlType="submit" loading={addVarMut.isPending || updateVarMut.isPending}>
                  {variantEditingId ? "Save changes" : "Save Variant"}
                </Button>
              </Space>
            </Form>
          </Card>
        </Space>
      </Modal>
    </>
  );
}
