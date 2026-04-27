import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  App,
  Button,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
} from "antd";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import { voucherApi } from "@/api/voucher.api";
import { PageHeader } from "@/components/common/PageHeader";
import { PermissionGate } from "@/components/common/PermissionGate";
import { formatDateTime } from "@/utils/format";
import type { Voucher, VoucherRequest, VoucherStatus } from "@/types/voucher";

const df = (s: string) => dayjs(s);

export function VouchersPage() {
  const { message } = App.useApp();
  const qc = useQueryClient();
  const { data: list = [], isLoading } = useQuery({ queryKey: ["vouchers"], queryFn: () => voucherApi.list() });
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Voucher | null>(null);
  const [form] = Form.useForm<VoucherRequest & { startD?: dayjs.Dayjs; endD?: dayjs.Dayjs }>();

  useEffect(() => {
    if (!open) {
      setEditing(null);
      form.resetFields();
    }
  }, [open, form]);

  const showCreate = () => {
    setEditing(null);
    form.setFieldsValue({
      discountType: "percentage",
      status: "active" as VoucherStatus,
      startD: dayjs(),
      endD: dayjs().add(7, "day"),
    });
    setOpen(true);
  };

  const showEdit = (v: Voucher) => {
    setEditing(v);
    form.setFieldsValue({
      code: v.code,
      name: v.name,
      description: v.description ?? undefined,
      discountType: v.discountType,
      discountValue: v.discountValue,
      minOrderAmount: v.minOrderAmount,
      maxDiscountAmount: v.maxDiscountAmount ?? undefined,
      usageLimit: v.usageLimit,
      usageLimitPerUser: v.usageLimitPerUser,
      startD: df(v.startDate),
      endD: df(v.endDate),
      status: v.status,
    } as VoucherRequest & { startD?: dayjs.Dayjs; endD?: dayjs.Dayjs });
    setOpen(true);
  };

  const toReq = (v: VoucherRequest & { startD?: dayjs.Dayjs; endD?: dayjs.Dayjs }): VoucherRequest => ({
    code: v.code,
    name: v.name,
    description: v.description,
    discountType: v.discountType,
    discountValue: String(v.discountValue),
    minOrderAmount: v.minOrderAmount != null ? String(v.minOrderAmount) : "0",
    maxDiscountAmount: v.maxDiscountAmount,
    usageLimit: v.usageLimit,
    usageLimitPerUser: v.usageLimitPerUser,
    startDate: v.startD ? v.startD.format("YYYY-MM-DDTHH:mm:ss") : v.startDate,
    endDate: v.endD ? v.endD.format("YYYY-MM-DDTHH:mm:ss") : v.endDate,
    status: v.status,
  });

  const save = useMutation({
    mutationFn: (body: VoucherRequest) =>
      editing ? voucherApi.update(editing.id, body) : voucherApi.create(body),
    onSuccess: async () => {
      message.success("Đã lưu");
      setOpen(false);
      await qc.invalidateQueries({ queryKey: ["vouchers"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi lưu"),
  });
  const typeMap  = {
    "percentage": "Phần trăm",
    "fixed_amount": "Tiền",
  };
  const del = useMutation({
    mutationFn: (id: number) => voucherApi.remove(id),
    onSuccess: async () => {
      message.success("Đã xoá");
      await qc.invalidateQueries({ queryKey: ["vouchers"] });
    },
    onError: (e: unknown) => message.error(e instanceof Error ? e.message : "Lỗi xoá"),
  });

  return (
    <>
      <PageHeader
        title="Voucher"
        extra={
          <PermissionGate roles={["admin"]}>
            <Button type="primary" onClick={showCreate}>
              Thêm voucher
            </Button>
          </PermissionGate>
        }
      />
      <Table<Voucher>
        loading={isLoading}
        rowKey="id"
        dataSource={list}
        columns={[
          { title: "Mã", dataIndex: "code", width: 120 },
          { title: "Tên", dataIndex: "name" },
          { title: "Loại", dataIndex: "discountType", render: (t: string) => typeMap[t as keyof typeof typeMap] },
          { title: "Giá trị", dataIndex: "discountValue" },
          { title: "Đã dùng", dataIndex: "usedCount", width: 80 },
          {
            title: "Bắt đầu",
            dataIndex: "startDate",
            width: 160,
            render: (d: string) => formatDateTime(d),
          },
          {
            title: "Hết hạn",
            dataIndex: "endDate",
            width: 160,
            render: (d: string) => formatDateTime(d),
          },
          {
            title: "Trạng thái",
            dataIndex: "status",
            width: 100,
            render: (s: VoucherStatus) => <Tag color={s === "active" ? "green" : "default"}>{s}</Tag>,
          },
          {
            title: "",
            key: "a",
            width: 120,
            render: (_, v) => (
              <PermissionGate
                roles={["admin"]}
                fallback={
                  <Tag color="default" style={{ margin: 0 }}>
                    Chỉ xem
                  </Tag>
                }
              >
                <Space>
                  <Button type="link" onClick={() => showEdit(v)}>
                    Sửa
                  </Button>
                  <Popconfirm title="Xoá voucher?" onConfirm={() => del.mutate(v.id)} okText="Xoá" cancelText="Huỷ">
                    <Button type="link" danger loading={del.isPending}>
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
      <Drawer
        open={open}
        title={editing ? "Sửa voucher" : "Tạo voucher"}
        onClose={() => setOpen(false)}
        width={480}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(v) => {
            const r = toReq(v);
            if (!r.startDate || !r.endDate) {
              message.error("Chọn thời gian bắt đầu & kết thúc");
              return;
            }
            save.mutate(r);
          }}
        >
          <Form.Item name="code" label="Mã" rules={[{ required: true }]}>
            <Input disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="discountType" label="Loại" rules={[{ required: true }]}>
            <Select
              options={[
                { value: "percentage", label: "Phần trăm" },
                { value: "fixed_amount", label: "Số tiền" },
              ]}
            />
          </Form.Item>
          <Form.Item name="discountValue" label="Giá trị" rules={[{ required: true }]}>
            <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="0.01" />
          </Form.Item>
          <Form.Item name="minOrderAmount" label="Giá trị đơn tối thiểu (VND)">
            <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
          </Form.Item>
          <Form.Item name="maxDiscountAmount" label="Giảm tối đa (VND)">
            <InputNumber stringMode className="w-full" min={0} style={{ width: "100%" }} step="1" />
          </Form.Item>
          <Form.Item name="usageLimit" label="Giới hạn tổng lượt dùng">
            <InputNumber className="w-full" min={0} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="usageLimitPerUser" label="Mỗi user tối đa">
            <InputNumber className="w-full" min={0} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="startD" label="Bắt đầu" rules={[{ required: true }]}>
            <DatePicker showTime className="w-full" style={{ width: "100%" }} format="DD/MM/YYYY HH:mm" />
          </Form.Item>
          <Form.Item name="endD" label="Kết thúc" rules={[{ required: true }]}>
            <DatePicker showTime className="w-full" style={{ width: "100%" }} format="DD/MM/YYYY HH:mm" />
          </Form.Item>
          <Form.Item name="status" label="Trạng thái" initialValue="active">
            <Select
              options={(
                [
                  "active",
                  "inactive",
                  "expired",
                ] as VoucherStatus[]
              ).map((s) => ({ value: s, label: s }))}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={save.isPending} block>
            Lưu
          </Button>
        </Form>
      </Drawer>
    </>
  );
}
