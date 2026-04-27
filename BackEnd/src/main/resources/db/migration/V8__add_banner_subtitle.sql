-- =====================================================================
-- V8__add_banner_subtitle.sql
-- Add subtitle field for banners
-- =====================================================================
-- Banners (homepage / marketing)
CREATE TABLE banners (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title           VARCHAR(200)    DEFAULT NULL,
  image_url       VARCHAR(2048)   NOT NULL,
  use_file_upload TINYINT(1)      NOT NULL DEFAULT 0,
  link_url        VARCHAR(2048)   DEFAULT NULL,
  display_order   INT             NOT NULL DEFAULT 0,
  status          ENUM('active','inactive') NOT NULL DEFAULT 'active',
  start_at        DATETIME        DEFAULT NULL,
  end_at          DATETIME        DEFAULT NULL,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_banner_status (status),
  KEY idx_banner_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE banners
  ADD COLUMN subtitle VARCHAR(255) NULL AFTER title;

