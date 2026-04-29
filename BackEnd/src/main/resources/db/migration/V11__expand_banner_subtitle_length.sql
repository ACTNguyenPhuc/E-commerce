-- =====================================================================
-- V11__expand_banner_subtitle_length.sql
-- Allow storing JSON config in banner subtitle (up to 2048 chars)
-- =====================================================================

ALTER TABLE banners
  MODIFY COLUMN subtitle VARCHAR(2048) NULL;

