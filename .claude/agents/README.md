# Agents

전문화된 서브에이전트들로, 특정 도메인 작업을 위해 자동으로 실행됩니다.

---

## 에이전트란?

에이전트는 특정 역할에 전문화된 Claude의 분신입니다:
- 독립적인 컨텍스트와 지시사항
- 제한된 도구 세트
- 특화된 출력 형식
- 자동 또는 수동 호출

**장점:**
- 복잡한 작업을 병렬로 분산 처리
- 각 에이전트가 해당 도메인에 최적화
- 메인 대화의 컨텍스트 절약

---

## 사용 가능한 에이전트 (11개)

### 개발 에이전트

| 에이전트 | 설명 | 모델 | 도구 |
|---------|------|------|------|
| **frontend-developer** | React 컴포넌트, 반응형 디자인, 상태 관리 | sonnet | Read, Write, Edit, Bash |
| **backend-architect** | RESTful API, 마이크로서비스, DB 스키마 | sonnet | Read, Write, Edit, Bash |
| **database-architect** | 데이터 모델링, 스케일링, 기술 선택 | opus | Read, Write, Edit, Bash |

### 품질 보증 에이전트

| 에이전트 | 설명 | 모델 | 도구 |
|---------|------|------|------|
| **code-reviewer** | 코드 품질, 보안, 유지보수성 리뷰 | sonnet | Read, Write, Edit, Bash, Grep |
| **architect-reviewer** | SOLID 원칙, 아키텍처 일관성 검토 | opus | All tools |
| **test-engineer** | 테스트 전략, 자동화, 커버리지 분석 | sonnet | Read, Write, Edit, Bash |
| **test-automator** | 단위/통합/E2E 테스트 스위트 생성 | sonnet | Read, Write, Edit, Bash |

### 디자인 에이전트

| 에이전트 | 설명 | 모델 | 도구 |
|---------|------|------|------|
| **ui-ux-designer** | 사용자 연구, 와이어프레임, 디자인 시스템 | sonnet | Read, Write, Edit |

### 운영 에이전트

| 에이전트 | 설명 | 모델 | 도구 |
|---------|------|------|------|
| **context-manager** | 멀티 에이전트 워크플로우, 세션 컨텍스트 관리 | opus | Read, Write, Edit, TodoWrite |
| **task-decomposition-expert** | 복잡한 목표 분해, 워크플로우 설계 | sonnet | Read, Write |
| **prompt-engineer** | LLM 프롬프트 최적화, AI 시스템 설계 | opus | Read, Write, Edit |

---

## 에이전트 사용법

### 자동 호출 (권장)

Claude가 작업 컨텍스트에 따라 적절한 에이전트를 자동으로 호출합니다:

```
사용자: "이 PR의 코드 리뷰해줘"
→ Claude가 code-reviewer 에이전트 자동 호출

사용자: "새 API 엔드포인트 설계해줘"
→ Claude가 backend-architect 에이전트 자동 호출
```

### Task 도구로 호출

```
Claude: Task 도구를 사용하여 code-reviewer 에이전트를 실행합니다.
{
  "subagent_type": "code-reviewer",
  "prompt": "최근 변경사항에 대한 코드 리뷰를 수행하세요."
}
```

---

## 에이전트 구조

각 에이전트는 YAML frontmatter를 포함한 마크다운 파일입니다:

```markdown
---
name: agent-name
description: 에이전트 설명 (Task 도구에서 사용)
tools: Read, Write, Edit, Bash    # 허용된 도구
model: sonnet                     # sonnet | opus | haiku
---

에이전트 지시사항...

## Focus Areas
- 전문 영역 1
- 전문 영역 2

## Approach
1. 접근 방식 단계
2. ...

## Output
- 출력 형식
- ...
```

### Frontmatter 필드

| 필드 | 필수 | 설명 |
|-----|------|------|
| name | O | 에이전트 식별자 (파일명과 일치) |
| description | O | Task 도구에서 표시되는 설명 |
| tools | O | 사용 가능한 도구 목록 |
| model | X | 기본값: sonnet, 복잡한 작업은 opus |

