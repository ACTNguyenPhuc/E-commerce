import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Result
      status="404"
      title="Không tìm thấy trang"
      subTitle="Đường dẫn không tồn tại trên ứng dụng quản trị."
      extra={
        <Button type="primary" onClick={() => void navigate("/dashboard", { replace: true })}>
          Về tổng quan
        </Button>
      }
    />
  );
}
