-- Flyway Migration V1: Initial Schema
-- Generated from existing JPA entities

-- ============================================
-- User Management Tables
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    profile_image_url VARCHAR(500),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    lockout_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS social_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_social_accounts_provider_provider_id UNIQUE (provider, provider_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_codes (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Token Management Tables
-- ============================================

CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_token_blacklist_token ON token_blacklist(token);
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires_at ON token_blacklist(expires_at);

CREATE TABLE IF NOT EXISTS api_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    service_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    token VARCHAR(500) NOT NULL,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Nickname Service Tables
-- ============================================

CREATE TABLE IF NOT EXISTS nicknames (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    score INT NOT NULL DEFAULT 0,
    is_dictionary BOOLEAN NOT NULL DEFAULT FALSE,
    jamo_count INT NOT NULL DEFAULT 0,
    char_composition VARCHAR(20) NOT NULL DEFAULT 'MIXED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_nickname_score ON nicknames(score);
CREATE INDEX IF NOT EXISTS idx_nickname_is_dictionary ON nicknames(is_dictionary);
CREATE INDEX IF NOT EXISTS idx_nickname_char_composition ON nicknames(char_composition);

CREATE TABLE IF NOT EXISTS maplestory_characters (
    id BIGSERIAL PRIMARY KEY,
    nickname_id BIGINT NOT NULL UNIQUE REFERENCES nicknames(id) ON DELETE CASCADE,
    world_name VARCHAR(50),
    class_name VARCHAR(50),
    character_level INT,
    guild_name VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_maple_world ON maplestory_characters(world_name);
CREATE INDEX IF NOT EXISTS idx_maple_level ON maplestory_characters(character_level);

CREATE TABLE IF NOT EXISTS lostark_characters (
    id BIGSERIAL PRIMARY KEY,
    nickname_id BIGINT NOT NULL UNIQUE REFERENCES nicknames(id) ON DELETE CASCADE,
    server_name VARCHAR(50),
    class_name VARCHAR(50),
    item_level DECIMAL(10, 2),
    guild_name VARCHAR(50),
    expedition_level INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lostark_server ON lostark_characters(server_name);
CREATE INDEX IF NOT EXISTS idx_lostark_item_level ON lostark_characters(item_level);

CREATE TABLE IF NOT EXISTS dictionary_words (
    id BIGSERIAL PRIMARY KEY,
    nickname_id BIGINT NOT NULL UNIQUE REFERENCES nicknames(id) ON DELETE CASCADE,
    source VARCHAR(20) NOT NULL,
    pos VARCHAR(50),
    definition TEXT,
    origin VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dictionary_source ON dictionary_words(source);
CREATE INDEX IF NOT EXISTS idx_dictionary_pos ON dictionary_words(pos);
CREATE INDEX IF NOT EXISTS idx_dictionary_origin ON dictionary_words(origin);

-- ============================================
-- Score Configuration Tables
-- ============================================

CREATE TABLE IF NOT EXISTS score_rules (
    id BIGSERIAL PRIMARY KEY,
    service_type VARCHAR(20) NOT NULL,
    rule_type VARCHAR(30) NOT NULL,
    min_value INT,
    max_value INT,
    score INT NOT NULL DEFAULT 0,
    description VARCHAR(100),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_score_rule_service ON score_rules(service_type);
CREATE INDEX IF NOT EXISTS idx_score_rule_type ON score_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_score_rule_service_type ON score_rules(service_type, rule_type);

CREATE TABLE IF NOT EXISTS rarity_thresholds (
    id BIGSERIAL PRIMARY KEY,
    rarity_type VARCHAR(10) NOT NULL UNIQUE,
    min_score INT NOT NULL,
    description VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nickname_applied_rules (
    id BIGSERIAL PRIMARY KEY,
    nickname_id BIGINT NOT NULL UNIQUE REFERENCES nicknames(id) ON DELETE CASCADE,
    length_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    jamo_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    char_composition_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    maple_level_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    lostark_item_level_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    dictionary_rule_id BIGINT REFERENCES score_rules(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_applied_rule_nickname ON nickname_applied_rules(nickname_id);

-- ============================================
-- Service Settings Table
-- ============================================

CREATE TABLE IF NOT EXISTS service_settings (
    service_type VARCHAR(20) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Insert default service settings
INSERT INTO service_settings (service_type, enabled) VALUES
    ('MAPLESTORY', TRUE),
    ('LOSTARK', TRUE),
    ('DICTIONARY', TRUE)
ON CONFLICT (service_type) DO NOTHING;
