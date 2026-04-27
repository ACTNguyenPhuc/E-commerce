import { Button, Input, Segmented, Space, Upload } from "antd";
import type { UploadFile } from "antd/es/upload/interface";
import { useEffect, useMemo, useState } from "react";

type Mode = "url" | "file";

export type FileOrUrlImageValue = {
  mode: Mode;
  url?: string;
  file?: File | null;
};

export function FileOrUrlImageInput(props: {
  value?: FileOrUrlImageValue;
  onChange?: (v: FileOrUrlImageValue) => void;
  urlPlaceholder?: string;
  disabled?: boolean;
}) {
  const { value, onChange, disabled } = props;
  const mode = value?.mode ?? "url";
  const url = value?.url ?? "";
  const file = value?.file ?? null;

  const [objectUrl, setObjectUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!file) {
      setObjectUrl(null);
      return;
    }
    const u = URL.createObjectURL(file);
    setObjectUrl(u);
    return () => URL.revokeObjectURL(u);
  }, [file]);

  const previewSrc = useMemo(() => {
    if (mode === "file") return objectUrl ?? "";
    return url?.trim() ?? "";
  }, [mode, objectUrl, url]);

  const uploadFileList: UploadFile[] = useMemo(() => {
    if (!file) return [];
    return [
      {
        uid: "selected",
        name: file.name,
        status: "done",
        size: file.size,
        type: file.type,
      },
    ];
  }, [file]);

  const emit = (patch: Partial<FileOrUrlImageValue>) => {
    onChange?.({
      mode,
      url,
      file,
      ...patch,
    });
  };

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="middle">
      <Segmented
        value={mode}
        options={[
          { label: "URL", value: "url" },
          { label: "Tải file", value: "file" },
        ]}
        onChange={(v) => {
          const nextMode = v as Mode;
          if (nextMode === "url") emit({ mode: "url", file: null });
          else emit({ mode: "file" });
        }}
        disabled={disabled}
      />

      {mode === "url" ? (
        <Input
          value={url}
          onChange={(e) => emit({ url: e.target.value })}
          placeholder={props.urlPlaceholder ?? "https://..."}
          disabled={disabled}
        />
      ) : (
        <Upload.Dragger
          accept="image/*"
          multiple={false}
          fileList={uploadFileList}
          beforeUpload={(f) => {
            emit({ file: f });
            return false;
          }}
          onRemove={() => {
            emit({ file: null });
          }}
          disabled={disabled}
        >
          <p style={{ margin: 0, fontWeight: 600 }}>Kéo thả ảnh vào đây</p>
          <p style={{ margin: 0, opacity: 0.75 }}>hoặc bấm để chọn file</p>
        </Upload.Dragger>
      )}

      <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
        <div
          style={{
            width: 160,
            height: 160,
            border: "1px solid rgba(0,0,0,0.15)",
            borderRadius: 8,
            overflow: "hidden",
            background: "rgba(0,0,0,0.02)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          {previewSrc ? (
            // eslint-disable-next-line @typescript-eslint/no-misused-promises
            <img
              src={previewSrc}
              alt="preview"
              style={{ width: "100%", height: "100%", objectFit: "cover" }}
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = "";
              }}
            />
          ) : (
            <span style={{ opacity: 0.6, fontSize: 12 }}>Chưa có ảnh</span>
          )}
        </div>

        <Space direction="vertical" size="small" style={{ flex: 1 }}>
          <div style={{ fontSize: 12, opacity: 0.75 }}>
            Preview sẽ hiển thị URL (mode URL) hoặc ảnh bạn vừa chọn (mode file).
          </div>
          <Button
            onClick={() => emit({ url: "", file: null })}
            disabled={disabled || (!url && !file)}
          >
            Xoá
          </Button>
        </Space>
      </div>
    </Space>
  );
}

