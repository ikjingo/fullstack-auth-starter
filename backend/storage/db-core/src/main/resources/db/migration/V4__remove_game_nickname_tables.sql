-- Flyway Migration V4: Remove Game Nickname Tables
-- This migration removes all game-related tables that are no longer used

-- ============================================
-- Drop Indexes First (for tables that will be dropped)
-- ============================================

-- Nickname related indexes
DROP INDEX IF EXISTS idx_nickname_score;
DROP INDEX IF EXISTS idx_nickname_is_dictionary;
DROP INDEX IF EXISTS idx_nickname_char_composition;
DROP INDEX IF EXISTS idx_nickname_trgm;
DROP INDEX IF EXISTS idx_nickname_rarity_score;

-- MapleStory indexes
DROP INDEX IF EXISTS idx_maple_world;
DROP INDEX IF EXISTS idx_maple_level;

-- LostArk indexes
DROP INDEX IF EXISTS idx_lostark_server;
DROP INDEX IF EXISTS idx_lostark_item_level;

-- Dictionary indexes
DROP INDEX IF EXISTS idx_dictionary_source;
DROP INDEX IF EXISTS idx_dictionary_pos;
DROP INDEX IF EXISTS idx_dictionary_origin;

-- Score rule indexes
DROP INDEX IF EXISTS idx_score_rule_service;
DROP INDEX IF EXISTS idx_score_rule_type;
DROP INDEX IF EXISTS idx_score_rule_service_type;

-- Applied rules index
DROP INDEX IF EXISTS idx_applied_rule_nickname;

-- ============================================
-- Drop Tables (in dependency order)
-- ============================================

-- Tables with foreign keys first
DROP TABLE IF EXISTS nickname_applied_rules CASCADE;
DROP TABLE IF EXISTS maplestory_characters CASCADE;
DROP TABLE IF EXISTS lostark_characters CASCADE;
DROP TABLE IF EXISTS dictionary_words CASCADE;

-- Then parent tables
DROP TABLE IF EXISTS nicknames CASCADE;
DROP TABLE IF EXISTS score_rules CASCADE;
DROP TABLE IF EXISTS rarity_thresholds CASCADE;
DROP TABLE IF EXISTS service_settings CASCADE;
