# Skills

컨텍스트에 따라 자동 활성화되는 프로덕션 테스트된 스킬들입니다.

---

## 스킬이란?

스킬은 Claude가 필요할 때 로드하는 모듈화된 지식 베이스입니다. 제공하는 것:
- 도메인별 가이드라인
- 모범 사례
- 코드 예시
- 피해야 할 안티패턴

**문제점:** 기본적으로 스킬은 자동으로 활성화되지 않습니다.

**해결책:** 이 프로젝트는 스킬을 자동 활성화하는 hooks + skill-rules.json 설정을 포함합니다.

---

## 사용 가능한 스킬 (12개)

### 핵심 개발 스킬

| 스킬 | 설명 | 우선순위 |
|-----|------|---------|
| **backend-dev-guidelines** | Kotlin/Spring Boot 백엔드 개발 가이드라인 | HIGH |
| **frontend-dev-guidelines** | React/TypeScript/Tailwind 프론트엔드 가이드라인 | HIGH |
| **backend-test-generator** | JUnit5/MockK 기반 테스트 코드 생성 | HIGH |
| **refactor-code** | 백엔드/프론트엔드 코드 리팩토링 | HIGH |

### 워크플로우 스킬

| 스킬 | 설명 | 우선순위 |
|-----|------|---------|
| **pr-workflow** | 커밋, PR 생성, 코드 리뷰, 머지 자동화 | HIGH |
| **git-commit-helper** | Git diff 분석 기반 커밋 메시지 생성 | MEDIUM |
| **start-project** | Zenless 프로젝트 개발 환경 시작 | HIGH |
| **stop-project** | Zenless 프로젝트 개발 환경 중지 | HIGH |

### 도구 스킬

| 스킬 | 설명 | 우선순위 |
|-----|------|---------|
| **webapp-testing** | Playwright 기반 웹앱 테스트 및 스크린샷 | HIGH |
| **artifacts-builder** | React/Tailwind/shadcn HTML 아티팩트 생성 | MEDIUM |
| **docx** | Word 문서(.docx) 생성 및 편집 | MEDIUM |
| **skill-creator** | 새로운 Claude Code 스킬 생성 가이드 | LOW |

---

## 자동 활성화 작동 방식

### skill-rules.json

스킬 활성화 규칙은 `.claude/skills/skill-rules.json`에 정의됩니다:

```json
{
    "skills": {
        "backend-test-generator": {
            "type": "domain",
            "enforcement": "suggest",
            "priority": "high",
            "promptTriggers": {
                "keywords": ["백엔드 테스트", "junit", "mockk"],
                "intentPatterns": ["(테스트|test).*(작성|만들|추가)"]
            },
            "fileTriggers": {
                "pathPatterns": ["backend/**/*Test.kt"]
            }
        }
    }
}
```

### 트리거 유형

**promptTriggers** - 사용자 프롬프트 기반
- `keywords`: 정확한 키워드 매칭
- `intentPatterns`: 정규식 의도 패턴 매칭

**fileTriggers** - 파일 컨텍스트 기반
- `pathPatterns`: 파일 경로 패턴
- `pathExclusions`: 제외할 경로
- `contentPatterns`: 파일 내용 패턴

### 설정 옵션

| 옵션 | 값 | 설명 |
|-----|-----|------|
| type | `domain` / `guardrail` | 도메인 지식 vs 가드레일 |
| enforcement | `suggest` / `block` / `warn` | 제안 / 차단 / 경고 |
| priority | `critical` / `high` / `medium` / `low` | 우선순위 |

---

## 스킬 사용 예시

### 프롬프트로 활성화

```
사용자: "백엔드 테스트 작성해줘"
→ backend-test-generator 스킬 제안

사용자: "PR 만들어줘"
→ pr-workflow 스킬 제안

사용자: "서버 시작해줘"
→ start-project 스킬 제안
```

### 수동 활성화

스킬을 직접 호출할 수도 있습니다:

```
사용자: "backend-dev-guidelines 스킬을 사용해서 API 만들어줘"
```

---

## 스킬 구조

각 스킬은 다음 구조를 따릅니다:

```
skill-name/
├── SKILL.md                # 메인 스킬 파일 (<500줄)
└── resources/              # (선택) 추가 리소스
    ├── topic-1.md
    └── topic-2.md
```

### 500줄 규칙

대용량 스킬은 컨텍스트 한계에 도달합니다. 해결책:
- 메인 SKILL.md는 500줄 미만 (개요 + 네비게이션)
- 리소스 파일들 각각 500줄 미만 (상세 내용)
- Claude가 필요에 따라 점진적으로 로드

---

## 새 스킬 추가

### 1. 스킬 디렉토리 생성

```bash
mkdir -p .claude/skills/my-skill
```

### 2. SKILL.md 작성

```markdown
---
name: my-skill
description: 이 스킬이 하는 일
---

# My Skill

## Purpose
[스킬 목적]

## When to Use
[자동 활성화 시나리오]

## Quick Reference
[핵심 패턴과 예시]
```

### 3. skill-rules.json에 규칙 추가

```json
{
    "my-skill": {
        "type": "domain",
        "enforcement": "suggest",
        "priority": "medium",
        "promptTriggers": {
            "keywords": ["키워드1", "키워드2"]
        }
    }
}
```

---

## 트러블슈팅

### 스킬이 활성화되지 않음

**확인사항:**
1. 스킬 디렉토리가 `.claude/skills/`에 있는지
2. `skill-rules.json`에 스킬이 등록되어 있는지
3. 키워드/패턴이 프롬프트와 매칭되는지
4. hooks가 설치되어 있고 실행 가능한지

**디버그:**
```bash
# 스킬 존재 확인
ls -la .claude/skills/

# skill-rules.json 유효성 검증
cat .claude/skills/skill-rules.json | jq .

# hooks 실행 가능 확인
ls -la .claude/hooks/*.sh
```

### 스킬이 너무 자주 활성화됨

`skill-rules.json` 업데이트:
- 키워드를 더 구체적으로
- `pathPatterns` 범위 축소
- `intentPatterns` 구체화

---

## 다음 단계

1. **작업에 맞는 스킬 선택** - 위 목록 참조
2. **활성화 테스트** - 관련 키워드로 프롬프트
3. **필요시 커스터마이즈** - skill-rules.json 수정
