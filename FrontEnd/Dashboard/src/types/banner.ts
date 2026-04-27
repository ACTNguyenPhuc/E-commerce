export type CommonStatus = "active" | "inactive";

export interface Banner {
  id: number;
  title: string | null;
  subtitle: string | null;
  imageUrl: string;
  useFileUpload: boolean;
  linkUrl: string | null;
  displayOrder: number;
  status: CommonStatus;
  startAt: string | null;
  endAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface BannerRequest {
  title?: string;
  subtitle?: string;
  imageUrl?: string;
  useFileUpload?: boolean;
  linkUrl?: string;
  displayOrder?: number;
  status?: CommonStatus;
  startAt?: string | null;
  endAt?: string | null;
}

