-- Flyway Migration V6: Additional Composite Indexes
-- Add composite indexes for frequently used query patterns

-- ============================================
-- Refresh Token Composite Indexes
-- ============================================

-- For session management queries (user_id + revoked + expires_at)
-- Used by: findActiveSessionsByUser, countActiveSessionsByUser, enforceSessionLimit
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_active
    ON refresh_tokens(user_id, revoked, expires_at)
    WHERE revoked = FALSE;

-- ============================================
-- API Token Composite Indexes
-- ============================================

-- For finding token by user and service
-- Used by: findByUserAndServiceId, existsByUserAndServiceId
CREATE INDEX IF NOT EXISTS idx_api_tokens_user_service
    ON api_tokens(user_id, service_id);

-- ============================================
-- Token Blacklist Indexes
-- ============================================

-- For blacklist lookup and cleanup
-- Used by: existsByToken (high frequency - every auth request)
CREATE INDEX IF NOT EXISTS idx_token_blacklist_token
    ON token_blacklist(token);

-- For cleanup of expired blacklisted tokens
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires_at
    ON token_blacklist(expires_at);

-- ============================================
-- Password Reset Code Composite Indexes
-- ============================================

-- For code verification queries
-- Used by: findByEmailAndCodeAndUsedFalse, findByEmailAndCodeAndVerifiedTrueAndUsedFalse
CREATE INDEX IF NOT EXISTS idx_password_reset_email_code
    ON password_reset_codes(email, code);
