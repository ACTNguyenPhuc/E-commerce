export type CommonStatus = "active" | "inactive";
export type ProductStatus = "draft" | "active" | "inactive" | "out_of_stock";

export interface Category {
  id: number;
  parentId: number | null;
  name: string;
  slug: string;
  description: string | null;
  imageUrl: string | null;
  useFileUpload?: boolean;
  displayOrder: number;
  status: CommonStatus;
  children?: Category[];
}

export interface Brand {
  id: number;
  name: string;
  slug: string;
  logoUrl: string | null;
  useFileUpload?: boolean;
  description: string | null;
  status: CommonStatus;
}

export interface Product {
  id: number;
  categoryId: number;
  brandId: number | null;
  sku: string;
  name: string;
  slug: string;
  shortDescription: string | null;
  basePrice: string;
  salePrice: string | null;
  thumbnailUrl: string | null;
  useFileUpload?: boolean;
  status: ProductStatus;
  isFeatured: boolean;
  soldCount: number;
  ratingAvg: string;
  ratingCount: number;
}

export interface ProductDetail extends Product {
  description: string | null;
  viewCount: number;
  images: ProductImage[];
  variants: ProductVariantRow[];
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  useFileUpload?: boolean;
  altText: string | null;
  displayOrder: number;
  isPrimary: boolean;
}

export interface ProductVariantRow {
  id: number;
  productId: number;
  sku: string;
  price: string;
  salePrice: string | null;
  stockQuantity: number;
  imageUrl: string | null;
  useFileUpload?: boolean;
  status: CommonStatus;
  attributes: { id: number; attributeName: string; value: string; colorCode: string | null }[];
}

export interface AttributeValueResponse {
  id: number;
  attributeId: number;
  attributeName: string;
  value: string;
  colorCode: string | null;
}

export interface ProductAttribute {
  id: number;
  name: string;
  slug: string;
}

export interface ProductAttributeRequest {
  name: string;
  slug: string;
}

export interface ProductAttributeValueRequest {
  attributeId: number;
  value: string;
  colorCode?: string | null;
  displayOrder?: number;
}

export interface ProductRequest {
  categoryId: number;
  brandId: number | null;
  sku: string;
  name: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  basePrice: string;
  salePrice?: string | null;
  thumbnailUrl?: string;
  useFileUpload?: boolean;
  status?: ProductStatus;
  isFeatured?: boolean;
}

export interface CategoryRequest {
  name: string;
  parentId?: number | null;
  slug?: string;
  description?: string;
  imageUrl?: string;
  useFileUpload?: boolean;
  displayOrder?: number;
  status?: CommonStatus;
}

export interface BrandRequest {
  name: string;
  slug?: string;
  logoUrl?: string;
  useFileUpload?: boolean;
  description?: string;
  status?: CommonStatus;
}

export interface VariantRequest {
  sku: string;
  price: string;
  salePrice?: string | null;
  stockQuantity: number;
  imageUrl?: string;
  useFileUpload?: boolean;
  weightGram?: string | null;
  status?: CommonStatus;
  attributeValueIds?: number[];
}
