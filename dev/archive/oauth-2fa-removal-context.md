# OAuth/2FA Removal - Context

**Status**: ✅ Completed
**Last Updated**: 2025-12-25

## Overview

Zenless 프로젝트에서 fullstack-auth-starter로 분리하면서 OAuth 소셜 로그인과 2FA 기능을 제거한 작업입니다.

## Completed Work

### 1. 프론트엔드 정리
- `SocialIcons.tsx` 삭제
- `profile.ts`에서 소셜 계정 연동 API 함수 제거
- `components/common/index.ts`에서 SocialIcons export 제거
- 로케일 파일(ko.ts, en.ts, types.ts)에서 소셜/2FA 관련 메시지 제거
- `errors.ts`에서 SocialAuthErrorType, TwoFactorErrorType 제거

### 2. 백엔드 설정 정리
- `SecurityConfig.kt`: Google OAuth, forgot-password 엔드포인트 제거
- `ContentSecurityPolicyFilter.kt`: Google accounts CSP 정책 제거
- `RateLimitFilter.kt`: 비밀번호 재설정, 2FA 경로 Rate Limit 제거

### 3. 의존성 및 설정 제거
- `build.gradle.kts`: Google OAuth (google-api-client), TOTP (totp) 의존성 제거
- `application.yml`: Google OAuth, password-reset 설정 제거
- `application-local.yml`: Google OAuth 설정 제거
- `application-test.yml`: Google OAuth, password-reset 설정 제거

### 4. 도메인 모델 정리
- `AuthProvider.kt`: GOOGLE, KAKAO, NAVER 제거 (LOCAL만 유지)

### 5. 테스트 및 문서 업데이트
- `RateLimitFilterTest.kt`: 2FA, password forgot 테스트 제거
- `ContentSecurityPolicyFilterTest.kt`: Google accounts 테스트 수정
- `PasswordServiceTest.kt`: OAuth 참조 테스트명 수정
- `auth-api/README.md`: 소셜 로그인/2FA 문서 제거
- `core-api/README.md`: 소셜/2FA 에러 타입 문서 제거
- `openapi3.yaml`: linkedSocialAccounts 없이 재생성

### 6. 코드 주석 정리
- `AuthController.kt`: SocialAuthController 참조 제거
- `PasswordService.kt`: OAuth 참조 주석 수정

## Commits Made

1. `[refactor] [FULL] 소셜 로그인 및 2FA 관련 코드 정리`
2. `[refactor] [FULL] 보안 설정 및 에러 타입 정리`
3. `[refactor] [BE] OAuth/2FA 의존성 및 참조 제거`
4. `[chore] [BE] 설정 파일 정리 및 API 문서 재생성`

## Verification

- 백엔드 컴파일: ✅ 성공
- 백엔드 테스트: ✅ 성공
- 프론트엔드 빌드: ✅ 성공
- OAuth/Social/2FA 참조 검색: ✅ 없음

## Key Files Modified

### Backend
- `backend/api/auth-api/build.gradle.kts`
- `backend/api/auth-api/src/main/resources/application.yml`
- `backend/api/auth-api/src/main/resources/application-local.yml`
- `backend/api/auth-api/src/test/resources/application-test.yml`
- `backend/api/auth-api/src/main/kotlin/com/starter/api/auth/config/SecurityConfig.kt`
- `backend/api/auth-api/src/main/kotlin/com/starter/api/auth/security/ContentSecurityPolicyFilter.kt`
- `backend/api/auth-api/src/main/kotlin/com/starter/api/auth/security/RateLimitFilter.kt`
- `backend/api/auth-api/src/main/kotlin/com/starter/api/auth/controller/AuthController.kt`
- `backend/api/auth-api/src/main/kotlin/com/starter/api/auth/service/auth/PasswordService.kt`
- `backend/storage/db-core/src/main/kotlin/com/starter/storage/db/user/AuthProvider.kt`
- `backend/api/auth-api/README.md`
- `backend/core/core-api/README.md`

### Frontend
- `frontend/src/api/services/profile.ts`
- `frontend/src/api/client/errors.ts`
- `frontend/src/components/common/index.ts`
- `frontend/src/locales/ko.ts`
- `frontend/src/locales/en.ts`
- `frontend/src/locales/types.ts`

### Deleted Files
- `frontend/src/components/common/SocialIcons.tsx`

## Lessons Learned

1. **의존성 제거 순서**: 코드 → 설정 → 의존성 순으로 제거해야 빌드 오류 방지
2. **테스트 업데이트**: 기능 제거 시 관련 테스트도 함께 업데이트 필요
3. **OpenAPI 재생성**: 응답 DTO 변경 시 openapi3.yaml 재생성 필요
4. **ktlint 검사**: 코드 삭제 시 발생할 수 있는 포맷팅 오류 주의
