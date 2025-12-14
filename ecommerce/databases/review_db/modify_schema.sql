-- review_db/modify_schema.sql
ALTER TABLE review.reviews ADD COLUMN IF NOT EXISTS moderation_status VARCHAR(20) DEFAULT 'pending';
CREATE INDEX IF NOT EXISTS ix_reviews_moderation_status ON review.reviews (moderation_status);
