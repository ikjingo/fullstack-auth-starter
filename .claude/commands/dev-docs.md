---
description: 복잡한 기능 개발을 위한 구조화된 전략 계획 및 dev docs 생성
argument-hint: 계획할 내용을 설명하세요 (예: "인증 시스템 리팩토링", "마이크로서비스 구현")
---

당신은 전략적 계획 전문가입니다. 다음 작업을 위한 포괄적이고 실행 가능한 계획을 작성하세요: $ARGUMENTS

## 지시사항

### 1. 요청 분석
- 요청의 범위와 복잡도를 파악합니다
- 관련 코드베이스 파일을 분석하여 현재 상태를 이해합니다

### 2. 구조화된 계획 작성

다음 내용을 포함하여 계획을 작성하세요:

**Executive Summary (개요)**
- 무엇을 왜 만드는지

**Current State Analysis (현재 상태 분석)**
- 현재 시스템 상태
- 관련 코드 및 의존성

**Proposed Future State (제안된 미래 상태)**
- 목표 아키텍처
- 예상 개선 효과

**Implementation Phases (구현 단계)**
- 논리적 섹션으로 나눈 단계
- 각 단계의 작업들과 수락 기준
- 작업 간 의존성
- 노력 수준 추정 (S/M/L/XL)

**Risk Assessment (리스크 평가)**
- 잠재적 위험과 완화 전략

**Success Metrics (성공 지표)**
- 완료 기준

### 3. 작업 관리 구조 생성

다음 파일들을 생성하세요:

**디렉토리:** `dev/active/[task-name]/` (프로젝트 루트 기준)

**파일들:**
- `[task-name]-plan.md` - 포괄적인 계획
- `[task-name]-context.md` - 핵심 파일, 결정사항, 의존성
- `[task-name]-tasks.md` - 진행 상황 추적을 위한 체크리스트

각 파일에 "Last Updated: YYYY-MM-DD" 포함

### 4. 품질 기준

- 계획은 필요한 모든 컨텍스트를 자체적으로 포함해야 함
- 명확하고 실행 가능한 언어 사용
- 관련 기술적 세부사항 포함
- 기술적 및 비즈니스적 관점 모두 고려
- 잠재적 위험과 엣지 케이스 고려

### 5. context.md 필수 섹션

```markdown
## SESSION PROGRESS (YYYY-MM-DD)

### ✅ COMPLETED
- 완료된 작업 목록

### 🟡 IN PROGRESS
- 현재 진행 중인 작업
- 작업 중인 파일 경로

### ⚠️ BLOCKERS
- 진행을 막는 문제들

## Quick Resume
다음 세션에서 작업을 재개하려면:
1. 이 파일 읽기
2. [구체적인 다음 단계]
3. tasks.md에서 남은 작업 확인
```

## 참고

- 이 커맨드는 plan mode를 종료한 후 명확한 비전이 있을 때 사용하기 좋습니다
- 컨텍스트 리셋을 견딜 수 있는 영구적인 작업 구조를 생성합니다
- dev/README.md 파일의 가이드라인을 참조하세요
