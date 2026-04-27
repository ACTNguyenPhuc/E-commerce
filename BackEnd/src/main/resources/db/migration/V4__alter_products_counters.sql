-- Keep counters consistent with JPA Long mappings

ALTER TABLE products
  MODIFY COLUMN view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  MODIFY COLUMN sold_count BIGINT UNSIGNED NOT NULL DEFAULT 0;

