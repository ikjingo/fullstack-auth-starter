# OAuth/2FA Removal - Tasks

**Status**: ✅ All Completed
**Last Updated**: 2025-12-25

## Task List

### 프론트엔드 정리
- [x] SocialIcons.tsx 삭제
- [x] profile.ts에서 소셜 계정 API 함수 제거
- [x] components/common/index.ts에서 SocialIcons export 제거
- [x] 로케일 파일에서 소셜/2FA 메시지 제거
- [x] errors.ts에서 Social/2FA 에러 타입 제거

### 백엔드 보안 설정 정리
- [x] SecurityConfig.kt: Google OAuth 엔드포인트 제거
- [x] ContentSecurityPolicyFilter.kt: Google accounts CSP 제거
- [x] RateLimitFilter.kt: 2FA/password-reset 경로 제거

### 의존성 및 설정 제거
- [x] build.gradle.kts: Google OAuth, TOTP 의존성 제거
- [x] application.yml: Google, password-reset 설정 제거
- [x] application-local.yml: Google 설정 제거
- [x] application-test.yml: Google, password-reset 설정 제거

### 도메인 모델 정리
- [x] AuthProvider.kt: 소셜 프로바이더 제거

### 테스트 정리
- [x] RateLimitFilterTest.kt: 제거된 경로 테스트 삭제
- [x] ContentSecurityPolicyFilterTest.kt: Google 테스트 수정
- [x] PasswordServiceTest.kt: OAuth 참조 테스트명 수정
- [x] ktlint 오류 수정

### 문서 업데이트
- [x] auth-api/README.md 업데이트
- [x] core-api/README.md 업데이트
- [x] openapi3.yaml 재생성

### 코드 주석 정리
- [x] AuthController.kt 주석 수정
- [x] PasswordService.kt 주석 수정

### 검증
- [x] 백엔드 컴파일 테스트
- [x] 백엔드 전체 테스트
- [x] 프론트엔드 빌드 테스트
- [x] OAuth/Social/2FA 참조 검색
- [x] GitHub 푸시

## Summary

- **Total Tasks**: 24
- **Completed**: 24
- **Remaining**: 0
