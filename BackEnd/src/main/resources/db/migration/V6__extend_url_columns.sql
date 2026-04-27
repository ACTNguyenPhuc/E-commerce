-- =====================================================================
-- V6__extend_url_columns.sql
-- Increase URL column lengths to avoid truncation (e.g., long public URLs)
-- =====================================================================

ALTER TABLE users
  MODIFY avatar_url VARCHAR(2048) NULL;

ALTER TABLE categories
  MODIFY image_url VARCHAR(2048) NULL;

ALTER TABLE brands
  MODIFY logo_url VARCHAR(2048) NULL;

ALTER TABLE products
  MODIFY thumbnail_url VARCHAR(2048) NULL;

ALTER TABLE product_images
  MODIFY image_url VARCHAR(2048) NOT NULL;

ALTER TABLE product_variants
  MODIFY image_url VARCHAR(2048) NULL;