### 모델 선택 가이드

| 모델 | 사용 시나리오 |
|-----|-------------|
| **haiku** | 빠른 단순 작업 (파일 탐색, 간단한 수정) |
| **sonnet** | 대부분의 개발 작업 (기본값) |
| **opus** | 복잡한 아키텍처 분석, 심층 리뷰 |

---

## 에이전트별 상세

### code-reviewer

**언제 사용:** 코드 변경 후 품질 검토가 필요할 때

**자동 수행:**
1. `git diff`로 최근 변경사항 확인
2. 변경된 파일에 집중
3. 우선순위별 피드백 제공

**리뷰 체크리스트:**
- 코드 가독성과 간결성
- 함수/변수 네이밍
- 중복 코드
- 에러 처리
- 보안 (시크릿, API 키)
- 테스트 커버리지
- 성능 고려사항

### architect-reviewer

**언제 사용:** 구조적 변경이나 새로운 서비스 추가 시

**전문 영역:**
- SOLID 원칙 준수
- 의존성 방향 검증
- 적절한 추상화 수준
- 스케일링/유지보수 이슈 식별

### context-manager

**언제 사용:** 복잡한 프로젝트에서 세션 간 컨텍스트 유지가 필요할 때

**주요 기능:**
- 에이전트 출력에서 핵심 결정 추출
- 재사용 가능한 패턴 문서화
- 컴포넌트 간 통합 포인트 추적
- 미해결 이슈와 TODO 관리

### task-decomposition-expert

**언제 사용:** 복잡한 목표를 여러 단계로 분해해야 할 때

**접근 방식:**
1. 사용자 목표 분석
2. 관리 가능한 컴포넌트로 분해
3. 최적의 도구/에이전트 조합 식별
4. 워크플로우 아키텍처 설계

---

## 새 에이전트 추가

### 1. 에이전트 파일 생성

```bash
touch .claude/agents/my-agent.md
```

### 2. Frontmatter 작성

```markdown
---
name: my-agent
description: 에이전트 설명 (자동 호출 트리거에 사용)
tools: Read, Write, Edit
model: sonnet
---
```

### 3. 지시사항 작성

에이전트의 역할, 전문 영역, 접근 방식, 출력 형식을 명시합니다.

### 4. 테스트

```
사용자: "Task 도구로 my-agent 에이전트를 테스트해줘"
```

---

## 모범 사례

### 에이전트 설계

1. **단일 책임**: 하나의 에이전트는 하나의 도메인에 집중
2. **도구 최소화**: 필요한 도구만 허용 (보안 + 효율)
3. **명확한 출력**: 예상 출력 형식을 명시
4. **재사용성**: 프로젝트 독립적인 일반 지침

### 에이전트 호출

1. **병렬 실행**: 독립적인 작업은 여러 에이전트 동시 호출
2. **적절한 모델**: 작업 복잡도에 맞는 모델 선택
3. **컨텍스트 전달**: 충분한 배경 정보 제공
4. **결과 검증**: 에이전트 출력 검토 후 적용

---

## 트러블슈팅

### 에이전트가 호출되지 않음

**확인사항:**
1. `.claude/agents/` 디렉토리에 파일이 있는지
2. frontmatter가 올바른 형식인지
3. `name` 필드가 파일명과 일치하는지

### 에이전트가 도구를 사용하지 못함

**확인사항:**
1. `tools` 필드에 필요한 도구가 포함되어 있는지
2. 프로젝트 `settings.json`에서 도구가 허용되어 있는지

### 에이전트 출력이 부적절함

**해결방법:**
1. 지시사항을 더 구체적으로 수정
2. 예시 출력 추가
3. 모델을 opus로 업그레이드

---

## 관련 문서

- [Skills README](./../skills/README.md) - 도메인별 스킬 문서
- [Commands README](./../commands/) - 슬래시 커맨드 문서
- [Hooks README](./../hooks/README.md) - 훅 시스템 문서
