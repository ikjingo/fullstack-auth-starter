---
name: refactor-code
description: Zenless 프로젝트의 코드 리팩토링 스킬. 백엔드(Kotlin/Spring Boot)와 프론트엔드(React/TypeScript) 코드의 품질 개선, 구조 최적화, 패턴 적용을 수행. 사용자가 "코드 리팩토링해 줘", "이 코드 개선해 줘", "코드 정리해 줘" 등을 요청할 때 사용.
---

# Zenless 코드 리팩토링 스킬

Zenless 프로젝트(Kotlin/Spring Boot 백엔드 + React/TypeScript 프론트엔드)의 코드 품질을 개선하고 구조를 최적화하는 스킬.

## 리팩토링 대상

$ARGUMENTS

## 리팩토링 프로세스

### 1단계: 사전 분석

1. **코드 파악**
   - 리팩토링 대상 코드의 위치와 범위 확인
   - 현재 기능과 동작 완전히 이해
   - 기존 테스트 및 문서 검토
   - 의존성과 사용처 파악

2. **문제점 식별**
   - 코드 스멜 탐지 (긴 메서드, 중복 코드, 복잡한 조건문 등)
   - 프로젝트 컨벤션 위반 사항 확인
   - 성능 이슈 파악
   - 보안 취약점 확인

### 2단계: 테스트 커버리지 확인

**백엔드 (Kotlin/Spring Boot)**
```bash
cd backend && ./gradlew test
```

**프론트엔드 (React/TypeScript)**
```bash
cd frontend && npm run lint
cd frontend && npm run build  # 타입 체크
```

- 테스트가 없으면 리팩토링 전에 먼저 작성
- 기존 동작을 검증하는 테스트 baseline 확보

### 3단계: 리팩토링 전략 수립

리팩토링 목표 정의:
- 가독성 향상
- 유지보수성 개선
- 성능 최적화
- 코드 중복 제거
- 프로젝트 컨벤션 준수

### 4단계: 점진적 리팩토링

작은 단위로 변경하며 각 변경 후 테스트 실행:

1. 한 번에 하나의 리팩토링만 수행
2. 변경 후 테스트로 기능 유지 확인
3. 작동하는 변경사항 자주 커밋

---

## 백엔드 리팩토링 가이드 (Kotlin/Spring Boot)

상세 패턴은 `references/backend-patterns.md` 참조.

### 핵심 패턴 요약

**서비스 레이어 분리**
- 컨트롤러는 요청/응답 처리만
- 비즈니스 로직은 서비스 레이어로

**DTO 변환**
- Response DTO에 `companion object { fun from(entity) }` 패턴 사용

**예외 처리**
- `sealed class DomainException` 상속 활용
- `GlobalExceptionHandler`에서 통합 처리

**Repository 최적화**
- 필요한 필드만 조회 (Projection)
- N+1 문제 방지 (JOIN FETCH)

---

## 프론트엔드 리팩토링 가이드 (React/TypeScript)

상세 패턴은 `references/frontend-patterns.md` 참조.

### 핵심 패턴 요약

**스타일 유틸리티 적용 (필수)**

`@/utils/classNames.ts`의 유틸리티를 반드시 사용:

```tsx
// Bad
<div className="w-full min-h-full bg-background">
  <div className="max-w-[1200px] mx-auto px-6 py-12">

// Good
import { pageContainer, pageContent } from '@/utils'
<div className={pageContainer}>
  <div className={pageContent}>
```

주요 유틸리티:
- `pageContainer`, `pageContent`, `pageContentNarrow` - 레이아웃
- `card`, `cardPadding`, `cardRoundedLg` - 카드
- `flexCenter`, `flexBetween`, `flexRowGap` - 플렉스
- `textButton`, `textButtonPrimary` - 텍스트 버튼

**shadcn/ui 버튼**
- Primary Action: `<Button>` 사용
- Inline Text Action: `<button className={textButton}>`

**상태 관리**
- 서버 상태: TanStack Query
- 클라이언트 상태: Zustand

**폼 처리**
- react-hook-form + Zod 조합
- `mode: 'onChange'`로 실시간 검증

---

## 리팩토링 체크리스트

### 공통
- [ ] 테스트 커버리지 확인 완료
- [ ] 기존 기능 동작 유지 확인
- [ ] 코드 중복 제거
- [ ] 네이밍 개선 (명확하고 일관된 이름)
- [ ] 불필요한 코드 제거

### 백엔드
- [ ] 서비스 레이어 분리
- [ ] DTO 변환 패턴 적용
- [ ] 예외 처리 표준화
- [ ] N+1 쿼리 문제 해결
- [ ] 트랜잭션 범위 최적화

### 프론트엔드
- [ ] 스타일 유틸리티 적용 (`@/utils/classNames.ts`)
- [ ] shadcn/ui 버튼 가이드 준수
- [ ] TanStack Query로 서버 상태 관리
- [ ] react-hook-form + Zod로 폼 처리
- [ ] 컴포넌트 크기 적정화 (100줄 이상 분리 고려)

---

## 리팩토링 완료 후

1. **테스트 실행**
   ```bash
   # 백엔드
   cd backend && ./gradlew test

   # 프론트엔드
   cd frontend && npm run lint && npm run build
   ```

2. **변경사항 커밋**
   ```
   [refactor] 리팩토링 내용 설명
   ```

## Resources

### references/
- `backend-patterns.md` - 백엔드 리팩토링 상세 패턴
- `frontend-patterns.md` - 프론트엔드 리팩토링 상세 패턴
