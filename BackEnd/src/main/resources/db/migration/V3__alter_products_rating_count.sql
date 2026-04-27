-- Fix schema mismatch between Flyway V1 and JPA mapping:
-- JPA maps Product.ratingCount as Long (BIGINT) but V1 created INT UNSIGNED.
-- Keep Java model as-is and widen DB column.

ALTER TABLE products
  MODIFY COLUMN rating_count BIGINT UNSIGNED NOT NULL DEFAULT 0;

