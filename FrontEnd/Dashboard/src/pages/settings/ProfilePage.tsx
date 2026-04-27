import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Avatar, Button, Card, DatePicker, Divider, Form, Input, Modal, Select, Space, Typography } from "antd";
import { EditOutlined, MailOutlined, PhoneOutlined, UserOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import { userApi } from "@/api/user.api";
import { PageHeader } from "@/components/common/PageHeader";
import { useAuthStore } from "@/stores/authStore";
import type { MeUpdate } from "@/types/user";
import { FileOrUrlImageInput } from "@/components/common/FileOrUrlImageInput";

type FormVals = MeUpdate & { dateOfBirthD?: dayjs.Dayjs };

export function ProfilePage() {
  const { message } = App.useApp();
  const setUser = useAuthStore((s) => s.setUser);
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ["me"], queryFn: () => userApi.me() });
  const [form] = Form.useForm<FormVals>();
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarModalOpen, setAvatarModalOpen] = useState(false);
  const useFileUploadW = Form.useWatch("useFileUpload", form) ?? false;
  const avatarUrlW = Form.useWatch("avatarUrl", form) ?? "";

  useEffect(() => {
    if (!data) return;
    form.setFieldsValue({
      fullName: data.fullName,
      phone: data.phone ?? undefined,
      avatarUrl: data.avatarUrl ?? undefined,
      gender: data.gender ?? undefined,
      dateOfBirthD: data.dateOfBirth ? dayjs(data.dateOfBirth) : undefined,
      useFileUpload: false,
    });
    setAvatarFile(null);
  }, [data, form]);

  const m = useMutation({
    mutationFn: (p: { body: MeUpdate; file?: File | null }) => userApi.updateMe(p.body, p.file),
    onSuccess: (u) => {
      setUser(u);
      message.success("Đã cập nhật hồ sơ");
      void qc.invalidateQueries({ queryKey: ["me"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi cập nhật"),
  });

  if (isLoading && !data) {
    return <p>Đang tải…</p>;
  }
  if (!data) {
    return <p>Không tải được hồ sơ.</p>;
  }

  const headerAvatarSrc = useFileUploadW ? "" : avatarUrlW?.trim();

  return (
    <>
      <PageHeader
        title="Thông tin người dùng"
      />
      <div style={{ maxWidth: 760 }}>
        <Card
          style={{ borderRadius: 12 }}
          bodyStyle={{ padding: 20 }}
          title={null}
          extra={null}
        >
          <Space align="start" style={{ width: "100%", justifyContent: "space-between" }}>
            <Space align="start" size={14}>
              <div style={{ position: "relative" }}>
                <Avatar
                  size={56}
                  src={headerAvatarSrc || data.avatarUrl || undefined}
                  icon={<UserOutlined />}
                />
                <Button
                  type="text"
                  size="small"
                  aria-label="Chỉnh ảnh đại diện"
                  onClick={() => setAvatarModalOpen(true)}
                  style={{
                    position: "absolute",
                    right: -6,
                    bottom: -6,
                    width: 26,
                    height: 26,
                    padding: 0,
                    borderRadius: 999,
                    background: "#fff",
                    border: "1px solid rgba(0,0,0,0.08)",
                    boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                  icon={<EditOutlined style={{ fontSize: 12, opacity: 0.75 }} />}
                />
              </div>

              <div style={{ lineHeight: 1.1 }}>
                <Typography.Text strong style={{ fontSize: 16, display: "block" }}>
                  {data.fullName}
                </Typography.Text>
                <Typography.Text type="secondary" style={{ display: "block" }}>
                  {data.email}
                </Typography.Text>
              </div>
            </Space>

            {/* Placeholder area matching the sample’s top-right close icon spacing */}
            <div style={{ width: 24 }} />
          </Space>

          <Divider style={{ margin: "16px 0" }} />

          <Modal
            title="Chỉnh ảnh đại diện"
            open={avatarModalOpen}
            onCancel={() => setAvatarModalOpen(false)}
            okText="Xong"
            onOk={() => setAvatarModalOpen(false)}
            destroyOnClose
          >
            <FileOrUrlImageInput
              value={{
                mode: useFileUploadW ? "file" : "url",
                url: useFileUploadW === false ? avatarUrlW : "",
                file: avatarFile,
              }}
              onChange={(next) => {
                form.setFieldValue("useFileUpload", next.mode === "file");
                form.setFieldValue("avatarUrl", next.url || undefined);
                setAvatarFile(next.file ?? null);
              }}
              urlPlaceholder="https://... hoặc /api/uploads/..."
            />
           
          </Modal>

          <Form<FormVals>
            form={form}
            layout="vertical"
            initialValues={{ useFileUpload: false }}
            onFinish={(v) => {
              const useFileUpload = Boolean(v.useFileUpload) && !!avatarFile;
              const body: MeUpdate = {
                fullName: v.fullName,
                phone: v.phone,
                avatarUrl: useFileUpload ? undefined : v.avatarUrl,
                useFileUpload,
                gender: v.gender,
                dateOfBirth: v.dateOfBirthD ? v.dateOfBirthD.format("YYYY-MM-DD") : null,
              };
              m.mutate({ body, file: useFileUpload ? avatarFile : null });
            }}
            style={{ margin: 0 }}
          >
            {/* Register hidden fields so Form tracks them reliably */}
            <Form.Item name="useFileUpload" hidden>
              <Input />
            </Form.Item>
            <Form.Item name="avatarUrl" hidden>
              <Input />
            </Form.Item>

            <Space direction="vertical" style={{ width: "100%" }} size={0}>
              <div style={{ padding: "10px 2px" }}>
                <Typography.Text type="secondary" style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <UserOutlined />
                  Name
                </Typography.Text>
                <Form.Item
                  name="fullName"
                  style={{ margin: 0, marginTop: 6 }}
                  rules={[{ required: true, message: "Nhập họ tên" }]}
                >
                  <Input placeholder="Nhập họ tên" />
                </Form.Item>
              </div>

              <Divider style={{ margin: "8px 0" }} />

              <div style={{ padding: "10px 2px" }}>
                <Typography.Text type="secondary" style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <MailOutlined />
                  Email account
                </Typography.Text>
                <div style={{ marginTop: 8 }}>
                  <Input value={data.email} disabled />
                </div>
              </div>

              <Divider style={{ margin: "8px 0" }} />

              <div style={{ padding: "10px 2px" }}>
                <Typography.Text type="secondary" style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <PhoneOutlined />
                  Mobile number
                </Typography.Text>
                <Form.Item name="phone" style={{ margin: 0, marginTop: 6 }}>
                  <Input placeholder="0123456789" maxLength={10} />
                </Form.Item>
              </div>

              <Divider style={{ margin: "8px 0" }} />

              <div style={{ padding: "10px 2px" }}>
                <Typography.Text type="secondary" style={{ display: "block" }}>
                  Thông tin cá nhân
                </Typography.Text>
                <Space size={12} style={{ marginTop: 10, width: "100%" }} wrap>
                  <Form.Item name="gender" label="Giới tính" style={{ marginBottom: 0, minWidth: 220 }}>
                    <Select
                      allowClear
                      options={[
                        { value: "male", label: "Nam" },
                        { value: "female", label: "Nữ" },
                        { value: "other", label: "Khác" },
                      ]}
                    />
                  </Form.Item>
                  <Form.Item name="dateOfBirthD" label="Ngày sinh" style={{ marginBottom: 0, minWidth: 220 }}>
                    <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
                  </Form.Item>
                </Space>
              </div>

              <Divider style={{ margin: "8px 0" }} />

              <div style={{ padding: "10px 2px" }}>
                <Typography.Text type="secondary" style={{ display: "block" }}>
                  Ảnh đại diện
                </Typography.Text>
                <div style={{ marginTop: 10 }}>
                  <FileOrUrlImageInput
                    value={{
                      mode: useFileUploadW ? "file" : "url",
                      url: useFileUploadW ? "" : avatarUrlW,
                      file: avatarFile,
                    }}
                    onChange={(next) => {
                      form.setFieldValue("useFileUpload", next.mode === "file");
                      form.setFieldValue("avatarUrl", next.url || undefined);
                      setAvatarFile(next.file ?? null);
                    }}
                    urlPlaceholder="Dán link ảnh vào đây"
                  />
                </div>
              </div>

              <div style={{ paddingTop: 12 }}>
                <Button type="primary" htmlType="submit" loading={m.isPending} style={{ minWidth: 140 }}>
                  Save Change
                </Button>
              </div>
            </Space>
          </Form>
        </Card>
      </div>
    </>
  );
}
