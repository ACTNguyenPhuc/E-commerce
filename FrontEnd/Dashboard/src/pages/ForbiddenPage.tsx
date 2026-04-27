import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

export function ForbiddenPage() {
  const navigate = useNavigate();
  return (
    <Result
      status="403"
      title="Không có quyền"
      subTitle="Tài khoản không thể truy cập tài nguyên này."
      extra={
        <Button type="primary" onClick={() => void navigate("/dashboard", { replace: true })}>
          Về tổng quan
        </Button>
      }
    />
  );
}
