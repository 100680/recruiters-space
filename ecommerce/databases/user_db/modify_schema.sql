-- user_db/modify_schema.sql
-- Example safe modifications for production

-- 1) Add a nullable timezone column; backfill in batches then optionally set NOT NULL.
ALTER TABLE "user".users ADD COLUMN IF NOT EXISTS timezone TEXT;

-- 2) Add index on phone (if used heavily)
CREATE INDEX IF NOT EXISTS ix_users_phone ON "user".users (phone);

-- 3) Soft-delete bulk example (do NOT use DELETE)
-- UPDATE "user".users SET is_deleted = true, deleted_at = NOW() WHERE <condition>;
