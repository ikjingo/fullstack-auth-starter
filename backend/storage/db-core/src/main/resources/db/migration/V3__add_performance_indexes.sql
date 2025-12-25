-- Flyway Migration V3: Performance Indexes
-- Add additional indexes for frequently queried fields

-- ============================================
-- Extensions (required for some indexes)
-- ============================================

-- pg_trgm extension for trigram-based text search (Korean partial matching)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================
-- User Management Indexes
-- ============================================

-- For filtering users by status (active/inactive)
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- For filtering users by role (admin queries)
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- For checking locked accounts during login
CREATE INDEX IF NOT EXISTS idx_users_lockout_until ON users(lockout_until) WHERE lockout_until IS NOT NULL;

-- For finding social accounts by user
CREATE INDEX IF NOT EXISTS idx_social_accounts_user_id ON social_accounts(user_id);

-- ============================================
-- Token Management Indexes
-- ============================================

-- For finding refresh tokens by user
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- For cleanup of expired tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- For filtering revoked tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(revoked) WHERE revoked = FALSE;

-- For password reset code lookups
CREATE INDEX IF NOT EXISTS idx_password_reset_email ON password_reset_codes(email);

-- For cleanup of expired reset codes
CREATE INDEX IF NOT EXISTS idx_password_reset_expires_at ON password_reset_codes(expires_at);

-- For finding unused/unverified codes
CREATE INDEX IF NOT EXISTS idx_password_reset_status ON password_reset_codes(used, verified);

-- For API token lookups
CREATE INDEX IF NOT EXISTS idx_api_tokens_user_id ON api_tokens(user_id);

-- For token authentication
CREATE INDEX IF NOT EXISTS idx_api_tokens_token ON api_tokens(token);

-- ============================================
-- Nickname Service Indexes
-- ============================================

-- Composite index for common sorting (score + updated_at)
CREATE INDEX IF NOT EXISTS idx_nickname_score_updated ON nicknames(score DESC, updated_at DESC);

-- For sorting by update time
CREATE INDEX IF NOT EXISTS idx_nickname_updated_at ON nicknames(updated_at DESC);

-- Full-text search index for nickname search (Korean support)
-- Note: PostgreSQL requires pg_trgm extension for partial matching
CREATE INDEX IF NOT EXISTS idx_nickname_trgm ON nicknames USING gin (nickname gin_trgm_ops);

-- Composite index for rarity filtering with score sorting
CREATE INDEX IF NOT EXISTS idx_nickname_rarity_score ON nicknames(is_dictionary, score DESC);

-- ============================================
-- Character Table Indexes
-- ============================================

-- For filtering by guild in MapleStory
CREATE INDEX IF NOT EXISTS idx_maple_guild ON maplestory_characters(guild_name) WHERE guild_name IS NOT NULL;

-- For filtering by class in MapleStory
CREATE INDEX IF NOT EXISTS idx_maple_class ON maplestory_characters(class_name) WHERE class_name IS NOT NULL;

-- For filtering by guild in LostArk
CREATE INDEX IF NOT EXISTS idx_lostark_guild ON lostark_characters(guild_name) WHERE guild_name IS NOT NULL;

-- For filtering by class in LostArk
CREATE INDEX IF NOT EXISTS idx_lostark_class ON lostark_characters(class_name) WHERE class_name IS NOT NULL;

-- For expedition level filtering
CREATE INDEX IF NOT EXISTS idx_lostark_expedition ON lostark_characters(expedition_level) WHERE expedition_level IS NOT NULL;
