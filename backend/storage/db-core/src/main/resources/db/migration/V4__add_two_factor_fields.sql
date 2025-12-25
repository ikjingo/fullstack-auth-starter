-- V4: 2FA (Two-Factor Authentication) 필드 추가

ALTER TABLE users
    ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
    ADD COLUMN two_factor_secret VARCHAR(64);

COMMENT ON COLUMN users.two_factor_enabled IS '2차 인증 활성화 여부';
COMMENT ON COLUMN users.two_factor_secret IS 'TOTP 시크릿 키';
