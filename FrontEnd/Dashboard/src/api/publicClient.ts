import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL || "/api";

export const publicApi = axios.create({
  baseURL,
  timeout: 30_000,
  // Không set Content-Type mặc định để request multipart/form-data (FormData) tự hoạt động đúng.
});
