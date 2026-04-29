-- Allow district to be nullable to support Vietnam administrative mergers (v2 provinces API).
ALTER TABLE user_addresses
    MODIFY COLUMN district VARCHAR(100) NULL;

