-- Align DB column type with JPA Integer mapping

ALTER TABLE reviews
  MODIFY COLUMN rating INT UNSIGNED NOT NULL;

