-- =====================================================================
-- V7__banners_and_use_file_upload_columns.sql
-- Add use_file_upload flags for URL-backed entities + create banners table
-- =====================================================================

-- Flags to help client distinguish uploaded file vs external URL
ALTER TABLE users
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER avatar_url;

ALTER TABLE categories
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER image_url;

ALTER TABLE brands
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER logo_url;

ALTER TABLE products
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER thumbnail_url;

ALTER TABLE product_images
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER image_url;

ALTER TABLE product_variants
  ADD COLUMN use_file_upload TINYINT(1) NOT NULL DEFAULT 0 AFTER image_url;


