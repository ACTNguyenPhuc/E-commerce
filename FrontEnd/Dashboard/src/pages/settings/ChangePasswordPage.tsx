import { App, Button, Form, Input } from "antd";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";
import { PageHeader } from "@/components/common/PageHeader";

type FormVals = { oldPassword: string; newPassword: string; confirm: string };

export function ChangePasswordPage() {
  const { message } = App.useApp();
  const changePassword = useAuthStore((s) => s.changePassword);
  const nav = useNavigate();
  const [form] = Form.useForm<FormVals>();

  return (
    <>
      <PageHeader title="Đổi mật khẩu" description="Bắt buộc sau lần đăng nhập đầu (khuyến nghị bảo mật)." />
      <Form<FormVals>
        form={form}
        layout="vertical"
        onFinish={async (v) => {
          if (v.newPassword !== v.confirm) {
            message.error("Mật khẩu mới nhập lại chưa khớp");
            return;
          }
          try {
            await changePassword({ oldPassword: v.oldPassword, newPassword: v.newPassword });
            message.success("Đã đổi mật khẩu");
            void nav("/settings/profile", { replace: true });
          } catch (e) {
            message.error(e instanceof Error ? e.message : "Đổi mật khẩu thất bại");
          }
        }}
        style={{ maxWidth: 400 }}
      >
        <Form.Item name="oldPassword" label="Mật khẩu hiện tại" rules={[{ required: true }]}>
          <Input.Password autoComplete="current-password" />
        </Form.Item>
        <Form.Item
          name="newPassword"
          label="Mật khẩu mới"
          rules={[{ required: true }, { min: 8, message: "Tối thiểu 8 ký tự" }]}
        >
          <Input.Password autoComplete="new-password" />
        </Form.Item>
        <Form.Item name="confirm" label="Nhập lại" rules={[{ required: true }]}>
          <Input.Password autoComplete="new-password" />
        </Form.Item>
        <Button type="primary" htmlType="submit">
          Cập nhật
        </Button>
      </Form>
    </>
  );
}
