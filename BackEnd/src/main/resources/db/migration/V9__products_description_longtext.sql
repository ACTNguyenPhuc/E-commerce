-- =====================================================================
-- V9__products_description_longtext.sql
-- Store rich HTML product description safely
-- =====================================================================

ALTER TABLE products
  MODIFY COLUMN description LONGTEXT NULL;

