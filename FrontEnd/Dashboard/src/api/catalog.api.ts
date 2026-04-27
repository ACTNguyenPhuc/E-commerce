import { api, extractData } from "@/api/client";
import type { ApiResponse, PageResponse } from "@/types/api";
import type {
  Brand,
  BrandRequest,
  Category,
  CategoryRequest,
  AttributeValueResponse,
  ProductAttribute,
  ProductAttributeRequest,
  ProductAttributeValueRequest,
  Product,
  ProductDetail,
  ProductImage,
  ProductRequest,
  VariantRequest,
} from "@/types/catalog";
import { buildMultipartFormData } from "@/utils/multipart";

export const catalogApi = {
  categories: () =>
    api.get<ApiResponse<Category[]>>("/v1/categories").then(extractData),
  createCategory: (body: CategoryRequest, file?: File | null) =>
    api
      .post<ApiResponse<Category>>(
        "/v1/admin/categories",
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  updateCategory: (id: number, body: CategoryRequest, file?: File | null) =>
    api
      .put<ApiResponse<Category>>(
        `/v1/admin/categories/${id}`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  deleteCategory: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/categories/${id}`).then(extractData),

  brands: () => api.get<ApiResponse<Brand[]>>("/v1/brands").then(extractData),
  createBrand: (body: BrandRequest, file?: File | null) =>
    api
      .post<ApiResponse<Brand>>(
        "/v1/admin/brands",
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  updateBrand: (id: number, body: BrandRequest, file?: File | null) =>
    api
      .put<ApiResponse<Brand>>(
        `/v1/admin/brands/${id}`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  deleteBrand: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/brands/${id}`).then(extractData),

  products: (params: Record<string, string | number | undefined>) =>
    api
      .get<ApiResponse<PageResponse<Product>>>("/v1/products", { params })
      .then(extractData),
  adminProducts: (params: Record<string, string | number | undefined>) =>
    api
      .get<ApiResponse<PageResponse<Product>>>("/v1/admin/products", { params })
      .then(extractData),
  /** Chi tiết theo slug (API public; cần auth để gọi từ dashboard) */
  productBySlug: (slug: string) =>
    api.get<ApiResponse<ProductDetail>>(`/v1/products/${slug}`).then(extractData),
  createProduct: (body: ProductRequest, file?: File | null) =>
    api
      .post<ApiResponse<ProductDetail>>(
        "/v1/admin/products",
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  updateProduct: (id: number, body: ProductRequest, file?: File | null) =>
    api
      .put<ApiResponse<ProductDetail>>(
        `/v1/admin/products/${id}`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  deleteProduct: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/products/${id}`).then(extractData),
  addVariant: (productId: number, body: VariantRequest, file?: File | null) =>
    api
      .post<ApiResponse<unknown>>(
        `/v1/admin/products/${productId}/variants`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  updateVariant: (productId: number, variantId: number, body: VariantRequest, file?: File | null) =>
    api
      .put<ApiResponse<unknown>>(
        `/v1/admin/products/${productId}/variants/${variantId}`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  deleteVariant: (productId: number, variantId: number) =>
    api
      .delete<ApiResponse<null>>(
        `/v1/admin/products/${productId}/variants/${variantId}`
      )
      .then(extractData),

  productImages: (productId: number) =>
    api
      .get<ApiResponse<ProductImage[]>>(`/v1/admin/products/${productId}/images`)
      .then(extractData),
  addProductImage: (productId: number, body: Partial<ProductImage> & { imageUrl?: string; useFileUpload?: boolean; altText?: string; displayOrder?: number; isPrimary?: boolean }, file?: File | null) =>
    api
      .post<ApiResponse<ProductImage>>(
        `/v1/admin/products/${productId}/images`,
        buildMultipartFormData(body, file)
      )
      .then(extractData),
  deleteProductImage: (productId: number, imageId: number) =>
    api
      .delete<ApiResponse<null>>(`/v1/admin/products/${productId}/images/${imageId}`)
      .then(extractData),
  setPrimaryProductImage: (productId: number, imageId: number) =>
    api
      .put<ApiResponse<ProductImage>>(`/v1/admin/products/${productId}/images/${imageId}/primary`, null)
      .then(extractData),

  // Attributes (admin)
  attributes: () =>
    api.get<ApiResponse<ProductAttribute[]>>("/v1/admin/attributes").then(extractData),
  createAttribute: (body: ProductAttributeRequest) =>
    api.post<ApiResponse<ProductAttribute>>("/v1/admin/attributes", body).then(extractData),
  updateAttribute: (id: number, body: ProductAttributeRequest) =>
    api.put<ApiResponse<ProductAttribute>>(`/v1/admin/attributes/${id}`, body).then(extractData),
  deleteAttribute: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/attributes/${id}`).then(extractData),
  attributeValues: () =>
    api.get<ApiResponse<AttributeValueResponse[]>>("/v1/admin/attribute-values").then(extractData),
  attributeValuesByAttribute: (attributeId: number) =>
    api.get<ApiResponse<AttributeValueResponse[]>>(`/v1/admin/attributes/${attributeId}/values`).then(extractData),
  createAttributeValue: (body: ProductAttributeValueRequest) =>
    api.post<ApiResponse<AttributeValueResponse>>("/v1/admin/attribute-values", body).then(extractData),
  deleteAttributeValue: (id: number) =>
    api.delete<ApiResponse<null>>(`/v1/admin/attribute-values/${id}`).then(extractData),
};
