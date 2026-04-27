export function buildMultipartFormData<T extends object>(
  data: T,
  file?: File | null
): FormData {
  console.log("data", data);
  const fd = new FormData();
  fd.append("data", new Blob([JSON.stringify(data)], { type: "application/json" }));
  if (file) {
    fd.append("file", file);
  }
  return fd;
}

