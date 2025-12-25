-- V5: 세션 정보 필드 추가 (디바이스/클라이언트 식별용)

ALTER TABLE refresh_tokens
    ADD COLUMN user_agent VARCHAR(500);

ALTER TABLE refresh_tokens
    ADD COLUMN ip_address VARCHAR(50);

ALTER TABLE refresh_tokens
    ADD COLUMN device_info VARCHAR(100);

COMMENT ON COLUMN refresh_tokens.user_agent IS '클라이언트 User-Agent';
COMMENT ON COLUMN refresh_tokens.ip_address IS '클라이언트 IP 주소';
COMMENT ON COLUMN refresh_tokens.device_info IS '디바이스 정보 (파싱된 요약)';
